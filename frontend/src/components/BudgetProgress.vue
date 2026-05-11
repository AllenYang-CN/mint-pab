<template>
  <el-progress
    :percentage="percentage"
    :color="progressColor"
    :status="progressStatus"
    :stroke-width="14"
  />
</template>

<script>
export default {
  name: 'BudgetProgress',
  props: {
    percentage: {
      type: Number,
      default: 0
    },
    status: {
      type: String,
      default: 'NORMAL'
    }
  },
  computed: {
    progressColor() {
      const colorMap = {
        NORMAL: '#67C23A',
        WARNING: '#E6A23C',
        OVER: '#F56C6C'
      }
      return colorMap[this.status] || '#67C23A'
    },
    progressStatus() {
      if (this.status === 'OVER') return 'exception'
      if (this.status === 'WARNING') return 'warning'
      return 'success'
    }
  }
}
</script>
