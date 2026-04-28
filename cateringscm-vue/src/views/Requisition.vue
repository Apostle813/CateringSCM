<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="queryParams.orderNo" placeholder="出库单号" style="width: 200px; margin-right: 10px;" clearable />
      <el-select v-model="queryParams.status" placeholder="单据状态" style="width: 150px; margin-right: 10px;" clearable>
        <el-option label="待审核" :value="0" />
        <el-option label="已配送出库" :value="1" />
        <el-option label="已驳回" :value="9" />
      </el-select>
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button v-if="userRole === 'ADMIN' || userRole === 'PURCHASER'" type="success" @click="handleAdd">门店发起请购</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border style="margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="orderNo" label="出库单号" width="180" />
      <el-table-column prop="storeId" label="申请门店ID" width="120" align="center" />
      <el-table-column prop="warehouseId" label="出库仓库ID" width="120" align="center" />
      <el-table-column prop="createTime" label="申请时间" width="180" />
      <el-table-column label="出库状态" width="120" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : (scope.row.status === 9 ? 'danger' : 'warning')">
            {{ scope.row.status === 1 ? '已发货' : (scope.row.status === 9 ? '已驳回' : '待审核') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="财务结算" width="120" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.paymentStatus === 1 ? 'success' : 'danger'">
            {{ scope.row.paymentStatus === 1 ? '已结算' : '未结算' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" align="center" fixed="right">
        <template #default="scope">
          <template v-if="scope.row.status === 0">
            <template v-if="userRole === 'ADMIN' || userRole === 'WAREHOUSE'">
              <el-button type="primary" size="small" @click="handleAudit(scope.row.id)">审核发货</el-button>
              <el-button type="danger" size="small" @click="handleReject(scope.row.id)">驳回</el-button>
            </template>
            <span v-else style="color: #E6A23C; font-size: 13px;">等待审核</span>
          </template>
          
          <el-button
              v-if="scope.row.status === 1 && scope.row.paymentStatus === 0 && userRole === 'ADMIN'"
              type="warning"
              size="small"
              style="margin-left: 10px;"
              @click="handlePay(scope.row.id)">
            内部结算
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

    <!-- 发起请购弹窗 -->
    <el-dialog title="门店发起请购" v-model="dialogVisible" width="600px">
      <el-form :model="form" label-width="120px">
        <el-form-item label="申请门店" required>
          <el-select v-model="form.storeId" placeholder="请选择请购门店" style="width: 100%">
            <el-option label="北京朝阳一店 (ID:1)" :value="1" />
            <el-option label="北京海淀二店 (ID:2)" :value="2" />
            <el-option label="上海黄浦店 (ID:3)" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="发货仓库" required>
          <el-select v-model="form.warehouseId" placeholder="请选择发货仓库" style="width: 100%">
            <el-option v-for="w in warehouseList" :key="w.id" :label="w.name" :value="w.id" />
          </el-select>
        </el-form-item>

        <el-divider>选择请购物资</el-divider>
        <div v-for="(item, index) in form.details" :key="index" style="display: flex; margin-bottom: 10px; gap: 10px;">
          <el-select v-model="item.materialId" placeholder="选择食材" style="flex: 2;">
            <el-option v-for="m in materialList" :key="m.id" :label="m.name + ' (' + m.unit + ')'" :value="m.id" />
          </el-select>
          <el-input-number v-model="item.planQty" :min="1" placeholder="数量" style="flex: 1;" />
          <el-button type="danger" icon="Delete" circle @click="removeDetail(index)" />
        </div>
        <el-button type="dashed" style="width: 100%" @click="addDetail">+ 添加一项物资</el-button>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确认提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRequisitionPage, submitRequisition, auditRequisitionOutbound, payRequisitionOrder, rejectRequisitionOrder } from '@/api/requisition'
import { getWarehouseList } from '@/api/warehouse'
import { getMaterialPage } from '@/api/material'

const userRole = localStorage.getItem('userRole') || 'ADMIN'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  orderNo: '',
  status: null
})

const dialogVisible = ref(false)
const form = reactive({
  storeId: null,
  warehouseId: null,
  details: []
})

const warehouseList = ref([])
const materialList = ref([])

const getList = async () => {
  loading.value = true
  try {
    const res = await getRequisitionPage(queryParams)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.page = 1
  getList()
}

const loadBasicData = async () => {
  const wRes = await getWarehouseList()
  warehouseList.value = wRes
  
  const mRes = await getMaterialPage({ page: 1, pageSize: 500 })
  materialList.value = mRes.records
}

const handleAdd = () => {
  form.storeId = null
  form.warehouseId = null
  form.details = []
  dialogVisible.value = true
}

const addDetail = () => {
  form.details.push({ materialId: null, planQty: 1 })
}

const removeDetail = (index) => {
  form.details.splice(index, 1)
}

const submitForm = async () => {
  if (!form.storeId || !form.warehouseId) {
    return ElMessage.warning('请选择门店和仓库')
  }
  if (form.details.length === 0) {
    return ElMessage.warning('请至少添加一项物资明细')
  }
  for (let d of form.details) {
    if (!d.materialId || !d.planQty) {
      return ElMessage.warning('物资明细不完整')
    }
  }

  try {
    await submitRequisition(form)
    ElMessage.success('请购单提交成功！')
    dialogVisible.value = false
    getList()
  } catch(e) {}
}

const handleAudit = (id) => {
  ElMessageBox.confirm('确认审核通过并执行出库扣减库存吗？', '发货确认', {
    type: 'warning'
  }).then(async () => {
    await auditRequisitionOutbound(id)
    ElMessage.success('出库成功，库存已扣减！')
    getList()
  }).catch(() => {})
}

const handleReject = (id) => {
  ElMessageBox.confirm('确认驳回该出库单吗？', '驳回确认', {
    type: 'warning'
  }).then(async () => {
    await rejectRequisitionOrder(id)
    ElMessage.success('已驳回！')
    getList()
  }).catch(() => {})
}

const handlePay = (id) => {
  ElMessageBox.confirm('确认对该出库单进行内部财务结算吗？', '结算确认', {
    type: 'warning'
  }).then(async () => {
    await payRequisitionOrder(id)
    ElMessage.success('结算成功！')
    getList()
  }).catch(() => {})
}

onMounted(() => {
  getList()
  loadBasicData()
})
</script>

<style scoped>
.app-container { padding: 20px; }
.filter-container { margin-bottom: 20px; }
</style>
