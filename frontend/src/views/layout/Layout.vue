<template>
  <el-container>
    <el-aside width="220px">
      <div class="logo">
        <span>Mint PAB</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <i class="el-icon-s-home" />
          <span slot="title">首页</span>
        </el-menu-item>
        <el-menu-item index="/accounts">
          <i class="el-icon-wallet" />
          <span slot="title">账户管理</span>
        </el-menu-item>
        <el-menu-item index="/transactions">
          <i class="el-icon-document" />
          <span slot="title">流水记账</span>
        </el-menu-item>
        <el-menu-item index="/categories">
          <i class="el-icon-collection-tag" />
          <span slot="title">分类管理</span>
        </el-menu-item>
        <el-menu-item index="/budgets">
          <i class="el-icon-money" />
          <span slot="title">预算管理</span>
        </el-menu-item>
        <el-menu-item index="/reports">
          <i class="el-icon-s-data" />
          <span slot="title">财务报表</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header height="56px">
        <div class="header-title">个人记账系统</div>
        <div class="header-right">
          <span class="username">{{ username }}</span>
          <el-button type="text" icon="el-icon-switch-button" @click="handleLogout">
            退出登录
          </el-button>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import { mapState } from 'vuex'

export default {
  name: 'Layout',
  computed: {
    ...mapState('user', ['username']),
    activeMenu() {
      return this.$route.path
    }
  },
  methods: {
    async handleLogout() {
      try {
        await this.$confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await this.$store.dispatch('user/logout')
        this.$router.push('/login')
      } catch (e) {
        // cancel
      }
    }
  }
}
</script>

<style scoped>
.logo {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 18px;
  font-weight: 600;
  background-color: #2b3649;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.username {
  color: #606266;
  font-size: 14px;
}
</style>
