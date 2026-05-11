<template>
  <div class="login-page">
    <div class="login-box">
      <div class="login-title">Mint PAB</div>
      <div class="login-subtitle">个人记账系统</div>
      <el-form
        ref="loginForm"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        @keyup.enter.native="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="loginForm.username"
            prefix-icon="el-icon-user"
            placeholder="用户名"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            prefix-icon="el-icon-lock"
            type="password"
            placeholder="密码"
            show-password
            clearable
          />
        </el-form-item>
        <el-button
          type="primary"
          :loading="loading"
          class="login-btn"
          @click="handleLogin"
        >
          登 录
        </el-button>
      </el-form>
    </div>
  </div>
</template>

<script>
export default {
  name: 'Login',
  data() {
    return {
      loading: false,
      loginForm: {
        username: '',
        password: ''
      },
      loginRules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (!valid) return
        this.loading = true
        try {
          await this.$store.dispatch('user/login', this.loginForm)
          this.$message.success('登录成功')
          this.$router.push('/dashboard')
        } catch (e) {
          // handled by interceptor
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.login-title {
  font-size: 28px;
  font-weight: bold;
  color: #409eff;
  text-align: center;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 16px;
  color: #909399;
  text-align: center;
  margin-bottom: 32px;
}

.login-form {
  margin-top: 20px;
}

.login-btn {
  width: 100%;
  margin-top: 10px;
  height: 40px;
  font-size: 16px;
}
</style>
