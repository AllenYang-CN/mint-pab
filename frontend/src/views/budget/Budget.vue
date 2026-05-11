<template>
  <div>
    <div class="page-title">预算管理</div>

    <div class="search-bar">
      <el-form inline size="small">
        <el-form-item label="月份">
          <el-date-picker
            v-model="selectedMonth"
            type="month"
            value-format="yyyy-MM"
            placeholder="选择月份"
            @change="loadData"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-plus" @click="handleSetTotalBudget">设置总预算</el-button>
          <el-button icon="el-icon-plus" @click="handleSetCategoryBudget">设置分类预算</el-button>
        </el-form-item>
      </el-form>
    </div>

    <el-row :gutter="20" style="margin-bottom: 20px">
      <el-col :span="12">
        <el-card>
          <div slot="header">总预算</div>
          <div v-if="totalBudget">
            <div style="font-size: 28px; font-weight: 600; margin-bottom: 12px">
              {{ formatMoney(totalBudget.amount) }}
            </div>
            <div style="margin-bottom: 8px">
              已用：{{ formatMoney(totalBudget.usedAmount) }} /
              剩余：{{ formatMoney(totalBudget.remainingAmount) }}
            </div>
            <BudgetProgress
              :percentage="Math.min(parseFloat(totalBudget.usageRate || 0), 100)"
              :status="totalBudget.alertStatus || 'NORMAL'"
            />
          </div>
          <div v-else style="color: #909399; text-align: center; padding: 20px 0">
            未设置本月总预算
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <div slot="header">预算占比</div>
          <div id="budgetPieChart" style="height: 200px" />
        </el-card>
      </el-col>
    </el-row>

    <div class="table-card">
      <el-table v-loading="loading" :data="categoryBudgets" stripe>
        <el-table-column prop="categoryName" label="分类" min-width="120">
          <template slot-scope="scope">
            <span
              v-if="getCategoryColor(scope.row.categoryId)"
              class="cat-color-dot"
              :style="{ backgroundColor: getCategoryColor(scope.row.categoryId) }"
            />
            <i
              v-if="getCategoryIcon(scope.row.categoryId)"
              :class="getCategoryIcon(scope.row.categoryId)"
              style="margin-right: 4px"
            />
            {{ scope.row.categoryName }}
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="预算金额" width="120">
          <template slot-scope="scope">{{ formatMoney(scope.row.amount) }}</template>
        </el-table-column>
        <el-table-column prop="usedAmount" label="已用金额" width="120">
          <template slot-scope="scope">{{ formatMoney(scope.row.usedAmount) }}</template>
        </el-table-column>
        <el-table-column prop="remainingAmount" label="剩余金额" width="120">
          <template slot-scope="scope">{{ formatMoney(scope.row.remainingAmount) }}</template>
        </el-table-column>
        <el-table-column prop="usageRate" label="进度" min-width="200">
          <template slot-scope="scope">
            <BudgetProgress
              :percentage="Math.min(parseFloat(scope.row.usageRate || 0), 100)"
              :status="scope.row.alertStatus || 'NORMAL'"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleEditBudget(scope.row)">编辑</el-button>
            <el-button type="text" size="small" @click="handleDeleteBudget(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :visible.sync="budgetDialogVisible" :title="budgetDialogTitle" width="450px">
      <el-form ref="budgetForm" :model="budgetForm" :rules="budgetRules" label-width="100px" size="small">
        <el-form-item v-if="budgetForm.type === 'CATEGORY'" label="分类" prop="categoryId">
          <el-select v-model="budgetForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="item in expenseParentCategories"
              :key="item.id"
              :label="item.parentName"
              :value="item.id"
            >
              <span
                v-if="item.color"
                class="cat-color-dot"
                :style="{ backgroundColor: item.color }"
              />
              <i v-if="item.icon" :class="item.icon" style="margin-right: 4px" />
              {{ item.parentName }}
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="预算金额" prop="amount">
          <el-input-number
            v-model="budgetForm.amount"
            :precision="2"
            :min="0.01"
            :controls="false"
            placeholder="请输入预算金额"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="budgetDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitBudget">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getBudgetExecution, createBudget, updateBudget, deleteBudget } from '@/api/budget'
import { getCategories } from '@/api/category'
import BudgetProgress from '@/components/BudgetProgress.vue'
import { formatMoney } from '@/utils/format'
import * as echarts from 'echarts'

export default {
  name: 'Budget',
  components: { BudgetProgress },
  data() {
    return {
      loading: false,
      selectedMonth: '',
      totalBudget: null,
      categoryBudgets: [],
      categories: [],
      budgetDialogVisible: false,
      budgetDialogTitle: '设置预算',
      submitting: false,
      isEditBudget: false,
      editBudgetId: null,
      budgetForm: {
        type: 'TOTAL',
        categoryId: '',
        amount: undefined
      },
      budgetRules: {
        categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
        amount: [{ required: true, message: '请输入预算金额', trigger: 'blur' }]
      },
      pieChart: null
    }
  },
  computed: {
    expenseParentCategories() {
      const seen = new Set()
      return this.categories.filter((c) => {
        if (c.type !== 'EXPENSE') return false
        if (seen.has(c.parentName)) return false
        seen.add(c.parentName)
        return true
      })
    }
  },
  created() {
    const now = new Date()
    this.selectedMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    this.loadCategories()
    this.loadData()
  },
  mounted() {
    window.addEventListener('resize', this.resizeChart)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeChart)
    if (this.pieChart) {
      this.pieChart.dispose()
      this.pieChart = null
    }
  },
  methods: {
    formatMoney,
    getCategoryColor(categoryId) {
      const cat = this.categories.find((c) => c.id === categoryId)
      return cat ? cat.color : null
    },
    getCategoryIcon(categoryId) {
      const cat = this.categories.find((c) => c.id === categoryId)
      return cat ? cat.icon : null
    },
    async loadCategories() {
      try {
        const res = await getCategories({ type: 'EXPENSE' })
        this.categories = res.data || []
      } catch (e) {
        this.categories = []
      }
    },
    async loadData() {
      this.loading = true
      try {
        const res = await getBudgetExecution({ month: this.selectedMonth })
        const data = res.data || {}
        this.totalBudget = data.totalBudget || null
        this.categoryBudgets = data.categoryExecutions || []
        this.$nextTick(() => {
          this.renderPieChart()
        })
      } catch (e) {
        this.totalBudget = null
        this.categoryBudgets = []
      } finally {
        this.loading = false
      }
    },
    renderPieChart() {
      const dom = document.getElementById('budgetPieChart')
      if (!dom) return
      if (this.pieChart) {
        this.pieChart.dispose()
      }
      this.pieChart = echarts.init(dom)
      const data = this.categoryBudgets.map((item) => ({
        name: item.categoryName,
        value: parseFloat(item.amount || 0)
      }))
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          type: 'scroll',
          orient: 'vertical',
          right: 0,
          top: 20,
          bottom: 20,
          textStyle: { fontSize: 12 }
        },
        series: [
          {
            name: '预算占比',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['35%', '50%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 4,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 14,
                fontWeight: 'bold'
              }
            },
            data: data.length > 0 ? data : [{ name: '无数据', value: 0 }]
          }
        ]
      }
      this.pieChart.setOption(option)
    },
    resizeChart() {
      this.pieChart && this.pieChart.resize()
    },
    handleSetTotalBudget() {
      this.isEditBudget = !!(this.totalBudget && this.totalBudget.id)
      this.editBudgetId = this.totalBudget ? this.totalBudget.id : null
      this.budgetForm = {
        type: 'TOTAL',
        categoryId: '',
        amount: this.totalBudget ? parseFloat(this.totalBudget.amount) : undefined
      }
      this.budgetDialogTitle = this.isEditBudget ? '修改总预算' : '设置总预算'
      this.budgetDialogVisible = true
      this.$nextTick(() => {
        this.$refs.budgetForm && this.$refs.budgetForm.resetFields()
      })
    },
    handleSetCategoryBudget() {
      this.isEditBudget = false
      this.editBudgetId = null
      this.budgetForm = {
        type: 'CATEGORY',
        categoryId: '',
        amount: undefined
      }
      this.budgetDialogTitle = '设置分类预算'
      this.budgetDialogVisible = true
      this.$nextTick(() => {
        this.$refs.budgetForm && this.$refs.budgetForm.resetFields()
      })
    },
    handleEditBudget(row) {
      this.isEditBudget = true
      this.editBudgetId = row.id
      let categoryId = row.categoryId || ''
      const matched = this.categories.find((c) => c.id === row.categoryId)
      if (matched) {
        const representative = this.expenseParentCategories.find((c) => c.parentName === matched.parentName)
        if (representative) {
          categoryId = representative.id
        }
      }
      this.budgetForm = {
        type: row.type,
        categoryId: categoryId,
        amount: parseFloat(row.amount)
      }
      this.budgetDialogTitle = '修改预算'
      this.budgetDialogVisible = true
    },
    handleSubmitBudget() {
      this.$refs.budgetForm.validate(async (valid) => {
        if (!valid) return
        const payload = {
          month: this.selectedMonth,
          type: this.budgetForm.type,
          amount: String(this.budgetForm.amount.toFixed(2))
        }
        if (this.budgetForm.type === 'CATEGORY') {
          payload.categoryId = this.budgetForm.categoryId
        }
        this.submitting = true
        try {
          if (this.isEditBudget) {
            await updateBudget(this.editBudgetId, { amount: payload.amount })
            this.$message.success('修改成功')
          } else {
            await createBudget(payload)
            this.$message.success('设置成功')
          }
          this.budgetDialogVisible = false
          this.loadData()
        } catch (e) {
          // ignore
        } finally {
          this.submitting = false
        }
      })
    },
    async handleDeleteBudget(row) {
      try {
        await this.$confirm('确定要删除该预算吗？', '提示', { type: 'warning' })
        await deleteBudget(row.id)
        this.$message.success('删除成功')
        this.loadData()
      } catch (e) {
        // cancel or error
      }
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
