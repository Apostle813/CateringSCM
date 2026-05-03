<template>
  <div class="app-container">
    <el-alert title="仓库温区配置直接影响库存保质期，非系统运维人员请勿修改！" type="warning" show-icon style="margin-bottom: 20px;" />

    <div style="margin-bottom: 15px;">
      <el-button type="primary" @click="handleAdd">新增仓库</el-button>
    </div>

    <el-table :data="tableData" border stripe v-loading="loading">
      <el-table-column prop="id" label="仓库ID" width="80" align="center" />
      <el-table-column prop="name" label="仓库名称 (温区)" width="180">
        <template #default="scope">
          <span style="font-weight: bold;">{{ scope.row.name }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="location" label="物理位置" min-width="150" />
      <el-table-column prop="manager" label="负责人" width="120" />
      <el-table-column prop="createTime" label="建成时间" width="180" />
      <el-table-column label="操作" width="120" align="center">
        <template #default="scope">
          <el-button type="primary" link size="small" @click="handleEdit(scope.row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="仓库名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入仓库名称" />
        </el-form-item>
        <el-form-item label="物理位置" prop="location">
          <el-input v-model="form.location" placeholder="请输入物理位置" />
        </el-form-item>
        <el-form-item label="负责人" prop="manager">
          <el-input v-model="form.manager" placeholder="请输入负责人" />
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
import { getWarehouseList, addWarehouse, updateWarehouse } from '@/api/warehouse'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  name: '',
  location: '',
  manager: ''
})

const rules = {
  name: [{ required: true, message: '仓库名称不能为空', trigger: 'blur' }],
  location: [{ required: true, message: '物理位置不能为空', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    tableData.value = await getWarehouseList()
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  dialogTitle.value = '新增仓库'
  form.id = null
  form.name = ''
  form.location = ''
  form.manager = ''
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑仓库'
  form.id = row.id
  form.name = row.name
  form.location = row.location
  form.manager = row.manager
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (form.id) {
          await updateWarehouse(form)
          ElMessage.success('修改成功')
        } else {
          await addWarehouse(form)
          ElMessage.success('新增成功')
        }
        dialogVisible.value = false
        loadData()
      } catch (e) {
        console.error(e)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

onMounted(() => { loadData() })
</script>

<style scoped>
.app-container { padding: 20px; }
</style>