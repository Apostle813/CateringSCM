<template>
  <el-container class="layout-container">
    <el-aside width="220px">
      <div class="logo">
        <el-icon :size="24" color="#409EFF"><Box /></el-icon>
        <span>餐饮供应链系统</span>
      </div>

      <el-menu
          :default-active="$route.path"
          router
          class="el-menu-vertical"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页数据看板</span>
        </el-menu-item>

        <el-sub-menu v-if="userRole === 'ADMIN'" index="1">
          <template #title>
            <el-icon><Management /></el-icon>
            <span>基础信息管理</span>
          </template>
          <el-menu-item index="/material">食材物资管理</el-menu-item>
          <el-menu-item index="/warehouse">仓库配置管理</el-menu-item>
          <el-menu-item index="/supplier">供应商管理</el-menu-item>
          <el-menu-item index="/store">门店信息管理</el-menu-item>
          <el-menu-item index="/sysuser">系统用户配置</el-menu-item>
        </el-sub-menu>

        <el-sub-menu v-if="userRole === 'ADMIN' || userRole === 'PURCHASER' || userRole === 'WAREHOUSE'" index="3">
          <template #title>
            <el-icon><ShoppingCart /></el-icon>
            <span>采购与请购业务</span>
          </template>
          <el-menu-item index="/purchase">采购订单审批</el-menu-item>
          <el-menu-item index="/requisition">门店请购发货</el-menu-item>
        </el-sub-menu>

        <el-menu-item v-if="userRole === 'ADMIN' || userRole === 'WAREHOUSE'" index="/inventory">
          <el-icon><Box /></el-icon>
          <span>实时库存管理</span>
        </el-menu-item>

        <el-sub-menu v-if="userRole === 'ADMIN' || userRole === 'WAREHOUSE'" index="2">
          <template #title>
            <el-icon><List /></el-icon>
            <span>业务流水查询</span>
          </template>
          <el-menu-item index="/stockLog">库存变动流水</el-menu-item>
          <el-menu-item index="/operationLog">操作日志审计</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header>
        <div class="header-left">
          <span class="breadcrumb">当前页面：{{ $route.meta.title || '系统管理' }}</span>
        </div>
        <div class="header-right">
          <el-tag type="success" effect="plain" style="margin-right: 15px;">
            当前角色：{{ roleName }}
          </el-tag>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              欢迎您，{{ userInfo.realName || '系统用户' }} <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  HomeFilled,
  Management,
  ShoppingCart,
  Box,
  List,
  ArrowDown
} from '@element-plus/icons-vue'
import { getProfile } from '@/api/user'

const router = useRouter()

// 从本地缓存读取角色，默认为 ADMIN 以便开发调试
const userRole = ref(localStorage.getItem('userRole') || 'ADMIN')

// 用户信息
const userInfo = ref({})

// 计算角色中文名称，展示在顶栏
const roleName = computed(() => {
  const roles = {
    'ADMIN': '系统管理员',
    'PURCHASER': '采购员',
    'WAREHOUSE': '库管员'
  }
  return roles[userRole.value] || '普通用户'
})

// 加载用户信息
const loadProfile = async () => {
  try {
    const res = await getProfile()
    userInfo.value = res
    // 缓存角色信息
    if (res.roleCode) {
      localStorage.setItem('userRole', res.roleCode)
      userRole.value = res.roleCode
    }
  } catch (error) {
    console.error('加载用户信息失败', error)
  }
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'logout') {
    localStorage.clear()
    window.location.href = '/login'  // 强制跳转并刷新页面
  } else if (command === 'profile') {
    router.push('/profile')
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.layout-container { height: 100vh; }
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: bold;
  background-color: #2b2f3a;
}
.logo span { margin-left: 10px; }
.el-aside { background-color: #304156; transition: width 0.3s; }
.el-header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6e6e6;
  height: 60px;
}
.user-info { cursor: pointer; color: #409EFF; font-weight: 500; }
.breadcrumb { color: #606266; font-size: 14px; }
.el-menu { border-right: none; }
</style>
