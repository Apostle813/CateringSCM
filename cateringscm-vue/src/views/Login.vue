<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="title">餐饮供应链管理系统</h2>
      <el-form :model="loginForm" :rules="rules" ref="loginRef">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入账号" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" show-password @keyup.enter="handleLogin" />
        </el-form-item>
        <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">
          登录系统
        </el-button>
      </el-form>
      <div class="forgot-link">
        <el-button type="primary" link size="small" @click="showForgotDialog">忘记密码？</el-button>
      </div>
    </el-card>

    <!-- 忘记密码弹窗 — 两步流程 -->
    <el-dialog title="忘记密码" v-model="forgotVisible" width="440px" :close-on-click-modal="false" @closed="onForgotClosed">
      <!-- 步骤 1：输入账号 -->
      <el-form v-if="forgotStep === 1" :model="forgotForm" label-width="80px">
        <el-form-item label="登录账号">
          <el-input v-model="forgotForm.username" placeholder="请输入您的登录账号" clearable @keyup.enter="handleCheckUsername" />
        </el-form-item>
      </el-form>
      <div v-if="forgotStep === 1 && forgotResult !== null" style="margin-top: 12px;">
        <el-alert v-if="forgotResult" title="账号验证通过" type="success" :closable="false" show-icon>
          <p style="margin:4px 0 0;">请继续第二步，验证您的注册手机号。</p>
        </el-alert>
        <el-alert v-else title="账号不存在" type="warning" :closable="false" show-icon>
          <p style="margin:4px 0 0;">未找到该账号，请检查输入是否正确。</p>
        </el-alert>
      </div>

      <!-- 步骤 2：手机验证 + 设置新密码 -->
      <el-form v-if="forgotStep === 2" :model="forgotForm" :rules="pwdRules" ref="pwdFormRef" label-width="100px">
        <el-form-item label="当前账号">
          <el-tag>{{ forgotForm.username }}</el-tag>
        </el-form-item>
        <el-form-item label="注册手机号" prop="phone">
          <el-input v-model="forgotForm.phone" placeholder="请输入您注册时填写的手机号" clearable />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="forgotForm.newPassword" type="password" show-password placeholder="设置新密码（至少3位）" @keyup.enter="handleResetPassword" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="forgotForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" @keyup.enter="handleResetPassword" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="forgotVisible = false">关闭</el-button>
        <el-button v-if="forgotStep === 1 && forgotResult" type="primary" @click="forgotStep = 2">下一步</el-button>
        <el-button v-if="forgotStep === 1" type="primary" @click="handleCheckUsername" :loading="checking">查询</el-button>
        <el-button v-if="forgotStep === 2" @click="forgotStep = 1">上一步</el-button>
        <el-button v-if="forgotStep === 2" type="primary" @click="handleResetPassword" :loading="resetting">重置密码</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/user'
import request from '@/utils/request'

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
        localStorage.setItem('scm_token', res.token)
        localStorage.setItem('scm_user', JSON.stringify(res))
        localStorage.setItem('userRole', res.roleCode)
        ElMessage.success('登录成功')
        await router.push('/')
      } finally {
        loading.value = false
      }
    }
  })
}

// ========== 忘记密码 — 两步流程 ==========
const forgotVisible = ref(false)
const forgotStep = ref(1)       // 1=输入账号, 2=手机验证+设密码
const checking = ref(false)
const resetting = ref(false)
const forgotResult = ref(null)  // null=未查询, true=账号存在, false=不存在
const pwdFormRef = ref(null)

const forgotForm = reactive({
  username: '',
  phone: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 3, message: '密码至少3位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== forgotForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const showForgotDialog = () => {
  forgotForm.username = ''
  forgotForm.phone = ''
  forgotForm.newPassword = ''
  forgotForm.confirmPassword = ''
  forgotResult.value = null
  forgotStep.value = 1
  forgotVisible.value = true
}

const onForgotClosed = () => {
  // 弹窗关闭时重置状态
  forgotForm.username = ''
  forgotForm.phone = ''
  forgotForm.newPassword = ''
  forgotForm.confirmPassword = ''
  forgotResult.value = null
  forgotStep.value = 1
}

const handleCheckUsername = async () => {
  if (!forgotForm.username.trim()) {
    ElMessage.warning('请输入账号')
    return
  }
  checking.value = true
  try {
    const res = await request({ url: '/user/exists', method: 'get', params: { username: forgotForm.username.trim() } })
    forgotResult.value = res
    if (!res) {
      ElMessage.warning('账号不存在')
    }
  } catch (e) {
    forgotResult.value = false
    ElMessage.error('查询失败')
  } finally {
    checking.value = false
  }
}

const handleResetPassword = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    resetting.value = true
    try {
      await request({
        url: '/user/reset-password',
        method: 'put',
        data: {
          username: forgotForm.username.trim(),
          phone: forgotForm.phone.trim(),
          newPassword: forgotForm.newPassword
        }
      })
      ElMessage.success('密码重置成功，请返回登录')
      forgotVisible.value = false
    } catch (e) {
      // 错误信息由拦截器统一处理
    } finally {
      resetting.value = false
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
.forgot-link {
  text-align: right;
  margin-top: 8px;
}
</style>
