<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <h2>餐饮 SCM 系统</h2>
      </div>
      <el-menu
          :default-active="$route.path"
          router
          class="menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>首页大屏看板</span>
        </el-menu-item>

        <el-sub-menu index="1">
          <template #title>
            <el-icon><Box /></el-icon>
            <span>基础档案</span>
          </template>
          <el-menu-item index="/material">食材档案管理</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="2">
          <template #title>
            <el-icon><ShoppingCart /></el-icon>
            <span>采购管理</span>
          </template>
          <el-menu-item index="/purchase">采购订单审批</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="3">
          <template #title>
            <el-icon><Van /></el-icon>
            <span>WMS 库存核心</span>
          </template>
          <el-menu-item index="/inventory">库存台账与发料</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="4">
          <template #title>
            <el-icon><DocumentChecked /></el-icon>
            <span>安全与审计</span>
          </template>
          <el-menu-item index="/stocklog">操作流水追溯</el-menu-item>
        </el-sub-menu>

      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="breadcrumb">当前位置：{{ $route.name }}</div>
        <div class="user-info">
          <span>欢迎，{{ currentUser }}</span>
          <el-button type="danger" link @click="handleLogout" style="margin-left: 20px;">
            <el-icon><SwitchButton /></el-icon> 退出
          </el-button>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DataLine, Box, ShoppingCart, SwitchButton,Van,DocumentChecked } from '@element-plus/icons-vue' // 引入图标

const router = useRouter()
const currentUser = ref('')

onMounted(() => {
  // 从登录时存的 localStorage 中取出用户名
  const userStr = localStorage.getItem('scm_user')
  if (userStr) {
    const user = JSON.parse(userStr)
    currentUser.value = user.username
  }
})

// 退出登录逻辑
const handleLogout = () => {
  localStorage.removeItem('scm_token')
  localStorage.removeItem('scm_user')
  ElMessage.success('已安全退出')
  router.push('/login')
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  width: 100vw;
}
.aside {
  background-color: #304156;
  color: white;
  display: flex;
  flex-direction: column;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  background-color: #2b3649;
  border-bottom: 1px solid #1f2d3d;
}
.logo h2 {
  margin: 0;
  color: white;
  font-size: 18px;
}
.menu {
  border-right: none;
  flex: 1;
}
.header {
  background-color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
}
.main-content {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>