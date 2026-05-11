<template>
  <div>
    <div class="page-title">分类管理</div>

    <div class="table-card">
      <div style="margin-bottom: 16px">
        <el-button type="primary" icon="el-icon-plus" @click="handleAddParent">新增一级分类</el-button>
        <el-button type="primary" icon="el-icon-plus" @click="handleAddChild">新增二级分类</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        row-key="id"
        :expand-row-keys="expandedKeys"
        @expand-change="handleExpandChange"
      >
        <el-table-column type="expand">
          <template slot-scope="scope">
            <el-table :data="scope.row.subItems" :show-header="false" size="small" class="nested-table">
              <el-table-column width="40" />
              <el-table-column prop="name" label="二级分类" min-width="200">
                <template slot-scope="subScope">
                  <span style="margin-left: 24px">
                    <span
                      v-if="subScope.row.color"
                      class="color-dot"
                      :style="{ backgroundColor: subScope.row.color }"
                    />
                    <i v-if="subScope.row.icon" :class="subScope.row.icon" style="margin-right: 4px" />
                    {{ subScope.row.name }}
                  </span>
                </template>
              </el-table-column>
              <el-table-column prop="typeName" label="类型" width="100" />
              <el-table-column prop="isSystem" label="预置" width="80">
                <template slot-scope="subScope">
                  <el-tag v-if="subScope.row.isSystem" type="info" size="mini">系统</el-tag>
                  <el-tag v-else type="success" size="mini">自定义</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="操作" width="150">
                <template slot-scope="subScope">
                  <el-button
                    v-if="!subScope.row.isSystem"
                    type="text"
                    size="small"
                    @click="handleEdit(subScope.row, 'child')"
                  >编辑</el-button>
                  <el-button
                    v-if="!subScope.row.isSystem"
                    type="text"
                    size="small"
                    @click="handleDelete(subScope.row)"
                  >删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="一级分类" min-width="200">
          <template slot-scope="scope">
            <span
              v-if="scope.row.color"
              class="color-dot"
              :style="{ backgroundColor: scope.row.color }"
            />
            <i v-if="scope.row.icon" :class="scope.row.icon" style="margin-right: 6px" />
            {{ scope.row.name }}
          </template>
        </el-table-column>
        <el-table-column prop="typeName" label="类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.type === 'INCOME' ? 'success' : 'danger'" size="small">
              {{ scope.row.typeName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="isSystem" label="预置" width="80">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.isSystem" type="info" size="mini">系统</el-tag>
            <el-tag v-else type="success" size="mini">自定义</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="childrenCount" label="子分类数" width="100">
          <template slot-scope="scope">{{ scope.row.subItems ? scope.row.subItems.length : 0 }} 个</template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button
              v-if="!scope.row.isSystem"
              type="text"
              size="small"
              @click="handleEdit(scope.row, 'parent')"
            >编辑</el-button>
            <el-button
              v-if="!scope.row.isSystem"
              type="text"
              size="small"
              @click="handleDelete(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog :visible.sync="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="form" :model="form" :rules="rules" label-width="100px" size="small">
        <el-form-item label="分类类型" prop="type">
          <el-radio-group v-model="form.type" :disabled="isEdit">
            <el-radio label="EXPENSE">支出</el-radio>
            <el-radio label="INCOME">收入</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.level === 'child' || (isEdit && form.parentId !== null)" label="所属一级分类" prop="parentId">
          <el-select
            v-model="form.parentId"
            placeholder="请选择一级分类"
            :disabled="isEdit"
            style="width: 100%"
          >
            <el-option
              v-for="item in parentCategoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
              <span
                v-if="item.color"
                class="color-dot"
                :style="{ backgroundColor: item.color }"
              />
              <i v-if="item.icon" :class="item.icon" style="margin-right: 4px" />
              {{ item.name }}
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="20" />
        </el-form-item>
        <el-form-item label="分类颜色" prop="color">
          <el-color-picker v-model="form.color" show-alpha :predefine="predefineColors" />
        </el-form-item>
        <el-form-item label="分类图标" prop="icon">
          <el-select v-model="form.icon" placeholder="请选择图标" style="width: 100%" clearable filterable>
            <el-option
              v-for="item in iconOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            >
              <i :class="item.value" style="margin-right: 8px" />
              <span>{{ item.label }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="排序序号" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%" />
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
import { getCategoryTree, createCategory, updateCategory, deleteCategory } from '@/api/category'

const ICON_OPTIONS = [
  { value: 'el-icon-coin', label: '硬币' },
  { value: 'el-icon-money', label: '金钱' },
  { value: 'el-icon-food', label: '食物' },
  { value: 'el-icon-truck', label: '运输' },
  { value: 'el-icon-shopping-bag-1', label: '购物袋' },
  { value: 'el-icon-house', label: '房屋' },
  { value: 'el-icon-film', label: '电影' },
  { value: 'el-icon-first-aid-kit', label: '急救箱' },
  { value: 'el-icon-reading', label: '阅读' },
  { value: 'el-icon-sort', label: '转换' },
  { value: 'el-icon-trophy', label: '奖杯' },
  { value: 'el-icon-data-line', label: '数据' },
  { value: 'el-icon-suitcase', label: '公文包' },
  { value: 'el-icon-present', label: '礼物' },
  { value: 'el-icon-takeaway-box', label: '外卖' },
  { value: 'el-icon-bowl', label: '碗' },
  { value: 'el-icon-apple', label: '苹果' },
  { value: 'el-icon-coffee', label: '咖啡' },
  { value: 'el-icon-bus', label: '公交' },
  { value: 'el-icon-car', label: '汽车' },
  { value: 'el-icon-goods', label: '商品' },
  { value: 'el-icon-box', label: '盒子' },
  { value: 'el-icon-mobile-phone', label: '手机' },
  { value: 'el-icon-office-building', label: '办公楼' },
  { value: 'el-icon-lightbulb', label: '灯泡' },
  { value: 'el-icon-key', label: '钥匙' },
  { value: 'el-icon-setting', label: '设置' },
  { value: 'el-icon-monitor', label: '显示器' },
  { value: 'el-icon-place', label: '地点' },
  { value: 'el-icon-user', label: '用户' },
  { value: 'el-icon-capsule', label: '胶囊' },
  { value: 'el-icon-stethoscope', label: '听诊器' },
  { value: 'el-icon-heartbeat', label: '心跳' },
  { value: 'el-icon-notebook-2', label: '笔记本' },
  { value: 'el-icon-school', label: '学校' },
  { value: 'el-icon-document', label: '文档' },
  { value: 'el-icon-star-off', label: '星星' },
  { value: 'el-icon-cherry', label: '樱桃' },
  { value: 'el-icon-ice-cream-round', label: '冰淇淋' },
  { value: 'el-icon-water-cup', label: '水杯' },
  { value: 'el-icon-potato-strips', label: '薯条' },
  { value: 'el-icon-lollipop', label: '棒棒糖' },
  { value: 'el-icon-moon-night', label: '月亮' },
  { value: 'el-icon-sunny', label: '太阳' },
  { value: 'el-icon-partly-cloudy', label: '多云' },
  { value: 'el-icon-umbella', label: '雨伞' }
]

export default {
  name: 'Category',
  data() {
    return {
      loading: false,
      categoryTree: [],
      expandedKeys: [],
      dialogVisible: false,
      isEdit: false,
      submitting: false,
      currentId: null,
      iconOptions: ICON_OPTIONS,
      predefineColors: [
        '#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399',
        '#00BCD4', '#E040FB', '#FF9800', '#8BC34A', '#FF5722',
        '#9C27B0', '#3F51B5', '#009688', '#795548', '#607D8B'
      ],
      form: {
        level: 'parent',
        type: 'EXPENSE',
        parentId: null,
        name: '',
        color: '#409EFF',
        icon: '',
        sortOrder: 0
      },
      rules: {
        type: [{ required: true, message: '请选择分类类型', trigger: 'change' }],
        parentId: [{ required: true, message: '请选择一级分类', trigger: 'change' }],
        name: [
          { required: true, message: '请输入分类名称', trigger: 'blur' },
          { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    dialogTitle() {
      if (this.isEdit) return '编辑分类'
      return this.form.level === 'parent' ? '新增一级分类' : '新增二级分类'
    },
    parentCategoryOptions() {
      return this.categoryTree.map(item => ({
        id: item.id,
        name: item.name,
        type: item.type,
        color: item.color,
        icon: item.icon
      })).filter(item => {
        if (this.form.type) {
          return item.type === this.form.type
        }
        return true
      })
    },
    tableData() {
      return this.categoryTree.map((item) => ({
        id: item.id,
        name: item.name,
        type: item.type,
        typeName: item.typeName,
        isSystem: item.isSystem,
        parentId: null,
        color: item.color,
        icon: item.icon,
        subItems: (item.children || []).map(child => ({
          ...child,
          parentId: item.id
        }))
      }))
    }
  },
  created() {
    this.loadCategories()
  },
  methods: {
    async loadCategories() {
      this.loading = true
      try {
        const res = await getCategoryTree()
        this.categoryTree = res.data || []
      } catch (e) {
        this.categoryTree = []
      } finally {
        this.loading = false
      }
    },
    handleExpandChange(row, expandedRows) {
      this.expandedKeys = expandedRows.map((r) => r.id)
    },
    handleAddParent() {
      this.isEdit = false
      this.currentId = null
      this.form = { level: 'parent', type: 'EXPENSE', parentId: null, name: '', color: '#409EFF', icon: '', sortOrder: 0 }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.form && this.$refs.form.resetFields()
      })
    },
    handleAddChild() {
      this.isEdit = false
      this.currentId = null
      this.form = { level: 'child', type: 'EXPENSE', parentId: null, name: '', color: '#409EFF', icon: '', sortOrder: 0 }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.form && this.$refs.form.resetFields()
      })
    },
    handleEdit(row, level) {
      this.isEdit = true
      this.currentId = row.id
      this.form = {
        level: level,
        type: row.type,
        parentId: row.parentId || null,
        name: row.name,
        color: row.color || '#409EFF',
        icon: row.icon || '',
        sortOrder: row.sortOrder || 0
      }
      this.dialogVisible = true
    },
    handleSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (!valid) return
        const isParent = this.form.level === 'parent' && !this.isEdit
        const payload = {
          name: this.form.name,
          sortOrder: this.form.sortOrder,
          color: this.form.color || null,
          icon: this.form.icon || null
        }
        if (isParent || (this.isEdit && this.form.parentId === null)) {
          // 一级分类
          payload.parentId = null
        } else {
          // 二级分类
          payload.parentId = this.form.parentId
        }
        if (!this.isEdit) {
          payload.type = this.form.type
        }
        this.submitting = true
        try {
          if (this.isEdit) {
            await updateCategory(this.currentId, payload)
            this.$message.success('更新成功')
          } else {
            await createCategory(payload)
            this.$message.success('创建成功')
          }
          this.dialogVisible = false
          this.loadCategories()
        } catch (e) {
          // ignore
        } finally {
          this.submitting = false
        }
      })
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该分类吗？', '提示', { type: 'warning' })
        await deleteCategory(row.id)
        this.$message.success('删除成功')
        this.loadCategories()
      } catch (e) {
        // cancel or error
      }
    }
  }
}
</script>

<style scoped>
.nested-table {
  margin-left: 40px;
  width: calc(100% - 40px);
}

.nested-table ::v-deep .el-table__header-wrapper {
  display: none;
}

.color-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  margin-right: 6px;
  vertical-align: middle;
}
</style>
