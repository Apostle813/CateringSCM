<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="queryParams.name" placeholder="输入食材名称搜索" style="width: 200px; margin-right: 10px;" clearable />
      <el-button type="primary" @click="handleQuery">搜索</el-button>
      <el-button type="success" @click="handleAdd">新增食材</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border style="margin-top: 20px; width: 100%">
      <el-table-column prop="id" label="ID" width="80" align="center" />
      <el-table-column prop="name" label="食材名称" />
      <el-table-column prop="category" label="分类" />
      <el-table-column prop="unit" label="单位" width="80" align="center" />
      <el-table-column prop="price" label="参考价 (元)" width="120" />
      <el-table-column prop="spec" label="规格说明" show-overflow-tooltip />
      <el-table-column label="操作" width="180" align="center">
        <template #default="scope">
          <el-button size="small" type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" link @click="handleDelete(scope.row)">删除</el-button>
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
      <el-form :model="form" label-width="100px">
        <el-form-item label="食材名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" style="width: 100%">
            <el-option label="蔬菜类" value="蔬菜类" />
            <el-option label="肉禽类" value="肉禽类" />
            <el-option label="海鲜类" value="海鲜类" />
            <el-option label="粮油类" value="粮油类" />
          </el-select>
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" placeholder="如: kg, 个, 瓶" />
        </el-form-item>
        <el-form-item label="参考价">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="0.1" />
        </el-form-item>
        <el-form-item label="规格说明">
          <el-input type="textarea" v-model="form.spec" />
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMaterialPage, addMaterial, updateMaterial, deleteMaterial } from '@/api/material'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  name: ''
})

const form = reactive({
  id: null,
  name: '',
  category: '',
  unit: '',
  price: 0,
  spec: ''
})

// 获取列表数据
const getList = async () => {
  loading.value = true
  try {
    const res = await getMaterialPage(queryParams)
    // 对应后端 PageResult 的 records 和 total
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

const resetForm = () => {
  Object.assign(form, { id: null, name: '', category: '', unit: '', price: 0, spec: '' })
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增食材'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  resetForm()
  Object.assign(form, row)
  dialogTitle.value = '编辑食材'
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除食材 "${row.name}" 吗？`, '警告', { type: 'warning' }).then(async () => {
    await deleteMaterial(row.id)
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

const submitForm = async () => {
  if (!form.name) return ElMessage.warning('食材名称不能为空')

  if (form.id) {
    await updateMaterial(form)
    ElMessage.success('修改成功')
  } else {
    await addMaterial(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  getList()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.app-container { padding: 20px; }
.filter-container { margin-bottom: 20px; }
</style>