<template>
  <div class="dashboard-container">
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover" class="stat-card asset-card">
          <div class="title">当前库存总资产估算</div>
          <div class="value">¥ {{ statData.totalInventoryAsset || '0.00' }}</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="stat-card expense-card">
          <div class="title">本月已入库采购总支出</div>
          <div class="value">¥ {{ statData.monthlyPurchaseAmount || '0.00' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="hover" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span style="font-weight: bold; color: #F56C6C;">
            <el-icon><Warning /></el-icon> 库存告警 (库存量不足 20)
          </span>
        </div>
      </template>
      <el-table :data="statData.lowStockList" border stripe>
        <el-table-column prop="materialName" label="食材名称" />
        <el-table-column prop="warehouseName" label="所在仓库" />
        <el-table-column prop="quantity" label="当前余量" width="120">
          <template #default="scope">
            <el-tag type="danger" effect="dark">{{ scope.row.quantity }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Warning } from '@element-plus/icons-vue'
import { getDashboardStat } from '@/api/dashboard'

const statData = ref({
  totalInventoryAsset: 0,
  monthlyPurchaseAmount: 0,
  lowStockList: []
})

const fetchDashboardData = async () => {
  try {
    const res = await getDashboardStat()
    if(res) {
      statData.value = res
    }
  } catch (error) {
    console.error("获取看板数据失败", error)
  }
}

onMounted(() => {
  fetchDashboardData()
})
</script>

<style scoped>
.dashboard-container { padding: 10px; }
.stat-card { text-align: center; padding: 20px 0; border-radius: 8px; }
.asset-card { background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%); }
.expense-card { background: linear-gradient(135deg, #ffedd5 0%, #fed7aa 100%); }
.title { font-size: 16px; color: #606266; margin-bottom: 10px; }
.value { font-size: 32px; font-weight: bold; color: #303133; }
</style>