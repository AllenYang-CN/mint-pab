<template>
  <div>
    <div class="page-title">财务报表</div>

    <div class="search-bar">
      <el-form inline size="small">
        <el-form-item label="报表类型">
          <el-radio-group v-model="reportType" @change="handleTypeChange">
            <el-radio-button label="balance">资产负债表</el-radio-button>
            <el-radio-button label="income">收支表</el-radio-button>
            <el-radio-button label="cashflow">现金流量表</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="月份/日期">
          <el-date-picker
            v-if="reportType === 'balance'"
            v-model="selectedDate"
            type="date"
            value-format="yyyy-MM-dd"
            placeholder="选择日期"
            @change="loadData"
          />
          <el-date-picker
            v-else
            v-model="selectedMonth"
            type="month"
            value-format="yyyy-MM"
            placeholder="选择月份"
            @change="loadData"
          />
        </el-form-item>
      </el-form>
    </div>

    <!-- 资产负债表 -->
    <div v-show="reportType === 'balance'">
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="8">
          <el-card>
            <div style="font-size: 14px; color: #909399; margin-bottom: 8px">总资产</div>
            <div style="font-size: 28px; font-weight: 600; color: #303133">
              {{ formatMoney(balanceSheet.totalAssets) }}
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <div slot="header">资产分布</div>
            <div id="balancePieChart" style="height: 300px" />
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <div slot="header">账户明细</div>
            <div v-for="group in balanceSheet.accountGroups" :key="group.type" style="margin-bottom: 16px">
              <div style="font-weight: 600; margin-bottom: 8px; color: #303133">
                {{ group.typeName }}（{{ formatMoney(group.totalBalance) }}，占比 {{ getGroupPercentage(group) }}%）
              </div>
              <el-table :data="group.accounts" size="small" :show-header="false" border>
                <el-table-column prop="name" label="账户" />
                <el-table-column prop="currentBalance" label="余额" align="right">
                  <template slot-scope="scope">{{ formatMoney(scope.row.currentBalance) }}</template>
                </el-table-column>
              </el-table>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 收支表 -->
    <div v-show="reportType === 'income'">
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="8">
          <el-card>
            <div style="font-size: 14px; color: #909399; margin-bottom: 8px">总收入</div>
            <div style="font-size: 28px; font-weight: 600; color: #67c23a">
              {{ formatMoney(incomeExpense.totalIncome) }}
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <div style="font-size: 14px; color: #909399; margin-bottom: 8px">总支出</div>
            <div style="font-size: 28px; font-weight: 600; color: #f56c6c">
              {{ formatMoney(incomeExpense.totalExpense) }}
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card>
            <div style="font-size: 14px; color: #909399; margin-bottom: 8px">结余</div>
            <div style="font-size: 28px; font-weight: 600; color: #409eff">
              {{ formatMoney(incomeExpense.balance) }}
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="12">
          <el-card>
            <div slot="header">收入 vs 支出</div>
            <div id="incomeBarChart" style="height: 300px" />
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <div slot="header">支出分类占比</div>
            <div id="expensePieChart" style="height: 300px" />
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <div slot="header">收入明细</div>
            <el-table :data="incomeExpense.incomeItems" size="small" stripe>
              <el-table-column prop="categoryName" label="分类" />
              <el-table-column prop="amount" label="金额" align="right">
                <template slot-scope="scope">{{ formatMoney(scope.row.amount) }}</template>
              </el-table-column>
              <el-table-column prop="percentage" label="占比" width="100">
                <template slot-scope="scope">{{ scope.row.percentage }}%</template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <div slot="header">支出明细</div>
            <el-table :data="incomeExpense.expenseItems" size="small" stripe>
              <el-table-column prop="categoryName" label="分类" />
              <el-table-column prop="amount" label="金额" align="right">
                <template slot-scope="scope">{{ formatMoney(scope.row.amount) }}</template>
              </el-table-column>
              <el-table-column prop="percentage" label="占比" width="100">
                <template slot-scope="scope">{{ scope.row.percentage }}%</template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 现金流量表 -->
    <div v-show="reportType === 'cashflow'">
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="24">
          <el-card>
            <div slot="header">各账户现金流量</div>
            <div id="cashFlowBarChart" style="height: 300px" />
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-card>
            <div slot="header">流入来源</div>
            <el-table :data="cashFlow.incomeSourceAnalysis" size="small" stripe>
              <el-table-column prop="categoryName" label="分类" />
              <el-table-column prop="amount" label="金额" align="right">
                <template slot-scope="scope">{{ formatMoney(scope.row.amount) }}</template>
              </el-table-column>
              <el-table-column prop="percentage" label="占比" width="100">
                <template slot-scope="scope">{{ scope.row.percentage }}%</template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <div slot="header">流出去向</div>
            <el-table :data="cashFlow.expenseDestinationAnalysis" size="small" stripe>
              <el-table-column prop="categoryName" label="分类" />
              <el-table-column prop="amount" label="金额" align="right">
                <template slot-scope="scope">{{ formatMoney(scope.row.amount) }}</template>
              </el-table-column>
              <el-table-column prop="percentage" label="占比" width="100">
                <template slot-scope="scope">{{ scope.row.percentage }}%</template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" style="margin-top: 20px">
        <el-col :span="24">
          <el-card>
            <div slot="header">账户现金流量明细</div>
            <el-table :data="cashFlow.accountFlows" size="small" stripe>
              <el-table-column prop="accountName" label="账户" min-width="160" />
              <el-table-column prop="accountType" label="类型" width="120">
                <template slot-scope="scope">
                  <el-tag size="small">{{ scope.row.accountType }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="beginBalance" label="期初余额" width="120" align="right">
                <template slot-scope="scope">{{ formatMoney(scope.row.beginBalance) }}</template>
              </el-table-column>
              <el-table-column prop="inflow" label="本期流入" width="120" align="right">
                <template slot-scope="scope">
                  <span class="income-text">{{ formatMoney(scope.row.inflow) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="outflow" label="本期流出" width="120" align="right">
                <template slot-scope="scope">
                  <span class="expense-text">{{ formatMoney(scope.row.outflow) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="netFlow" label="净流量" width="120" align="right">
                <template slot-scope="scope">{{ formatMoney(scope.row.netFlow) }}</template>
              </el-table-column>
              <el-table-column prop="endBalance" label="期末余额" width="120" align="right">
                <template slot-scope="scope">{{ formatMoney(scope.row.endBalance) }}</template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
import { getBalanceSheet, getIncomeExpense, getCashFlow } from '@/api/report'
import { formatMoney } from '@/utils/format'
import * as echarts from 'echarts'

export default {
  name: 'Report',
  data() {
    return {
      reportType: 'balance',
      selectedMonth: '',
      selectedDate: '',
      balanceSheet: { totalAssets: '0.00', accountGroups: [] },
      incomeExpense: { totalIncome: '0.00', totalExpense: '0.00', balance: '0.00', incomeItems: [], expenseItems: [] },
      cashFlow: { accountFlows: [], incomeSourceAnalysis: [], expenseDestinationAnalysis: [] },
      charts: {}
    }
  },
  created() {
    const now = new Date()
    this.selectedMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
    this.selectedDate = this.formatDate(now)
    this.loadData()
  },
  mounted() {
    window.addEventListener('resize', this.resizeCharts)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeCharts)
    Object.values(this.charts).forEach((chart) => {
      if (chart) chart.dispose()
    })
  },
  methods: {
    formatMoney,
    getGroupPercentage(group) {
      const total = parseFloat(this.balanceSheet.totalAssets || 0)
      if (total === 0) return '0.0'
      return ((parseFloat(group.totalBalance || 0) / total) * 100).toFixed(1)
    },
    formatDate(date) {
      const d = new Date(date)
      const pad = (n) => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
    },
    async loadData() {
      if (this.reportType === 'balance') {
        await this.loadBalanceSheet()
      } else if (this.reportType === 'income') {
        await this.loadIncomeExpense()
      } else if (this.reportType === 'cashflow') {
        await this.loadCashFlow()
      }
    },
    async loadBalanceSheet() {
      try {
        const res = await getBalanceSheet({ date: this.selectedDate })
        this.balanceSheet = res.data || { totalAssets: '0.00', accountGroups: [] }
        this.$nextTick(() => {
          this.renderBalancePieChart()
        })
      } catch (e) {
        this.balanceSheet = { totalAssets: '0.00', accountGroups: [] }
      }
    },
    async loadIncomeExpense() {
      try {
        const res = await getIncomeExpense({ month: this.selectedMonth })
        this.incomeExpense = res.data || { totalIncome: '0.00', totalExpense: '0.00', balance: '0.00', incomeItems: [], expenseItems: [] }
        this.$nextTick(() => {
          this.renderIncomeBarChart()
          this.renderExpensePieChart()
        })
      } catch (e) {
        this.incomeExpense = { totalIncome: '0.00', totalExpense: '0.00', balance: '0.00', incomeItems: [], expenseItems: [] }
      }
    },
    async loadCashFlow() {
      try {
        const res = await getCashFlow({ month: this.selectedMonth })
        this.cashFlow = res.data || { accountFlows: [], incomeSourceAnalysis: [], expenseDestinationAnalysis: [] }
        this.$nextTick(() => {
          this.renderCashFlowBarChart()
        })
      } catch (e) {
        this.cashFlow = { accountFlows: [], incomeSourceAnalysis: [], expenseDestinationAnalysis: [] }
      }
    },
    handleTypeChange() {
      this.disposeAllCharts()
      this.loadData()
    },
    disposeAllCharts() {
      Object.keys(this.charts).forEach((key) => {
        this.disposeChart(key)
      })
    },
    renderBalancePieChart() {
      const dom = document.getElementById('balancePieChart')
      if (!dom) return
      this.disposeChart('balancePie')
      const chart = echarts.init(dom)
      this.charts.balancePie = chart
      const data = (this.balanceSheet.accountGroups || []).map((g) => ({
        name: g.typeName,
        value: parseFloat(g.totalBalance || 0)
      }))
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{
          type: 'pie',
          radius: '60%',
          data: data.length > 0 ? data : [{ name: '无数据', value: 0 }],
          emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } }
        }]
      })
    },
    renderIncomeBarChart() {
      const dom = document.getElementById('incomeBarChart')
      if (!dom) return
      this.disposeChart('incomeBar')
      const chart = echarts.init(dom)
      this.charts.incomeBar = chart
      const totalIncome = parseFloat(this.incomeExpense.totalIncome || 0)
      const totalExpense = parseFloat(this.incomeExpense.totalExpense || 0)
      chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: { data: ['收入', '支出'] },
        xAxis: { type: 'category', data: ['汇总'] },
        yAxis: { type: 'value' },
        series: [
          { name: '收入', type: 'bar', data: [totalIncome], itemStyle: { color: '#67c23a' } },
          { name: '支出', type: 'bar', data: [totalExpense], itemStyle: { color: '#f56c6c' } }
        ]
      })
    },
    renderExpensePieChart() {
      const dom = document.getElementById('expensePieChart')
      if (!dom) return
      this.disposeChart('expensePie')
      const chart = echarts.init(dom)
      this.charts.expensePie = chart
      const data = (this.incomeExpense.expenseItems || []).map((item) => ({
        name: item.categoryName,
        value: parseFloat(item.amount || 0)
      }))
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { orient: 'vertical', left: 'left' },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          data: data.length > 0 ? data : [{ name: '无数据', value: 0 }],
          emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } }
        }]
      })
    },
    renderCashFlowBarChart() {
      const dom = document.getElementById('cashFlowBarChart')
      if (!dom) return
      this.disposeChart('cashFlowBar')
      const chart = echarts.init(dom)
      this.charts.cashFlowBar = chart
      const accounts = this.cashFlow.accountFlows || []
      chart.setOption({
        tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
        legend: { data: ['流入', '流出', '净流量'] },
        xAxis: { type: 'category', data: accounts.map((a) => a.accountName), axisLabel: { rotate: 30 } },
        yAxis: { type: 'value' },
        series: [
          { name: '流入', type: 'bar', data: accounts.map((a) => parseFloat(a.inflow || 0)), itemStyle: { color: '#67c23a' } },
          { name: '流出', type: 'bar', data: accounts.map((a) => parseFloat(a.outflow || 0)), itemStyle: { color: '#f56c6c' } },
          { name: '净流量', type: 'bar', data: accounts.map((a) => parseFloat(a.netFlow || 0)), itemStyle: { color: '#409eff' } }
        ]
      })
    },
    disposeChart(key) {
      if (this.charts[key]) {
        this.charts[key].dispose()
        this.charts[key] = null
      }
    },
    resizeCharts() {
      Object.values(this.charts).forEach((chart) => {
        if (chart) chart.resize()
      })
    }
  }
}
</script>
