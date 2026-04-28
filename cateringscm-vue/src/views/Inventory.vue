<template>
  <div class="app-container">
    <div class="filter-container">
      <el-button type="primary" icon="Refresh" @click="getList">刷新库存</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe style="margin-top: 20px;">
      <el-table-column prop="id" label="库存ID" width="80" align="center" />
      <el-table-column prop="warehouseName" label="所在仓库" />
      <el-table-column prop="materialName" label="物资名称" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="quantity" label="当前库存" width="120">
        <template #default="scope">
          <span style="font-size: 16px; font-weight: bold;" :style="{ color: scope.row.quantity < 20 ? '#F56C6C' : '#67C23A' }">
            {{ scope.row.quantity }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="unit" label="单位" width="80" align="center" />

      <el-table-column label="库管操作" width="220" align="center">
        <template #default="scope">
          <el-button size="small" type="warning" @click="openOutbound(scope.row)">领料出库</el-button>
          <el-button size="small" type="danger" plain @click="openAdjust(scope.row)">盘点调整</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end;"
        @current-change="getList"
    />

    <el-dialog title="物资领料出库" v-model="outboundVisible" width="400px">
      <el-form :model="outboundForm" label-width="100px">
        <el-form-item label="单据编号">
          <el-input v-model="outboundForm.referenceNo" placeholder="如: OUT-20260405-01" />
        </el-form-item>
        <el-form-item label="出库数量">
          <el-input-number v-model="outboundForm.outQty" :min="1" />
        </el-form-item>
        <div style="margin-left: 100px; color: #909399; font-size: 12px;">当前最多可出库: {{ currentSelected.quantity }}</div>
      </el-form>
      <template #footer>
        <el-button @click="outboundVisible = false">取消</el-button>
        <el-button type="primary" @click="submitOutbound">确认出库</el-button>
      </template>
    </el-dialog>

    <el-dialog title="实地盘点调整" v-model="adjustVisible" width="400px">
      <el-form :model="adjustForm" label-width="100px">
        <el-form-item label="单据编号">
          <el-input v-model="adjustForm.referenceNo" placeholder="如: ADJ-20260405-01" />
        </el-form-item>
        <el-form-item label="实际查点数">
          <el-input-number v-model="adjustForm.realQty" :min="0" />
        </el-form-item>
        <div style="margin-left: 100px; color: #E6A23C; font-size: 12px;">系统账面数: {{ currentSelected.quantity }}，请填入实际清点数</div>
      </el-form>
      <template #footer>
        <el-button @click="adjustVisible = false">取消</el-button>
        <el-button type="danger" @click="submitAdjust">确认调整</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getInventoryPage, outboundInventory, adjustInventory } from '@/api/inventory'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10 })
// 弹窗控制
const outboundVisible = ref(false)
const adjustVisible = ref(false)
const currentSelected = ref({})

const outboundForm = reactive({ warehouseId: null, materialId: null, outQty: 1, referenceNo: '' })
const adjustForm = reactive({ warehouseId: null, materialId: null, realQty: 0, reason: '' })

const getList = async () => {
  loading.value = true
  try {
    const res = await getInventoryPage(queryParams)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const openOutbound = (row) => {
  currentSelected.value = row
  outboundForm.warehouseId = row.warehouseId
  outboundForm.materialId = row.materialId
  outboundForm.outQty = 1 // <--- 这里改为 outQty
  outboundForm.referenceNo = 'OUT' + Date.now()
  outboundVisible.value = true
}

const openAdjust = (row) => {
  currentSelected.value = row
  adjustForm.warehouseId = row.warehouseId
  adjustForm.materialId = row.materialId
  adjustForm.realQty = row.realQty
  adjustForm.referenceNo = 'ADJ' + Date.now()
  adjustVisible.value = true
}

const submitOutbound = async () => {
  try {
    // 发起出库请求
    await outboundInventory(outboundForm)
    ElMessage.success('出库成功！')
    outboundVisible.value = false // 只有成功才会关闭弹窗
    getList() // 刷新表格
  } catch (error) {
    // 如果报错，捕获异常，防止破坏 Vue 路由！
    console.error('出库操作失败:', error)
  }
}

const submitAdjust = async () => {
  try {
    await adjustInventory(adjustForm)
    ElMessage.success('盘点调整完成！')
    adjustVisible.value = false
    getList()
  } catch (error) {
    console.error('盘点操作失败:', error)
  }
}

onMounted(() => { getList() })
</script>
<style scoped>.app-container { padding: 20px; }</style>