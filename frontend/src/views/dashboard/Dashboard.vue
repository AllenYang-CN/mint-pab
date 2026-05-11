<template>
  <div>
    <div class="page-title">首页</div>

    <el-alert
      v-if="budgetAlert && budgetAlert.hasBudget"
      :title="`本月预算执行：${budgetAlert.alertStatusName}（已用 ${budgetAlert.usageRate}%）`"
      :type="alertType"
      :closable="false"
      show-icon
      style="margin-bottom: 20px"
    >
      <div>
        总预算：{{ formatMoney(budgetAlert.totalBudget) }}，
        已用：{{ formatMoney(budgetAlert.usedAmount) }}，
        剩余：{{ formatMoney(budgetAlert.remainingAmount) }}
      </div>
    </el-alert>

    <el-row :gutter="20">
      <el-col :span="10">
        <el-card>
          <div slot="header">快速记账</div>
          <TransactionForm :visible.sync="formVisible" @success="handleSuccess" />
          <el-form
            ref="quickForm"
            :model="quickForm"
            :rules="quickRules"
            label-width="80px"
            size="small"
          >
            <el-form-item label="类型" prop="type">
              <el-radio-group v-model="quickForm.type" @change="handleTypeChange">
                <el-radio-button label="EXPENSE">支出</el-radio-button>
                <el-radio-button label="INCOME">收入</el-radio-button>
                <el-radio-button label="TRANSFER">转账</el-radio-button>
              </el-radio-group>
            </el-form-item>

            <el-form-item
              v-if="quickForm.type === 'EXPENSE'"
              label="支出账户"
              prop="fromAccountId"
            >
              <el-select v-model="quickForm.fromAccountId" placeholder="请选择" style="width: 100%">
                <el-option
                  v-for="item in activeAccounts"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item
              v-if="quickForm.type === 'INCOME'"
              label="收入账户"
              prop="toAccountId"
            >
              <el-select v-model="quickForm.toAccountId" placeholder="请选择" style="width: 100%">
                <el-option
                  v-for="item in activeAccounts"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>

            <template v-if="quickForm.type === 'TRANSFER'">
              <el-form-item label="转出账户" prop="fromAccountId">
                <el-select v-model="quickForm.fromAccountId" placeholder="请选择" style="width: 100%">
                  <el-option
                    v-for="item in activeAccounts"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="转入账户" prop="toAccountId">
                <el-select v-model="quickForm.toAccountId" placeholder="请选择" style="width: 100%">
                  <el-option
                    v-for="item in activeAccounts"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>
            </template>

            <el-form-item label="金额" prop="amount">
              <el-input-number
                v-model="quickForm.amount"
                :precision="2"
                :min="0.01"
                :controls="false"
                placeholder="请输入金额"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item label="分类" prop="categoryId">
              <el-cascader
                v-model="quickForm.categoryId"
                :options="categoryOptions"
                :props="{ value: 'id', label: 'label' }"
                placeholder="请选择分类"
                style="width: 100%"
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

            <el-form-item label="备注" prop="remark">
              <el-input v-model="quickForm.remark" placeholder="备注（可选）" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="submitting" @click="handleQuickSubmit">
                记一笔
              </el-button>
              <el-button @click="resetQuickForm">重置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>

      <el-col :span="14">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-card>
              <div slot="header">今日</div>
              <div class="summary-item">
                <div class="summary-label">收入</div>
                <div class="summary-value income-text">{{ formatMoney(summary.today?.income) }}</div>
              </div>
              <div class="summary-item">
                <div class="summary-label">支出</div>
                <div class="summary-value expense-text">{{ formatMoney(summary.today?.expense) }}</div>
              </div>
              <div class="summary-item">
                <div class="summary-label">笔数</div>
                <div class="summary-value">{{ summary.today?.count || 0 }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card>
              <div slot="header">本周</div>
              <div class="summary-item">
                <div class="summary-label">收入</div>
                <div class="summary-value income-text">{{ formatMoney(summary.week?.income) }}</div>
              </div>
              <div class="summary-item">
                <div class="summary-label">支出</div>
                <div class="summary-value expense-text">{{ formatMoney(summary.week?.expense) }}</div>
              </div>
              <div class="summary-item">
                <div class="summary-label">笔数</div>
                <div class="summary-value">{{ summary.week?.count || 0 }}</div>
              </div>
              <div class="summary-item">
                <div class="summary-label">日均支出</div>
                <div class="summary-value">{{ formatMoney(summary.week?.dailyAverage) }}</div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card>
              <div slot="header">本月</div>
              <div class="summary-item">
                <div class="summary-label">收入</div>
                <div class="summary-value income-text">{{ formatMoney(summary.month?.income) }}</div>
              </div>
              <div class="summary-item">
                <div class="summary-label">支出</div>
                <div class="summary-value expense-text">{{ formatMoney(summary.month?.expense) }}</div>
              </div>
              <div class="summary-item">
                <div class="summary-label">结余</div>
                <div class="summary-value">{{ formatMoney(summary.month?.balance) }}</div>
              </div>
              <div class="summary-item">
                <div class="summary-label">预算执行率</div>
                <div class="summary-value">{{ summary.month?.budgetExecutionRate || '-' }}%</div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import { getDashboardSummary } from '@/api/dashboard'
import { getAccounts } from '@/api/account'
import { getCategoryTree } from '@/api/category'
import { createTransaction } from '@/api/transaction'
import TransactionForm from '@/components/TransactionForm.vue'
import { formatMoney } from '@/utils/format'

export default {
  name: 'Dashboard',
  components: { TransactionForm },
  data() {
    return {
      summary: {},
      budgetAlert: null,
      accounts: [],
      categories: [],
      categoryTree: [],
      formVisible: false,
      submitting: false,
      quickForm: {
        type: 'EXPENSE',
        fromAccountId: '',
        toAccountId: '',
        amount: undefined,
        categoryId: '',
        remark: ''
      },
      quickRules: {
        fromAccountId: [{ required: true, message: '请选择账户', trigger: 'change' }],
        toAccountId: [{ required: true, message: '请选择账户', trigger: 'change' }],
        amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
        categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
      }
    }
  },
  computed: {
    alertType() {
      if (!this.budgetAlert) return 'info'
      const map = { NORMAL: 'success', WARNING: 'warning', OVER: 'error' }
      return map[this.budgetAlert.alertStatus] || 'info'
    },
    activeAccounts() {
      return this.accounts.filter((a) => a.status === 'ACTIVE')
    },
    categoryOptions() {
      const typeMap = { EXPENSE: 'EXPENSE', INCOME: 'INCOME', TRANSFER: 'EXPENSE' }
      const targetType = typeMap[this.quickForm.type]
      return this.categoryTree
        .filter((group) => group.type === targetType)
        .map((group) => ({
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
    this.loadData()
    this.loadAccounts()
    this.loadCategories()
  },
  methods: {
    formatMoney,
    async loadData() {
      try {
        const res = await getDashboardSummary()
        const d = res.data || {}
        this.summary = {
          today: { income: d.todayIncome, expense: d.todayExpense, count: d.todayCount },
          week: { income: d.weekIncome, expense: d.weekExpense, count: d.weekCount, dailyAverage: d.weekDailyAvgExpense },
          month: { income: d.monthIncome, expense: d.monthExpense, balance: d.monthBalance, budgetExecutionRate: d.budgetUsageRate }
        }
        if (d.budgetUsageRate != null) {
          const usageRate = d.budgetUsageRate
          const totalBudget = usageRate > 0 ? d.monthExpense / (usageRate / 100) : 0
          this.budgetAlert = {
            hasBudget: true,
            alertStatus: d.budgetStatus,
            alertStatusName: { NORMAL: '正常', WARNING: '警告', OVER: '超支' }[d.budgetStatus] || d.budgetStatus,
            usageRate: usageRate,
            totalBudget: totalBudget,
            usedAmount: d.monthExpense,
            remainingAmount: totalBudget - d.monthExpense
          }
        } else {
          this.budgetAlert = null
        }
      } catch (e) {
        // ignore
      }
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
    handleTypeChange() {
      this.quickForm.fromAccountId = ''
      this.quickForm.toAccountId = ''
      this.quickForm.categoryId = ''
    },
    resetQuickForm() {
      this.quickForm = {
        type: 'EXPENSE',
        fromAccountId: '',
        toAccountId: '',
        amount: undefined,
        categoryId: '',
        remark: ''
      }
    },
    handleQuickSubmit() {
      this.$refs.quickForm.validate(async (valid) => {
        if (!valid) return
        if (this.quickForm.type === 'TRANSFER' && this.quickForm.fromAccountId === this.quickForm.toAccountId) {
          this.$message.error('转出账户和转入账户不能相同')
          return
        }
        const payload = {
          type: this.quickForm.type,
          amount: String(this.quickForm.amount.toFixed(2)),
          categoryId: Array.isArray(this.quickForm.categoryId)
            ? this.quickForm.categoryId[this.quickForm.categoryId.length - 1]
            : this.quickForm.categoryId,
          transactionTime: this.formatNow(),
          remark: this.quickForm.remark
        }
        if (this.quickForm.type === 'EXPENSE' || this.quickForm.type === 'TRANSFER') {
          payload.fromAccountId = this.quickForm.fromAccountId
        }
        if (this.quickForm.type === 'INCOME' || this.quickForm.type === 'TRANSFER') {
          payload.toAccountId = this.quickForm.toAccountId
        }
        this.submitting = true
        try {
          await createTransaction(payload)
          this.$message.success('记账成功')
          this.resetQuickForm()
          this.loadData()
        } catch (e) {
          // ignore
        } finally {
          this.submitting = false
        }
      })
    },
    handleSuccess() {
      this.loadData()
    },
    formatNow() {
      const d = new Date()
      const pad = (n) => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    }
  }
}
</script>

<style scoped>
.summary-item {
  margin-bottom: 12px;
}

.summary-item:last-child {
  margin-bottom: 0;
}

.summary-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.summary-value {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.cat-color-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
}
</style>
