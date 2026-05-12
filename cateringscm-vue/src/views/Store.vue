<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="queryParams.name" placeholder="门店名称" style="width: 200px; margin-right: 10px;" clearable />
      <el-select v-model="queryParams.status" placeholder="门店状态" style="width: 150px; margin-right: 10px;" clearable>
        <el-option label="正常" :value="1" />
        <el-option label="停用" :value="0" />
      </el-select>
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button type="success" icon="Plus" @click="handleAdd">新增门店</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border stripe style="margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="门店名称" width="180">
        <template #default="scope">
          <span style="font-weight: bold;">{{ scope.row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="address" label="门店地址" min-width="200" show-overflow-tooltip />
      <el-table-column prop="contact" label="联系人" width="120" />
      <el-table-column prop="phone" label="联系电话" width="150" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '正常' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button type="primary" link size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" link size="small" @click="handleDelete(scope.row)">删除</el-button>
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

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="门店名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入门店名称" />
        </el-form-item>
        <el-form-item label="门店地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入门店地址" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="form.contact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="状态" v-if="form.id">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="正常" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStorePage, addStore, updateStore, deleteStore } from '@/api/store'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  name: '',
  status: null
})

const form = reactive({
  id: null,
  name: '',
  address: '',
  contact: '',
  phone: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '门店名称不能为空', trigger: 'blur' }],
  address: [{ required: true, message: '门店地址不能为空', trigger: 'blur' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getStorePage(queryParams)
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

const handleAdd = () => {
  dialogTitle.value = '新增门店'
  form.id = null
  form.name = ''
  form.address = ''
  form.contact = ''
  form.phone = ''
  form.status = 1
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑门店'
  form.id = row.id
  form.name = row.name
  form.address = row.address
  form.contact = row.contact
  form.phone = row.phone
  form.status = row.status
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除门店「${row.name}」吗？`, '删除确认', {
    type: 'warning'
  }).then(async () => {
    await deleteStore(row.id)
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (form.id) {
          await updateStore(form)
          ElMessage.success('修改成功')
        } else {
          await addStore(form)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        getList()
      } catch (e) {
        console.error(e)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => { getList() })
</script>

<style scoped>
.app-container { padding: 20px; }
.filter-container { margin-bottom: 20px; display: flex; flex-wrap: wrap; gap: 10px; }
</style>
