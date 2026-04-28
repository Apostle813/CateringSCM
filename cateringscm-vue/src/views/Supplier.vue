<template>
  <div class="app-container">
    <div class="filter-container" style="margin-bottom: 20px; display: flex; gap: 10px;">
      <el-input v-model="queryParams.name" placeholder="请输入供应商名称" style="width: 200px" clearable @clear="loadData" @keyup.enter="loadData" />
      <el-button type="primary" icon="Search" @click="loadData">搜索</el-button>
      <el-button type="success" icon="Plus" @click="handleAdd">新增供应商</el-button>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="供应商编号" width="100" align="center" />
      <el-table-column prop="name" label="供应商名称" min-width="150" />
      <el-table-column prop="contact" label="联系人" width="120" />
      <el-table-column prop="phone" label="联系电话" width="150" />
      <el-table-column prop="status" label="合作状态" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
            {{ scope.row.status === 1 ? '正常合作' : '已终止' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="录入时间" width="180" />
      <el-table-column label="操作" width="150" align="center" fixed="right">
        <template #default="scope">
          <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(scope.row)" v-if="scope.row.status === 1">终止合作</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="queryParams.page"
      v-model:page-size="queryParams.pageSize"
      :total="total"
      layout="total, prev, pager, next"
      style="margin-top: 20px; justify-content: flex-end;"
      @current-change="loadData"
    />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="form" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name" :rules="[{ required: true, message: '请输入供应商名称', trigger: 'blur' }]">
          <el-input v-model="form.name" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="联系人" prop="contact">
          <el-input v-model="form.contact" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="正常合作" inactive-text="已终止" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getSupplierPage, addSupplier, updateSupplier, deleteSupplier } from '@/api/supplier'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const queryParams = reactive({ page: 1, pageSize: 10, name: '' })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const form = reactive({ id: null, name: '', contact: '', phone: '', status: 1 })

const loadData = async () => {
  loading.value = true
  try {
    const res = await getSupplierPage(queryParams)
    tableData.value = res.records
    total.value = res.total
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增供应商'
  Object.assign(form, { id: null, name: '', contact: '', phone: '', status: 1 })
  dialogVisible.value = true
  if (formRef.value) formRef.value.clearValidate()
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑供应商'
  Object.assign(form, { ...row })
  dialogVisible.value = true
  if (formRef.value) formRef.value.clearValidate()
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.id) {
          await updateSupplier(form)
          ElMessage.success('修改成功')
        } else {
          await addSupplier(form)
          ElMessage.success('添加成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (e) {
        console.error(e)
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要终止与该供应商的合作吗？', '提示', { type: 'warning' }).then(async () => {
    try {
      await deleteSupplier(row.id)
      ElMessage.success('操作成功')
      loadData()
    } catch (e) {
      console.error(e)
    }
  }).catch(() => {})
}

onMounted(() => { loadData() })
</script>
<style scoped>.app-container { padding: 20px; }</style>