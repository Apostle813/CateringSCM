<template>
  <div class="app-container">
    <el-alert title="安全审计溯源：所有的库存变动都在此记录，不可篡改。" type="info" show-icon style="margin-bottom: 20px;"/>

    <el-table :data="tableData" v-loading="loading" border size="small" style="width: 100%">
      <el-table-column prop="id" label="流水ID" width="70" align="center" />
      <el-table-column prop="createTime" label="操作时间" width="160" />
      <el-table-column prop="referenceNo" label="关联单号" width="180" />
      <el-table-column prop="type" label="业务类型" width="100" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.type === 1" type="success">采购入库</el-tag>
          <el-tag v-else-if="scope.row.type === 2" type="warning">领料出库</el-tag>
          <el-tag v-else-if="scope.row.type === 3" type="danger">盘点调整</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="warehouseName" label="变动仓库" />
      <el-table-column prop="materialName" label="变动物资" />
      <el-table-column label="变动前" width="80" align="center" prop="beforeQty" />
      <el-table-column label="差值" width="80" align="center">
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

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 15 })

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
onMounted(() => { getList() })
</script>
<style scoped>.app-container { padding: 20px; }</style>