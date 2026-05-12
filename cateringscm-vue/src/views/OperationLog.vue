<template>
  <div class="app-container">
    <el-alert title="审计追溯：所有业务操作均在此记录，不可篡改。" type="warning" show-icon style="margin-bottom: 20px;" />

    <div class="filter-container">
      <el-select v-model="queryParams.operationType" placeholder="操作类型" style="width: 160px;" clearable @change="handleQuery">
        <el-option v-for="item in operationTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-input v-model="queryParams.operatorName" placeholder="操作人姓名" style="width: 150px;" clearable />
      <el-select v-model="queryParams.targetType" placeholder="业务对象" style="width: 140px;" clearable @change="handleQuery">
        <el-option v-for="item in targetTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-date-picker v-model="queryParams.startDate" type="date" placeholder="开始日期" style="margin-right: 5px;" />
      <el-date-picker v-model="queryParams.endDate" type="date" placeholder="结束日期" style="margin-right: 5px;" />
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button icon="Refresh" @click="resetQuery">重置</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border size="small" style="width: 100%">
      <el-table-column prop="createTime" label="操作时间" width="160" />
      <el-table-column prop="operatorName" label="操作人" width="100" align="center" />
      <el-table-column label="操作类型" width="130" align="center">
        <template #default="scope">
          <el-tag :type="getTagType(scope.row.operationType)" size="small">
            {{ getTypeLabel(scope.row.operationType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="operationDesc" label="操作描述" min-width="300" show-overflow-tooltip />
      <el-table-column label="业务对象" width="100" align="center">
        <template #default="scope">
          {{ getTargetLabel(scope.row.targetType) }}
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
import { getOperationLogPage } from '@/api/operationLog'
import { Refresh } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const queryParams = reactive({
  page: 1,
  pageSize: 15,
  operationType: '',
  operatorName: '',
  targetType: '',
  startDate: '',
  endDate: ''
})

const operationTypeOptions = [
  { value: 'USER_LOGIN', label: '用户登录' },
  { value: 'USER_CREATE', label: '创建用户' },
  { value: 'USER_UPDATE', label: '修改用户' },
  { value: 'USER_DELETE', label: '删除用户' },
  { value: 'PURCHASE_SUBMIT', label: '发起采购' },
  { value: 'PURCHASE_AUDIT_PASS', label: '采购审核通过' },
  { value: 'PURCHASE_REJECT', label: '采购驳回' },
  { value: 'PURCHASE_PAY', label: '采购打款' },
  { value: 'PURCHASE_INBOUND', label: '采购入库' },
  { value: 'REQUISITION_SUBMIT', label: '发起请购' },
  { value: 'REQUISITION_AUDIT', label: '请购审核发货' },
  { value: 'REQUISITION_REJECT', label: '请购驳回' },
  { value: 'REQUISITION_PAY', label: '请购结算' },
  { value: 'INVENTORY_OUTBOUND', label: '手动出库' },
  { value: 'INVENTORY_ADJUST', label: '盘点调整' },
  { value: 'SUPPLIER_ADD', label: '新增供应商' },
  { value: 'SUPPLIER_UPDATE', label: '修改供应商' },
  { value: 'SUPPLIER_DELETE', label: '停用供应商' },
  { value: 'MATERIAL_ADD', label: '新增食材' },
  { value: 'MATERIAL_UPDATE', label: '修改食材' },
  { value: 'MATERIAL_DELETE', label: '删除食材' },
  { value: 'WAREHOUSE_ADD', label: '新增仓库' },
  { value: 'WAREHOUSE_UPDATE', label: '修改仓库' },
  { value: 'STORE_ADD', label: '新增门店' },
  { value: 'STORE_UPDATE', label: '修改门店' },
  { value: 'STORE_DELETE', label: '停用门店' }
]

const targetTypeOptions = [
  { value: 'purchase_order', label: '采购订单' },
  { value: 'requisition_order', label: '请购出库单' },
  { value: 'inventory', label: '库存管理' },
  { value: 'supplier', label: '供应商' },
  { value: 'material', label: '食材' },
  { value: 'warehouse', label: '仓库' },
  { value: 'store', label: '门店' },
  { value: 'sys_user', label: '系统用户' }
]

const typeTagMap = {
  USER_LOGIN: '', USER_CREATE: '', USER_UPDATE: '', USER_DELETE: 'danger',
  PURCHASE_SUBMIT: 'primary', PURCHASE_AUDIT_PASS: 'success', PURCHASE_REJECT: 'danger',
  PURCHASE_PAY: 'warning', PURCHASE_INBOUND: 'success',
  REQUISITION_SUBMIT: 'primary', REQUISITION_AUDIT: 'success', REQUISITION_REJECT: 'danger', REQUISITION_PAY: 'warning',
  INVENTORY_OUTBOUND: 'danger', INVENTORY_ADJUST: 'warning',
  SUPPLIER_ADD: '', SUPPLIER_UPDATE: '', SUPPLIER_DELETE: 'danger',
  MATERIAL_ADD: '', MATERIAL_UPDATE: '', MATERIAL_DELETE: 'danger',
  WAREHOUSE_ADD: '', WAREHOUSE_UPDATE: '',
  STORE_ADD: '', STORE_UPDATE: '', STORE_DELETE: 'danger'
}

const getTagType = (type) => typeTagMap[type] || 'info'
const getTypeLabel = (type) => {
  const found = operationTypeOptions.find(o => o.value === type)
  return found ? found.label : type
}
const getTargetLabel = (type) => {
  const found = targetTypeOptions.find(o => o.value === type)
  return found ? found.label : type || '-'
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getOperationLogPage(queryParams)
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

const resetQuery = () => {
  queryParams.operationType = ''
  queryParams.operatorName = ''
  queryParams.targetType = ''
  queryParams.startDate = ''
  queryParams.endDate = ''
  handleQuery()
}

onMounted(() => { getList() })
</script>

<style scoped>
.app-container { padding: 20px; }
.filter-container { margin-bottom: 20px; display: flex; flex-wrap: wrap; gap: 10px; }
</style>
