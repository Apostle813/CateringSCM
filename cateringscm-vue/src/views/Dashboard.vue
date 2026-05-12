<template>
  <div class="dashboard-container">
    <!-- 第一行：4个大统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 16px;">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" body-style="padding: 20px">
          <div class="stat-icon" style="background:#e0f2fe;">
            <el-icon :size="28" color="#0284c7"><Coin /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-label">库存总资产</div>
            <div class="stat-value">¥{{ formatNum(statData.totalInventoryAsset) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" body-style="padding: 20px">
          <div class="stat-icon" style="background:#ffedd5;">
            <el-icon :size="28" color="#ea580c"><Money /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-label">本月采购支出</div>
            <div class="stat-value">¥{{ formatNum(statData.monthPurchaseAmount) }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" body-style="padding: 20px">
          <div class="stat-icon" style="background:#fce7f3;">
            <el-icon :size="28" color="#db2777"><Document /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-label">待审批采购单</div>
            <div class="stat-value">{{ statData.pendingPurchaseCount ?? 0 }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card" body-style="padding: 20px">
          <div class="stat-icon" style="background:#dcfce7;">
            <el-icon :size="28" color="#16a34a"><ShoppingCart /></el-icon>
          </div>
          <div class="stat-body">
            <div class="stat-label">待审批请购单</div>
            <div class="stat-value">{{ statData.pendingRequisitionCount ?? 0 }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第二行：4个小统计卡片 -->
    <el-row :gutter="16" style="margin-bottom: 16px;">
      <el-col :span="3" v-for="item in miniStats" :key="item.label">
        <el-card shadow="hover" class="mini-card" body-style="padding: 12px; text-align: center;">
          <div class="mini-value" :style="{ color: item.color }">{{ item.value }}</div>
          <div class="mini-label">{{ item.label }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第三行：图表区 -->
    <el-row :gutter="16" style="margin-bottom: 16px;">
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span class="card-title">各仓库资产占比</span></template>
          <div ref="pieChartRef" style="height: 280px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span class="card-title">本周出入库流水</span></template>
          <div ref="barChartRef" style="height: 280px;"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header><span class="card-title">近6月采购趋势</span></template>
          <div ref="lineChartRef" style="height: 280px;"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 第四行：告警 -->
    <el-row>
      <el-col :span="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span style="font-weight: bold; color: #F56C6C;">
                <el-icon><Warning /></el-icon> 库存告警
              </span>
              <span style="margin-left: 24px; font-size: 13px; color: #909399;">
                告警阈值：
              </span>
              <el-input-number
                v-model="alertThreshold"
                :min="1"
                :max="999"
                size="small"
                style="width: 100px;"
              />
            </div>
          </template>
          <el-table :data="alertList" border stripe v-loading="loading">
            <el-table-column prop="materialName" label="食材名称" min-width="150" />
            <el-table-column prop="warehouseName" label="所在仓库" min-width="150" />
            <el-table-column prop="currentQty" label="当前余量" width="150" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.currentQty <= alertThreshold / 2 ? 'danger' : 'warning'" size="large" effect="dark">
                  {{ scope.row.currentQty }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { Warning, Coin, Money, Document, ShoppingCart } from '@element-plus/icons-vue'
import { getDashboardStat } from '@/api/dashboard'
import * as echarts from 'echarts'

// DOM refs
const pieChartRef = ref(null)
const barChartRef = ref(null)
const lineChartRef = ref(null)

let pieChart = null
let barChart = null
let lineChart = null

const loading = ref(false)
const alertThreshold = ref(20)

const statData = reactive({
  totalInventoryAsset: 0,
  monthPurchaseAmount: 0,
  pendingPurchaseCount: 0,
  pendingRequisitionCount: 0,
  monthInboundQty: 0,
  monthOutboundQty: 0,
  totalSupplier: 0,
  totalMaterial: 0,
  totalStore: 0,
  lowStockList: [],
  warehouseAssets: [],
  weeklyStockMovement: [],
  purchaseMonthlyTrend: []
})

// 小统计卡片配置
const miniStats = computed(() => [
  { label: '供应商', value: statData.totalSupplier ?? 0, color: '#0284c7' },
  { label: '食材总数', value: statData.totalMaterial ?? 0, color: '#7c3aed' },
  { label: '本月入库', value: statData.monthInboundQty ?? 0, color: '#16a34a' },
  { label: '本月出库', value: statData.monthOutboundQty ?? 0, color: '#ea580c' }
])

// 根据阈值过滤告警列表
const alertList = computed(() => {
  return (statData.lowStockList || []).filter(item => item.currentQty < alertThreshold.value)
})

const formatNum = (val) => {
  if (val == null) return '0.00'
  return Number(val).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

// 获取后端数据
const fetchDashboardData = async () => {
  try {
    const res = await getDashboardStat()
    if (res) {
      Object.assign(statData, res)
    }
  } catch (error) {
    console.error('获取看板数据失败', error)
  }
}

// 补全周度数据：MySQL %w 格式 0=周日 -> 映射到 [周日..周六]
const fillWeeklyData = (rawList) => {
  const dayNames = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  const map = {}
  if (rawList) {
    rawList.forEach(item => {
      map[String(item.dayOfWeek)] = item
    })
  }
  const result = []
  for (let i = 0; i < 7; i++) {
    const key = String(i)
    if (map[key]) {
      result.push({
        day: dayNames[i],
        inbound: Number(map[key].inbound || 0),
        outbound: Number(map[key].outbound || 0)
      })
    } else {
      result.push({ day: dayNames[i], inbound: 0, outbound: 0 })
    }
  }
  return result
}

// 初始化饼图（仓库资产）
const initPieChart = () => {
  pieChart = echarts.init(pieChartRef.value)
  const assets = statData.warehouseAssets || []
  const data = assets.length > 0
    ? assets.map(a => ({ value: Number(a.value || 0), name: a.name }))
    : [{ value: 1, name: '暂无数据' }]

  pieChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 元 ({d}%)' },
    legend: { bottom: '0%', textStyle: { fontSize: 12 } },
    series: [{
      name: '资产占比',
      type: 'pie',
      radius: ['35%', '65%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data
    }]
  })
}

// 初始化柱状图（周度出入库）
const initBarChart = () => {
  barChart = echarts.init(barChartRef.value)
  const weeklyData = fillWeeklyData(statData.weeklyStockMovement)
  barChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['入库', '出库'], bottom: '0%' },
    grid: { left: '8%', right: '4%', top: '8%', bottom: '22%' },
    xAxis: { type: 'category', data: weeklyData.map(d => d.day), axisLabel: { fontSize: 11 } },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '入库', type: 'bar', barWidth: '30%',
        data: weeklyData.map(d => d.inbound),
        itemStyle: { color: '#409EFF', borderRadius: [4, 4, 0, 0] }
      },
      {
        name: '出库', type: 'bar', barWidth: '30%',
        data: weeklyData.map(d => d.outbound),
        itemStyle: { color: '#F56C6C', borderRadius: [4, 4, 0, 0] }
      }
    ]
  })
}

// 初始化折线图（近6月采购趋势）
const initLineChart = () => {
  lineChart = echarts.init(lineChartRef.value)
  const trend = statData.purchaseMonthlyTrend || []
  const months = trend.map(t => t.month)
  const amounts = trend.map(t => Number(t.amount || 0))
  lineChart.setOption({
    tooltip: { trigger: 'axis', formatter: '{b}<br/>采购额: ¥{c}' },
    grid: { left: '10%', right: '4%', top: '8%', bottom: '12%' },
    xAxis: { type: 'category', data: months.length ? months : ['暂无'], axisLabel: { fontSize: 11 } },
    yAxis: { type: 'value', axisLabel: { formatter: '¥{value}' } },
    series: [{
      data: amounts.length ? amounts : [0],
      type: 'line',
      smooth: true,
      lineStyle: { color: '#67C23A', width: 3 },
      itemStyle: { color: '#67C23A' },
      areaStyle: { color: 'rgba(103,194,58,0.15)' }
    }]
  })
}

// 全部图表初始化
const initAllCharts = () => {
  initPieChart()
  initBarChart()
  initLineChart()
}

// 窗口缩放自适应
const handleResize = () => {
  pieChart?.resize()
  barChart?.resize()
  lineChart?.resize()
}

onMounted(async () => {
  await fetchDashboardData()
  setTimeout(() => {
    initAllCharts()
    window.addEventListener('resize', handleResize)
  }, 100)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  barChart?.dispose()
  lineChart?.dispose()
})
</script>

<style scoped>
.dashboard-container { padding: 12px; background-color: #f0f2f5; min-height: calc(100vh - 84px); }

/* 大卡片 */
.stat-card { border-radius: 8px; }
.stat-card :deep(.el-card__body) { display: flex; align-items: center; gap: 16px; }
.stat-icon {
  width: 52px; height: 52px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center; flex-shrink: 0;
}
.stat-body { flex: 1; min-width: 0; }
.stat-label { font-size: 13px; color: #909399; margin-bottom: 4px; }
.stat-value { font-size: 22px; font-weight: bold; color: #303133; }

/* 小卡片 */
.mini-card { border-radius: 8px; }
.mini-value { font-size: 20px; font-weight: bold; }
.mini-label { font-size: 12px; color: #909399; margin-top: 2px; }

.card-title { font-weight: bold; font-size: 15px; }
.card-header { display: flex; align-items: center; }
</style>
