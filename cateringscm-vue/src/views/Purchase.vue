<template>
  <div class="app-container">
    <div class="filter-container">
      <el-button type="success" icon="Plus" @click="openQuickPurchase">发起快速直采</el-button>
      <el-button type="primary" icon="Refresh" @click="getList">刷新列表</el-button>
    </div>
    <el-dialog v-model="quickVisible" title="发起采购单" width="500px">
      <el-form :model="quickForm" label-width="100px">
        <el-form-item label="供应商">
          <el-select v-model="quickForm.supplierId" placeholder="请选择供应商" style="width: 100%">
            <el-option v-for="item in supplierOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="入库仓库">
          <el-select v-model="quickForm.warehouseId" placeholder="请选择仓库" style="width: 100%">
            <el-option v-for="item in warehouseOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="采购食材">
          <el-select v-model="quickForm.materialId" placeholder="请选择食材" style="width: 100%" filterable @change="handleMaterialChange">
            <el-option v-for="item in materialOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="采购数量">
          <el-input-number v-model="quickForm.planQty" :min="1" />
        </el-form-item>

        <el-form-item label="采购单价">
          <el-input-number v-model="quickForm.price" :precision="2" :step="0.5" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="quickVisible = false">取消</el-button>
        <el-button type="primary" @click="submitQuick">确认提交</el-button>
      </template>
    </el-dialog>

    <el-table :data="tableData" v-loading="loading" border stripe style="margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column prop="orderNo" label="单号" width="160" />
      <el-table-column prop="totalAmount" label="总金额" width="120">
        <template #default="scope">
          <span style="color: #67C23A; font-weight: bold;">¥{{ scope.row.totalAmount }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注说明" show-overflow-tooltip />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag v-if="scope.row.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="scope.row.status === 1" type="success">待入库</el-tag>
          <el-tag v-else-if="scope.row.status === 2" type="success">已入库</el-tag>
          <el-tag v-else-if="scope.row.status === 9" type="danger">已驳回</el-tag>
          <el-tag v-else type="info">未知</el-tag>
        </template>
      </el-table-column>



      <el-table-column label="操作(库管权限)" width="200" align="center">
        <template #default="scope">
          <template v-if="scope.row.status === 0">
            <template v-if="userRole === 'ADMIN' || userRole === 'WAREHOUSE'">
              <el-button type="primary" size="small" @click="handleAudit(scope.row.id)">审核通过</el-button>
              <el-button size="small" type="danger" @click="handleReject(scope.row.id)">驳回</el-button>
            </template>
            <span v-else style="color: #E6A23C; font-size: 13px;">等待审核</span>
          </template>
          <span v-else-if="scope.row.status > 1" style="color: #909399; font-size: 13px;">流程已结束</span>
          <span v-else style="color: #E6A23C; font-size: 13px;">待入库</span>
        </template>
      </el-table-column>
      <el-table-column label="支付状态">
        <template #default="scope">
          <el-tag :type="scope.row.paymentStatus === 1 ? 'success' : 'danger'">
            {{ scope.row.paymentStatus === 1 ? '已结算' : '待付款' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="财务状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.paymentStatus === 1 ? 'success' : 'danger'">
            {{ scope.row.paymentStatus === 1 ? '已打款' : '待付款' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="200" fixed="right" align="center">
        <template #default="scope">
          <el-button
              v-if="scope.row.status === 1"
              type="success"
              link
              @click="handleInbound(scope.row.id)">
            确认入库
          </el-button>

          <el-button
              v-if="scope.row.paymentStatus === 0"
              type="warning"
              link
              @click="handlePay(scope.row.id)">
            财务打款
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end;"
        @size-change="getList"
        @current-change="getList"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPurchasePage, auditPurchase, rejectPurchase,inboundPurchaseOrder,payPurchaseOrder,quickPurchase } from '@/api/purchase'
import { getSupplierList } from '@/api/supplier'
import { getWarehouseList } from '@/api/warehouse'
import { getMaterialList } from '@/api/material'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const quickVisible = ref(false)
const quickForm = reactive({
  supplierId: null,
  warehouseId: null,
  materialId: null,
  planQty: 1,
  price: 0.00
})
const userRole = localStorage.getItem('userRole')
const supplierOptions = ref([])
const warehouseOptions = ref([])
const materialOptions = ref([])

const loadOptions = async () => {
  try {
    const [supRes, whRes, matRes] = await Promise.all([
      getSupplierList(),
      getWarehouseList(),
      getMaterialList()
    ])
    supplierOptions.value = supRes
    warehouseOptions.value = whRes
    materialOptions.value = matRes
  } catch (error) {
    console.error('加载下拉选项失败', error)
  }
}

const queryParams = reactive({
  page: 1,
  pageSize: 10
})

const getList = async () => {
  loading.value = true
  try {
    const res = await getPurchasePage(queryParams)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

// 执行审核通过
const handleAudit = (id) => {
  ElMessageBox.confirm('确定要审核通过该单据吗？', '审核确认', {
    type: 'warning',
    confirmButtonText: '审核通过',
    cancelButtonText: '取消'
  }).then(async () => {
    await auditPurchase(id)
    ElMessage.success('审核通过！单据状态变更为待入库。')
    getList()
  }).catch(() => {})
}

// 执行驳回逻辑
const handleReject = (id) => {
  ElMessageBox.prompt('请输入驳回原因', '驳回操作', {
    confirmButtonText: '确认驳回',
    cancelButtonText: '取消',
    inputPattern: /.+/,
    inputErrorMessage: '驳回原因不能为空'
  }).then(async ({ value }) => {
    await rejectPurchase({ id: id, rejectReason: value })
    ElMessage.success('采购单已驳回！')
    getList()
  }).catch(() => {})
}

const handleInbound = async (id) => {
  try {
    await ElMessageBox.confirm('确认将该单据的物资入库吗？', '提示', { type: 'warning' })
    await inboundPurchaseOrder(id)
    ElMessage.success('入库成功，库存已更新！')
    getList() // 重新拉取表格数据
  } catch (error) {
    if(error !== 'cancel') console.error(error)
  }
}

// 财务打款逻辑
const handlePay = async (id) => {
  try {
    await ElMessageBox.confirm('确认向供应商打款结算吗？', '提示', { type: 'info' })
    await payPurchaseOrder(id)
    ElMessage.success('财务结算成功！')
    getList()
  } catch (error) {
    if(error !== 'cancel') console.error(error)
  }
}
const handleMaterialChange = (val) => {
  const selectedMaterial = materialOptions.value.find(item => item.id === val)
  if (selectedMaterial && selectedMaterial.price) {
    quickForm.price = selectedMaterial.price // 自动填入价格
  }
}

const openQuickPurchase = () => {
  loadOptions()
  Object.assign(quickForm, { supplierId: null, warehouseId: null, materialId: null, planQty: 1, price: 0.00 })
  quickVisible.value = true
}

const submitQuick = async () => {
  if (!quickForm.supplierId || !quickForm.warehouseId || !quickForm.materialId) {
    return ElMessage.warning('请将供应商、仓库和食材填写完整！')
  }

  try {
    await quickPurchase(quickForm)
    ElMessage.success('采购单发起成功！')
    quickVisible.value = false
    getList() // 刷新你表格里的采购单列表
  } catch (error) {
    console.error('提交失败', error)
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container { padding: 20px; }
.filter-container { margin-bottom: 20px; }
</style>