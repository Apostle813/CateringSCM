<template>
  <div class="dashboard-container">
    <el-row :gutter="20" style="margin-bottom: 20px;">
      <el-col :span="12">
        <el-card shadow="hover" class="stat-card asset-card">
          <div class="title">当前库存总资产估算</div>
          <div class="value">¥ {{ statData.totalInventoryAsset || '0.00' }}</div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover" class="stat-card expense-card">
          <div class="title">本月已入库采购总支出</div>
          <div class="value">¥ {{ statData.monthPurchaseAmount || '0.00' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>各仓库资产占比</span>
            </div>
          </template>
          <div ref="pieChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>

      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span>本周物资流转监控</span>
            </div>
          </template>
          <div ref="barChartRef" style="height: 300px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row style="margin-top: 20px;">
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span style="font-weight: bold; color: #F56C6C;">
                <el-icon><Warning /></el-icon> 库存告警 (全局总库存量不足 20)
              </span>
            </div>
          </template>
          <el-table :data="statData.lowStockList" border stripe>
            <el-table-column prop="materialName" label="食材名称" />
            <el-table-column prop="warehouseName" label="所在仓库"/>
            <el-table-column prop="currentQty" label="当前总余量" width="150" align="center">
              <template #default="scope">
            <span style="color: #F56C6C; font-weight: bold; font-size: 16px;">
              {{ scope.row.currentQty }}
            </span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Warning } from '@element-plus/icons-vue'
import { getDashboardStat } from '@/api/dashboard'
import * as echarts from 'echarts'

// 图表实例引用
const pieChartRef = ref(null)
const barChartRef = ref(null)

// 响应式数据绑定 (与后端 DashboardVO 字段绝对一致)
const statData = ref({
  totalInventoryAsset: 0,
  monthPurchaseAmount: 0,
  lowStockList: []
})

// 1. 获取后端真实数据的方法
const fetchDashboardData = async () => {
  try {
    const res = await getDashboardStat()
    if (res) {
      statData.value = res // 将后端数据赋给前端模板
    }
  } catch (error) {
    console.error("获取看板数据失败", error)
  }
}

// 2. 初始化 ECharts 静态图表的方法
const initCharts = () => {
  const pieChart = echarts.init(pieChartRef.value)
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { top: '5%', left: 'center' },
    series: [
      {
        name: '资产占比',
        type: 'pie',
        radius: ['40%', '70%'],
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        data: [
          { value: 45800, name: '主仓库(常温)' },
          { value: 72000, name: '冷冻库(-18℃)' },
          { value: 12500, name: '生鲜保鲜库' }
        ]
      }
    ]
  })

  const barChart = echarts.init(barChartRef.value)
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'] },
    yAxis: { type: 'value' },
    series: [
      {
        data: [120, 200, 150, 80, 70, 110, 130],
        type: 'bar',
        itemStyle: { color: '#409EFF', borderRadius: [5, 5, 0, 0] }
      }
    ]
  })

  window.addEventListener('resize', () => {
    pieChart.resize()
    barChart.resize()
  })
}

// 页面加载完毕后，同时触发获取数据和渲染图表
onMounted(() => {
  fetchDashboardData()
  // 注意：需要加一个很小的延迟，确保 DOM 完全渲染后再画图表
  setTimeout(() => {
    initCharts()
  }, 100)
})
</script>

<style scoped>
.dashboard-container { padding: 10px; background-color: #f0f2f5; min-height: calc(100vh - 84px); }
.stat-card { text-align: center; padding: 20px 0; border-radius: 8px; }
.asset-card { background: linear-gradient(135deg, #e0f2fe 0%, #bae6fd 100%); }
.expense-card { background: linear-gradient(135deg, #ffedd5 0%, #fed7aa 100%); }
.title { font-size: 16px; color: #606266; margin-bottom: 10px; }
.value { font-size: 32px; font-weight: bold; color: #303133; }
.card-header { font-weight: bold; font-size: 16px; }
</style>