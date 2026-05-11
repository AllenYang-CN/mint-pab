<template>
  <div>
    <div class="page-title">账户管理</div>

    <div class="table-card">
      <div style="margin-bottom: 16px">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增账户</el-button>
      </div>

      <el-table v-loading="loading" :data="accounts" stripe>
        <el-table-column prop="name" label="账户名称" min-width="140" />
        <el-table-column prop="typeName" label="类型" width="120">
          <template slot-scope="scope">
            <el-tag :type="typeTagType(scope.row.type)" size="small">
              {{ scope.row.typeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="initialBalance" label="初始余额" width="120">
          <template slot-scope="scope">{{ formatMoney(scope.row.initialBalance) }}</template>
        </el-table-column>
        <el-table-column prop="currentBalance" label="当前余额" width="120">
          <template slot-scope="scope">
            <span :class="balanceClass(scope.row.currentBalance)">
              {{ formatMoney(scope.row.currentBalance) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
              {{ scope.row.status === 'ACTIVE' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button
              type="text"
              size="small"
              @click="handleToggleStatus(scope.row)"
            >
              {{ scope.row.status === 'ACTIVE' ? '停用' : '启用' }}
            </el-button>
            <el-button type="text" size="small" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :visible.sync="dialogVisible" :title="isEdit ? '编辑账户' : '新增账户'" width="450px">
      <el-form ref="form" :model="form" :rules="rules" label-width="90px" size="small">
        <el-form-item label="账户名称" prop="name">
          <el-input v-model="form.name" placeholder="2-30个字符" maxlength="30" />
        </el-form-item>
        <el-form-item label="账户类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%" :disabled="isEdit">
            <el-option label="现金" value="CASH" />
            <el-option label="银行储蓄卡" value="BANK_SAVINGS" />
            <el-option label="信用卡" value="CREDIT_CARD" />
            <el-option label="支付宝" value="ALIPAY" />
            <el-option label="微信" value="WECHAT" />
          </el-select>
        </el-form-item>
        <el-form-item label="初始余额" prop="initialBalance">
          <el-input-number
            v-model="form.initialBalance"
            :precision="2"
            :controls="false"
            placeholder="请输入初始余额"
            style="width: 100%"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAccounts, createAccount, updateAccount, deleteAccount, updateAccountStatus } from '@/api/account'
import { formatMoney } from '@/utils/format'

export default {
  name: 'Account',
  data() {
    return {
      loading: false,
      accounts: [],
      dialogVisible: false,
      isEdit: false,
      submitting: false,
      currentId: null,
      form: {
        name: '',
        type: '',
        initialBalance: 0,
        remark: ''
      },
      rules: {
        name: [
          { required: true, message: '请输入账户名称', trigger: 'blur' },
          { min: 2, max: 30, message: '长度在 2 到 30 个字符', trigger: 'blur' }
        ],
        type: [{ required: true, message: '请选择账户类型', trigger: 'change' }],
        initialBalance: [{ required: true, message: '请输入初始余额', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadAccounts()
  },
  methods: {
    formatMoney,
    async loadAccounts() {
      this.loading = true
      try {
        const res = await getAccounts()
        this.accounts = res.data || []
      } catch (e) {
        this.accounts = []
      } finally {
        this.loading = false
      }
    },
    typeTagType(type) {
      const map = {
        CASH: 'warning',
        BANK_SAVINGS: 'primary',
        CREDIT_CARD: 'danger',
        ALIPAY: 'success',
        WECHAT: 'success'
      }
      return map[type] || ''
    },
    balanceClass(balance) {
      const num = parseFloat(balance)
      if (num < 0) return 'expense-text'
      return ''
    },
    handleAdd() {
      this.isEdit = false
      this.currentId = null
      this.form = { name: '', type: '', initialBalance: 0, remark: '' }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.form && this.$refs.form.resetFields()
      })
    },
    handleEdit(row) {
      this.isEdit = true
      this.currentId = row.id
      this.form = {
        name: row.name,
        type: row.type,
        initialBalance: parseFloat(row.initialBalance) || 0,
        remark: row.remark || ''
      }
      this.dialogVisible = true
    },
    handleSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        const payload = {
          name: this.form.name,
          remark: this.form.remark
        }
        if (!this.isEdit) {
          payload.type = this.form.type
          payload.initialBalance = String(this.form.initialBalance.toFixed(2))
        }
        this.submitting = true
        try {
          if (this.isEdit) {
            await updateAccount(this.currentId, payload)
            this.$message.success('更新成功')
          } else {
            await createAccount(payload)
            this.$message.success('创建成功')
          }
          this.dialogVisible = false
          this.loadAccounts()
        } catch (e) {
          // ignore
        } finally {
          this.submitting = false
        }
      })
    },
    async handleToggleStatus(row) {
      const newStatus = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
      const actionText = newStatus === 'ACTIVE' ? '启用' : '停用'
      try {
        await this.$confirm(`确定要${actionText}该账户吗？`, '提示', { type: 'warning' })
        await updateAccountStatus(row.id, { status: newStatus })
        this.$message.success(`${actionText}成功`)
        this.loadAccounts()
      } catch (e) {
        // cancel or error
      }
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该账户吗？删除后不可恢复', '提示', { type: 'warning' })
        await deleteAccount(row.id)
        this.$message.success('删除成功')
        this.loadAccounts()
      } catch (e) {
        // cancel or error
      }
    }
  }
}
</script>
