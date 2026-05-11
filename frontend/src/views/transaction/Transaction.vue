<template>
  <div>
    <div class="page-title">流水记账</div>

    <div class="search-bar">
      <el-form :model="query" inline size="small">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            value-format="yyyy-MM-dd"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.types" multiple collapse-tags placeholder="全部" style="width: 160px">
            <el-option label="收入" value="INCOME" />
            <el-option label="支出" value="EXPENSE" />
            <el-option label="转账" value="TRANSFER" />
          </el-select>
        </el-form-item>
        <el-form-item label="账户">
          <el-select v-model="query.accountIds" multiple collapse-tags placeholder="全部" style="width: 160px">
            <el-option
              v-for="item in accounts"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="分类">
          <el-cascader
            v-model="query.categoryIds"
            :options="categoryOptions"
            :props="{ multiple: true, value: 'id', label: 'label' }"
            collapse-tags
            placeholder="全部"
            style="width: 180px"
            clearable
          >
            <template slot-scope="{ data }">
              <span
                v-if="data.color"
                class="cat-color-dot"
                :style="{ backgroundColor: data.color }"
              />
              <i v-if="data.icon" :class="data.icon" style="margin-right: 4px" />
              <span>{{ data.label }}</span>
            </template>
          </el-cascader>
        </el-form-item>
        <el-form-item label="金额范围">
          <el-input-number v-model="query.minAmount" :precision="2" :controls="false" placeholder="最小" style="width: 100px" />
          <span style="margin: 0 6px">-</span>
          <el-input-number v-model="query.maxAmount" :precision="2" :controls="false" placeholder="最大" style="width: 100px" />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="query.keyword" placeholder="备注关键词" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
          <el-button icon="el-icon-download" @click="handleExport">导出</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div style="margin-bottom: 16px">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增记账</el-button>
      </div>

      <el-table v-loading="loading" :data="transactions" stripe>
        <el-table-column prop="transactionTime" label="时间" width="160">
          <template slot-scope="scope">{{ formatDateTime(scope.row.transactionTime) }}</template>
        </el-table-column>
        <el-table-column prop="typeName" label="类型" width="80">
          <template slot-scope="scope">
            <el-tag :type="typeTagType(scope.row.type)" size="small">{{ scope.row.typeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="categoryName" label="分类" min-width="140">
          <template slot-scope="scope">
            <span
              v-if="getCategoryInfo(scope.row.categoryId, 'color')"
              class="cat-color-dot"
              :style="{ backgroundColor: getCategoryInfo(scope.row.categoryId, 'color') }"
            />
            <i
              v-if="getCategoryInfo(scope.row.categoryId, 'icon')"
              :class="getCategoryInfo(scope.row.categoryId, 'icon')"
              style="margin-right: 4px"
            />
            {{ scope.row.categoryName }}
          </template>
        </el-table-column>
        <el-table-column prop="account" label="账户" min-width="180">
          <template slot-scope="scope">
            <span v-if="scope.row.type === 'TRANSFER'">
              {{ scope.row.fromAccountName }} → {{ scope.row.toAccountName }}
            </span>
            <span v-else-if="scope.row.type === 'INCOME'">{{ scope.row.toAccountName }}</span>
            <span v-else>{{ scope.row.fromAccountName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" width="120" align="right">
          <template slot-scope="scope">
            <span :class="amountClass(scope.row.type)">{{ amountPrefix(scope.row.type) }}{{ formatMoney(scope.row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top: 16px; text-align: right"
        :current-page="pagination.pageNum"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.pageSize"
        layout="total, sizes, prev, pager, next, jumper"
        :total="pagination.total"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <TransactionForm
      :visible.sync="formVisible"
      :edit-data.sync="editData"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script>
import { getTransactions, deleteTransaction } from '@/api/transaction'
import { getAccounts } from '@/api/account'
import { getCategoryTree } from '@/api/category'
import { exportTransactions } from '@/api/transaction'
import TransactionForm from '@/components/TransactionForm.vue'
import { formatMoney, formatDateTime } from '@/utils/format'

export default {
  name: 'Transaction',
  components: { TransactionForm },
  data() {
    return {
      loading: false,
      dateRange: [],
      query: {
        types: [],
        accountIds: [],
        categoryIds: [],
        minAmount: undefined,
        maxAmount: undefined,
        keyword: ''
      },
      transactions: [],
      accounts: [],
      categoryTree: [],
      pagination: {
        pageNum: 1,
        pageSize: 20,
        total: 0,
        pages: 0
      },
      formVisible: false,
      editData: null
    }
  },
  computed: {
    categoryOptions() {
      return this.categoryTree.map((group) => ({
        id: group.id,
        label: group.name,
        color: group.color,
        icon: group.icon,
        children: (group.children || []).map((child) => ({
          id: child.id,
          label: child.name,
          color: child.color,
          icon: child.icon
        }))
      }))
    }
  },
  created() {
    this.loadAccounts()
    this.loadCategories()
    this.loadTransactions()
  },
  methods: {
    formatMoney,
    formatDateTime,
    getCategoryInfo(categoryId, field) {
      for (const group of this.categoryTree) {
        if (group.id === categoryId) {
          return group[field]
        }
        const child = (group.children || []).find((c) => c.id === categoryId)
        if (child) {
          return child[field]
        }
      }
      return null
    },
    async loadAccounts() {
      try {
        const res = await getAccounts()
        this.accounts = res.data || []
      } catch (e) {
        this.accounts = []
      }
    },
    async loadCategories() {
      try {
        const res = await getCategoryTree()
        this.categoryTree = res.data || []
      } catch (e) {
        this.categoryTree = []
      }
    },
    async loadTransactions() {
      this.loading = true
      try {
        const params = {
          pageNum: this.pagination.pageNum,
          pageSize: this.pagination.pageSize,
          keyword: this.query.keyword || undefined
        }
        if (this.dateRange && this.dateRange.length === 2) {
          params.startDate = this.dateRange[0]
          params.endDate = this.dateRange[1]
        }
        if (this.query.types && this.query.types.length > 0) {
          params.types = this.query.types.join(',')
        }
        if (this.query.accountIds && this.query.accountIds.length > 0) {
          params.accountIds = this.query.accountIds.join(',')
        }
        if (this.query.categoryIds && this.query.categoryIds.length > 0) {
          const flat = []
          this.query.categoryIds.forEach((arr) => {
            flat.push(arr[arr.length - 1])
          })
          params.categoryIds = flat.join(',')
        }
        if (this.query.minAmount !== undefined && this.query.minAmount !== null) {
          params.minAmount = String(this.query.minAmount)
        }
        if (this.query.maxAmount !== undefined && this.query.maxAmount !== null) {
          params.maxAmount = String(this.query.maxAmount)
        }
        const res = await getTransactions(params)
        const data = res.data || {}
        this.transactions = data.list || []
        this.pagination.total = data.total || 0
        this.pagination.pages = data.pages || 0
      } catch (e) {
        this.transactions = []
      } finally {
        this.loading = false
      }
    },
    typeTagType(type) {
      const map = { INCOME: 'success', EXPENSE: 'danger', TRANSFER: 'primary' }
      return map[type] || ''
    },
    amountClass(type) {
      const map = { INCOME: 'income-text', EXPENSE: 'expense-text', TRANSFER: 'transfer-text' }
      return map[type] || ''
    },
    amountPrefix(type) {
      if (type === 'INCOME') return '+'
      if (type === 'EXPENSE') return '-'
      return ''
    },
    handleSearch() {
      this.pagination.pageNum = 1
      this.loadTransactions()
    },
    handleReset() {
      this.dateRange = []
      this.query = {
        types: [],
        accountIds: [],
        categoryIds: [],
        minAmount: undefined,
        maxAmount: undefined,
        keyword: ''
      }
      this.pagination.pageNum = 1
      this.loadTransactions()
    },
    handleExport() {
      const params = { format: 'excel' }
      if (this.dateRange && this.dateRange.length === 2) {
        params.startDate = this.dateRange[0]
        params.endDate = this.dateRange[1]
      }
      if (this.query.types && this.query.types.length > 0) {
        params.types = this.query.types.join(',')
      }
      if (this.query.accountIds && this.query.accountIds.length > 0) {
        params.accountIds = this.query.accountIds.join(',')
      }
      if (this.query.categoryIds && this.query.categoryIds.length > 0) {
        const flat = []
        this.query.categoryIds.forEach((arr) => {
          flat.push(arr[arr.length - 1])
        })
        params.categoryIds = flat.join(',')
      }
      if (this.query.minAmount !== undefined && this.query.minAmount !== null) {
        params.minAmount = String(this.query.minAmount)
      }
      if (this.query.maxAmount !== undefined && this.query.maxAmount !== null) {
        params.maxAmount = String(this.query.maxAmount)
      }
      params.keyword = this.query.keyword || undefined
      exportTransactions(params).catch(() => {
        // error handled by interceptor
      })
    },
    handleAdd() {
      this.editData = null
      this.formVisible = true
    },
    handleEdit(row) {
      this.editData = { ...row }
      this.formVisible = true
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该交易记录吗？', '提示', { type: 'warning' })
        await deleteTransaction(row.id)
        this.$message.success('删除成功')
        this.loadTransactions()
      } catch (e) {
        // cancel or error
      }
    },
    handleFormSuccess() {
      this.loadTransactions()
    },
    handleSizeChange(val) {
      this.pagination.pageSize = val
      this.loadTransactions()
    },
    handlePageChange(val) {
      this.pagination.pageNum = val
      this.loadTransactions()
    }
  }
}
</script>

<style scoped>
.cat-color-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
}
</style>
