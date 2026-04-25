<template>
  <div class="app-container">
    <div class="filter-container">
      <el-button type="success" icon="Plus">发起新采购单 (待开发)</el-button>
      <el-button type="primary" icon="Refresh" @click="getList">刷新列表</el-button>
    </div>

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
          <el-tag v-else-if="scope.row.status === 2" type="success">已入库</el-tag>
          <el-tag v-else-if="scope.row.status === 9" type="danger">已驳回</el-tag>
          <el-tag v-else type="info">未知</el-tag>
        </template>
      </el-table-column>

      <el-table-column label="操作(库管权限)" width="200" align="center">
        <template #default="scope">
          <template v-if="scope.row.status === 0">
            <el-button size="small" type="success" @click="handleAudit(scope.row.id)">入库</el-button>
            <el-button size="small" type="danger" @click="handleReject(scope.row.id)">驳回</el-button>
          </template>
          <span v-else style="color: #909399; font-size: 13px;">流程已结束</span>
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
import { getPurchasePage, auditPurchase, rejectPurchase } from '@/api/purchase'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

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

// 执行入库大事务
const handleAudit = (id) => {
  ElMessageBox.confirm('确定要审核通过并将该单据物资入库吗？', '入库确认', {
    type: 'warning',
    confirmButtonText: '确定入库',
    cancelButtonText: '取消'
  }).then(async () => {
    await auditPurchase(id)
    ElMessage.success('入库成功！库存已更新。')
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

onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container { padding: 20px; }
.filter-container { margin-bottom: 20px; }
</style>