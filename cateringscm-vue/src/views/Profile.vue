<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
        </div>
      </template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="登录账号">{{ userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ userInfo.realName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ userInfo.phone }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ userInfo.sex }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ userInfo.roleName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="userInfo.status === 1 ? 'success' : 'danger'">
            {{ userInfo.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getProfile } from '@/api/user'

const userInfo = ref({})

const loadProfile = async () => {
  try {
    const res = await getProfile()
    userInfo.value = res
  } catch (error) {
    console.error('加载个人信息失败', error)
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
