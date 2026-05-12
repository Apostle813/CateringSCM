<template>
  <div class="app-container">
    <el-alert title="安全审计溯源：所有的库存变动都在此记录，不可篡改。" type="info" show-icon style="margin-bottom: 20px;"/>

    <div class="filter-container" style="display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 20px;">
      <el-select v-model="queryParams.type" placeholder="业务类型" style="width: 130px;" clearable>
        <el-option label="采购入库" :value="1" />
        <el-option label="领料出库" :value="2" />
        <el-option label="盘点调整" :value="3" />
      </el-select>
      <el-select v-model="queryParams.warehouseId" placeholder="变动仓库" style="width: 150px;" clearable>
        <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" />
      </el-select>
      <el-input v-model="queryParams.materialName" placeholder="物资名称" style="width: 180px;" clearable />
      <el-date-picker v-model="queryParams.startDate" type="date" placeholder="开始日期" style="margin-right: 5px;" />
      <el-date-picker v-model="queryParams.endDate" type="date" placeholder="结束日期" style="margin-right: 5px;" />
      <el-button type="primary" @click="handleQuery">查询</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border size="small" style="width: 100%">
      <el-table-column prop="id" label="流水ID" width="70" align="center" />
      <el-table-column prop="createTime" label="操作时间" width="160" />
      <el-table-column prop="referenceNo" label="关联单号" width="180" />
      <el-table-column prop="type" label="业务类型" width="120" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.type === 1" type="success" effect="dark">采购入库</el-tag>
          <el-tag v-else-if="scope.row.type === 2" type="danger" effect="dark">领料出库</el-tag>
          <el-tag v-else-if="scope.row.type === 3" type="warning" effect="dark">盘点调整</el-tag>
          <el-tag v-else type="info">系统初始化</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="warehouseName" label="变动仓库" />
      <el-table-column prop="materialName" label="变动物资" />
      <el-table-column label="变动前" width="80" align="center" prop="beforeQty" />
      <el-table-column prop="changeQty" label="变动数量" width="100" align="center">
        <template #default="scope">
    <span :style="{ color: scope.row.changeQty > 0 ? '#67C23A' : '#F56C6C', fontWeight: 'bold' }">
      {{ scope.row.changeQty > 0 ? '+' + scope.row.changeQty : scope.row.changeQty }}
    </span>
        </template>
      </el-table-column>
      <el-table-column label="变动后" width="80" align="center" prop="afterQty" />
      <el-table-column prop="operatorName" label="操作人" width="100" align="center" />
    </el-table>

    <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end;"
        @current-change="getList"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStockLogPage } from '@/api/stockLog'
import { getWarehouseList } from '@/api/warehouse'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const warehouseList = ref([])
const queryParams = reactive({ page: 1, pageSize: 15, type: null, warehouseId: null, materialName: '', startDate: '', endDate: '' })

const handleQuery = () => {
  queryParams.page = 1
  getList()
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getStockLogPage(queryParams)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}
onMounted(() => { 
  getList()
  getWarehouseList().then(res => { warehouseList.value = res }).catch(() => {})
})
</script>
<style scoped>.app-container { padding: 20px; }</style>
