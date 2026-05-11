<template>
  <el-dialog
    :visible.sync="dialogVisible"
    :title="isEdit ? '编辑交易' : '记一笔'"
    width="500px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="form"
      :model="form"
      :rules="rules"
      label-width="90px"
      size="small"
    >
      <el-form-item label="交易类型" prop="type">
        <el-radio-group v-model="form.type" :disabled="isEdit" @change="handleTypeChange">
          <el-radio-button label="EXPENSE">支出</el-radio-button>
          <el-radio-button label="INCOME">收入</el-radio-button>
          <el-radio-button label="TRANSFER">转账</el-radio-button>
        </el-radio-group>
      </el-form-item>

      <el-form-item v-if="form.type === 'EXPENSE'" label="支出账户" prop="fromAccountId">
        <el-select v-model="form.fromAccountId" placeholder="请选择支出账户" style="width: 100%">
          <el-option
            v-for="item in activeAccounts"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item v-if="form.type === 'INCOME'" label="收入账户" prop="toAccountId">
        <el-select v-model="form.toAccountId" placeholder="请选择收入账户" style="width: 100%">
          <el-option
            v-for="item in activeAccounts"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>

      <template v-if="form.type === 'TRANSFER'">
        <el-form-item label="转出账户" prop="fromAccountId">
          <el-select v-model="form.fromAccountId" placeholder="请选择转出账户" style="width: 100%">
            <el-option
              v-for="item in activeAccounts"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="转入账户" prop="toAccountId">
          <el-select v-model="form.toAccountId" placeholder="请选择转入账户" style="width: 100%">
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
          v-model="form.amount"
          :precision="2"
          :min="0.01"
          :controls="false"
          placeholder="请输入金额"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="分类" prop="categoryId">
        <el-cascader
          v-model="form.categoryId"
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

      <el-form-item label="交易时间" prop="transactionTime">
        <el-date-picker
          v-model="form.transactionTime"
          type="datetime"
          value-format="yyyy-MM-dd HH:mm:ss"
          format="yyyy-MM-dd HH:mm"
          placeholder="选择交易时间"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="备注" prop="remark">
        <el-input
          v-model="form.remark"
          type="textarea"
          :rows="2"
          placeholder="请输入备注（可选）"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <div slot="footer">
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">
        确 定
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { getAccounts } from '@/api/account'
import { getCategoryTree } from '@/api/category'
import { createTransaction, updateTransaction } from '@/api/transaction'

export default {
  name: 'TransactionForm',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    editData: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      dialogVisible: false,
      submitting: false,
      accounts: [],
      categoryTree: [],
      form: {
        type: 'EXPENSE',
        fromAccountId: '',
        toAccountId: '',
        amount: undefined,
        categoryId: '',
        transactionTime: '',
        remark: ''
      },
      rules: {
        type: [{ required: true, message: '请选择交易类型', trigger: 'change' }],
        fromAccountId: [{ required: true, message: '请选择账户', trigger: 'change' }],
        toAccountId: [{ required: true, message: '请选择账户', trigger: 'change' }],
        amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
        categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
        transactionTime: [{ required: true, message: '请选择交易时间', trigger: 'change' }]
      }
    }
  },
  computed: {
    isEdit() {
      return !!this.editData
    },
    activeAccounts() {
      return this.accounts.filter((a) => a.status === 'ACTIVE')
    },
    categoryOptions() {
      const typeMap = {
        EXPENSE: 'EXPENSE',
        INCOME: 'INCOME',
        TRANSFER: 'EXPENSE'
      }
      const targetType = typeMap[this.form.type]
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
  watch: {
    visible(val) {
      this.dialogVisible = val
      if (val) {
        this.initData()
      }
    },
    dialogVisible(val) {
      this.$emit('update:visible', val)
    },
    editData: {
      immediate: true,
      handler(val) {
        if (val) {
          const category = this.findCategoryById(val.categoryId)
          this.form = {
            type: val.type || 'EXPENSE',
            fromAccountId: val.fromAccountId || '',
            toAccountId: val.toAccountId || '',
            amount: val.amount ? parseFloat(val.amount) : undefined,
            categoryId: category ? [category.parentId, val.categoryId] : val.categoryId || '',
            transactionTime: val.transactionTime || '',
            remark: val.remark || ''
          }
        } else {
          this.resetForm()
        }
      }
    }
  },
  methods: {
    async initData() {
      await Promise.all([this.loadAccounts(), this.loadCategories()])
      if (this.editData && this.editData.categoryId) {
        const category = this.findCategoryById(this.editData.categoryId)
        if (category) {
          this.form.categoryId = [category.parentId, this.editData.categoryId]
        }
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
      this.form.fromAccountId = ''
      this.form.toAccountId = ''
      this.form.categoryId = ''
    },
    findCategoryById(id) {
      for (const group of this.categoryTree) {
        const child = (group.children || []).find((c) => c.id === id)
        if (child) {
          return { ...child, parentId: group.id, parentName: group.name }
        }
      }
      return null
    },
    resetForm() {
      this.form = {
        type: 'EXPENSE',
        fromAccountId: '',
        toAccountId: '',
        amount: undefined,
        categoryId: '',
        transactionTime: this.formatNow(),
        remark: ''
      }
    },
    formatNow() {
      const d = new Date()
      const pad = (n) => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
    },
    handleClose() {
      this.$refs.form && this.$refs.form.resetFields()
      this.resetForm()
      this.$emit('update:editData', null)
    },
    handleSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return

        if (this.form.type === 'TRANSFER' && this.form.fromAccountId === this.form.toAccountId) {
          this.$message.error('转出账户和转入账户不能相同')
          return
        }

        const payload = {
          type: this.form.type,
          amount: String(this.form.amount.toFixed(2)),
          categoryId: Array.isArray(this.form.categoryId) ? this.form.categoryId[this.form.categoryId.length - 1] : this.form.categoryId,
          transactionTime: this.form.transactionTime,
          remark: this.form.remark
        }

        if (this.form.type === 'EXPENSE' || this.form.type === 'TRANSFER') {
          payload.fromAccountId = this.form.fromAccountId
        }
        if (this.form.type === 'INCOME' || this.form.type === 'TRANSFER') {
          payload.toAccountId = this.form.toAccountId
        }

        this.submitting = true
        try {
          if (this.isEdit) {
            await updateTransaction(this.editData.id, payload)
            this.$message.success('更新成功')
          } else {
            await createTransaction(payload)
            this.$message.success('记账成功')
          }
          this.dialogVisible = false
          this.$emit('success')
        } catch (e) {
          // handled by interceptor
        } finally {
          this.submitting = false
        }
      })
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
