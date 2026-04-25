<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="title">餐饮供应链管理系统</h2>
      <el-form :model="loginForm" :rules="rules" ref="loginRef">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入账号 (如: admin)" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
          登录系统
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/user'

const router = useRouter()
const loginRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
  password: [{ required: true, message: '密码不能为空', trigger: 'blur' }]
}

const handleLogin = () => {
  loginRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await login(loginForm)
        // 登录成功，将 token 存入本地
        localStorage.setItem('scm_token', res.token)
        localStorage.setItem('scm_user', JSON.stringify(res))
        ElMessage.success('登录成功')
        router.push('/') // 跳转到首页
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;
}
.login-card {
  width: 400px;
  padding: 20px;
}
.title {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}
.login-btn {
  width: 100%;
  margin-top: 10px;
}
</style>