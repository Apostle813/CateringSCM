<template>
  <div class="app-container">
    <div class="filter-container">
      <!-- 筛选表单 -->
      <el-input v-model="queryParams.orderNo" placeholder="采购单号" style="width: 200px; margin-right: 10px;" clearable />
      <el-select v-model="queryParams.status" placeholder="单据状态" style="width: 150px; margin-right: 10px;" clearable>
        <el-option label="待审核" :value="0" />
        <el-option label="待入库" :value="1" />
        <el-option label="已入库" :value="2" />
        <el-option label="已驳回" :value="9" />
      </el-select>
      <el-date-picker v-model="queryParams.startDate" type="date" placeholder="开始日期" style="margin-right: 10px;" />
      <el-date-picker v-model="queryParams.endDate" type="date" placeholder="结束日期" style="margin-right: 10px;" />
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button type="success" icon="Plus" v-if="userRole === 'ADMIN' || userRole === 'PURCHASER'" @click="openPurchaseDialog">发起采购</el-button>
      <el-button type="primary" icon="Refresh" @click="getList">刷新列表</el-button>
    </div>

    <!-- 发起采购弹窗(多食材) -->
    <el-dialog title="发起采购申请" v-model="purchaseVisible" width="750px">
      <el-form :model="purchaseForm" label-width="120px">
        <el-form-item label="供应商" required>
          <el-select v-model="purchaseForm.supplierId" placeholder="请选择供应商" style="width: 100%">
            <el-option v-for="item in supplierOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="入库仓库" required>
          <el-select v-model="purchaseForm.warehouseId" placeholder="请选择仓库" style="width: 100%" @change="onWarehouseChange">
            <el-option v-for="item in warehouseOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="purchaseForm.remark" type="textarea" />
        </el-form-item>

        <el-divider>采购物资明细</el-divider>
        <div v-for="(item, index) in purchaseForm.details" :key="index" style="display: flex; margin-bottom: 10px; gap: 10px; align-items: center;">
          <el-select v-model="item.materialId" placeholder="选择食材" style="flex: 2;" @change="handleMaterialChange($event, index)">
            <el-option v-for="m in materialOptions" :key="m.id" :label="m.name + ' (' + m.unit + ')'" :value="m.id" />
          </el-select>
          <el-input-number v-model="item.planQty" :min="1" placeholder="数量" style="flex: 1;" />
          <el-input-number v-model="item.price" :min="0" :precision="2" placeholder="单价" style="flex: 1;" />
          <el-tag v-if="item.stockLabel" :type="item.stockType || 'info'" size="small" style="white-space: nowrap;">
            {{ item.stockLabel }}
          </el-tag>
          <el-button type="danger" icon="Delete" circle @click="removeDetail(index)" />
        </div>
        <el-button type="dashed" style="width: 100%" @click="addDetail">+ 添加一项物资</el-button>
        
        <div style="margin-top: 15px; text-align: right;">
          总金额: <span style="color: #F56C6C; font-size: 20px; font-weight: bold;">¥{{ calculateTotal() }}</span>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="purchaseVisible = false">取消</el-button>
        <el-button type="primary" @click="submitPurchaseOrder">确认提交</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情弹窗 -->
    <el-dialog title="采购订单详情" v-model="detailVisible" width="750px">
      <el-descriptions :column="2" border style="margin-bottom: 20px;">
        <el-descriptions-item label="单号">{{ detailInfo.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="detailInfo.status === 0" type="warning">待审核</el-tag>
          <el-tag v-else-if="detailInfo.status === 1" type="success">待入库</el-tag>
          <el-tag v-else-if="detailInfo.status === 2">已入库</el-tag>
          <el-tag v-else-if="detailInfo.status === 9" type="danger">已驳回</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="供应商">{{ detailInfo.supplierName || detailInfo.supplierId }}</el-descriptions-item>
        <el-descriptions-item label="入库仓库">{{ detailInfo.warehouseName || detailInfo.warehouseId }}</el-descriptions-item>
        <el-descriptions-item label="总金额">
          <span style="color: #F56C6C; font-weight: bold;">¥{{ detailInfo.totalAmount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付状态">
          <el-tag :type="detailInfo.paymentStatus === 1 ? 'success' : 'danger'">
            {{ detailInfo.paymentStatus === 1 ? '已打款' : '待付款' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detailInfo.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailInfo.remark || '无' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>采购物资明细</el-divider>
      <el-table :data="detailList" border size="small">
        <el-table-column prop="materialName" label="食材名称" min-width="120" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="unit" label="单位" width="80" align="center" />
        <el-table-column prop="planQty" label="计划数量" width="100" align="center" />
        <el-table-column prop="realQty" label="实际数量" width="100" align="center" />
        <el-table-column prop="price" label="单价" width="100" align="right">
          <template #default="scope">¥{{ scope.row.price }}</template>
        </el-table-column>
        <el-table-column prop="lineAmount" label="小计" width="120" align="right">
          <template #default="scope">
            <span style="color: #F56C6C; font-weight: bold;">¥{{ scope.row.lineAmount }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <el-table :data="tableData" v-loading="loading" border stripe style="margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column prop="orderNo" label="单号" width="180" />
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
          <el-tag v-else-if="scope.row.status === 2">已入库</el-tag>
          <el-tag v-else-if="scope.row.status === 9" type="danger">已驳回</el-tag>
          <el-tag v-else type="info">未知</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="支付状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.paymentStatus === 1 ? 'success' : 'danger'">
            {{ scope.row.paymentStatus === 1 ? '已打款' : '待付款' }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="280" fixed="right" align="center">
        <template #default="scope">
          <el-button type="primary" link size="small" @click="showDetail(scope.row)">查看详情</el-button>

          <template v-if="scope.row.status === 0 && (userRole === 'ADMIN' || userRole === 'WAREHOUSE')">
            <el-button type="success" link size="small" @click="handleAudit(scope.row.id)">审核通过</el-button>
            <el-button size="small" type="danger" link @click="handleReject(scope.row.id)">驳回</el-button>
          </template>

          <el-button
              v-if="scope.row.status === 1"
              type="success"
              link
              size="small"
              @click="handleInbound(scope.row.id)">
            确认入库
          </el-button>

          <el-button
              v-if="scope.row.paymentStatus === 0 && (userRole === 'ADMIN')"
              type="warning"
              link
              size="small"
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
import { getPurchasePage, auditPurchase, rejectPurchase, inboundPurchaseOrder, payPurchaseOrder, submitPurchase, getPurchaseDetails } from '@/api/purchase'
import { getSupplierList } from '@/api/supplier'
import { getWarehouseList } from '@/api/warehouse'
import { getMaterialList } from '@/api/material'
import { getStockQty } from '@/api/inventory'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const purchaseVisible = ref(false)
const supplierOptions = ref([])
const warehouseOptions = ref([])
const materialOptions = ref([])

// 详情弹窗
const detailVisible = ref(false)
const detailInfo = ref({})
const detailList = ref([])

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  orderNo: '',
  status: null,
  startDate: '',
  endDate: ''
})

const purchaseForm = reactive({
  supplierId: null,
  warehouseId: null,
  remark: '',
  details: []
})

const userRole = localStorage.getItem('userRole')

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

const openPurchaseDialog = () => {
  loadOptions()
  purchaseForm.supplierId = null
  purchaseForm.warehouseId = null
  purchaseForm.remark = ''
  purchaseForm.details = []
  purchaseVisible.value = true
}

const addDetail = () => {
  purchaseForm.details.push({ materialId: null, planQty: 1, price: 0, stockLabel: '', stockType: 'info' })
}

const removeDetail = (index) => {
  purchaseForm.details.splice(index, 1)
}

// 仓库改变时，清空所有明细的库存提示
const onWarehouseChange = () => {
  purchaseForm.details.forEach(item => {
    item.stockLabel = ''
    item.stockType = 'info'
  })
  // 重新加载已有食材的库存
  purchaseForm.details.forEach((item, index) => {
    if (item.materialId && purchaseForm.warehouseId) {
      loadStockLabel(index)
    }
  })
}

// 选择食材后加载库存提示
const handleMaterialChange = async (val, index) => {
  const selected = materialOptions.value.find(item => item.id === val)
  if (selected && selected.price) {
    purchaseForm.details[index].price = selected.price
  }
  // 查询库存
  if (purchaseForm.warehouseId && val) {
    await loadStockLabel(index)
  } else {
    purchaseForm.details[index].stockLabel = ''
  }
}

// 加载库存标签
const loadStockLabel = async (index) => {
  const item = purchaseForm.details[index]
  if (!purchaseForm.warehouseId || !item.materialId) return
  try {
    const unit = materialOptions.value.find(m => m.id === item.materialId)?.unit || ''
    const qty = await getStockQty({ warehouseId: purchaseForm.warehouseId, materialId: item.materialId })
    item.stockLabel = `库存: ${qty}${unit}`
    item.stockType = qty > 0 ? 'success' : 'danger'
  } catch (e) {
    item.stockLabel = ''
  }
}

const calculateTotal = () => {
  return purchaseForm.details.reduce((sum, item) => {
    return sum + (item.planQty || 0) * (item.price || 0)
  }, 0).toFixed(2)
}

const submitPurchaseOrder = async () => {
  if (!purchaseForm.supplierId || !purchaseForm.warehouseId) {
    return ElMessage.warning('请选择供应商和仓库')
  }
  if (purchaseForm.details.length === 0) {
    return ElMessage.warning('请至少添加一项采购物资')
  }
  for (let d of purchaseForm.details) {
    if (!d.materialId || !d.planQty || !d.price) {
      return ElMessage.warning('采购明细不完整')
    }
  }

  try {
    const submitData = {
      supplierId: purchaseForm.supplierId,
      warehouseId: purchaseForm.warehouseId,
      remark: purchaseForm.remark,
      totalAmount: calculateTotal(),
      details: purchaseForm.details
    }
    await submitPurchase(submitData)
    ElMessage.success('采购申请提交成功！')
    purchaseVisible.value = false
    getList()
  } catch (error) {
    console.error('提交失败', error)
  }
}

const handleQuery = () => {
  queryParams.page = 1
  getList()
}

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

// 查看详情
const showDetail = async (row) => {
  detailInfo.value = row
  try {
    detailList.value = await getPurchaseDetails(row.id)
    // 补充供应商和仓库名称
    if (!detailInfo.value.supplierName && supplierOptions.value.length === 0) {
      await loadOptions()
    }
    const supplier = supplierOptions.value.find(s => s.id === row.supplierId)
    if (supplier) detailInfo.value.supplierName = supplier.name
    const warehouse = warehouseOptions.value.find(w => w.id === row.warehouseId)
    if (warehouse) detailInfo.value.warehouseName = warehouse.name
    detailVisible.value = true
  } catch (e) {
    console.error('加载详情失败', e)
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
    getList()
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

onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container { padding: 20px; }
.filter-container { margin-bottom: 20px; display: flex; flex-wrap: wrap; gap: 10px; }
</style>
