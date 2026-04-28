<template>
  <div class="app-container">
    <div class="filter-container">
      <el-input v-model="queryParams.username" placeholder="登录账号" style="width: 200px; margin-right: 10px;" clearable />
      <el-input v-model="queryParams.realName" placeholder="真实姓名" style="width: 200px; margin-right: 10px;" clearable />
      <el-button type="primary" @click="handleQuery">查询</el-button>
      <el-button type="success" @click="handleAdd">新增员工</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" border style="margin-top: 20px;">
      <el-table-column prop="id" label="ID" width="60" align="center" />
      <el-table-column prop="username" label="登录账号" width="150" />
      <el-table-column prop="realName" label="真实姓名" width="120" />
      <el-table-column prop="phone" label="联系电话" width="150" />
      <el-table-column label="角色" width="120">
        <template #default="scope">
          <el-tag :type="getRoleTagType(scope.row.roleId)">{{ getRoleName(scope.row.roleId) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="scope">
          <el-switch
            v-model="scope.row.status"
            :active-value="1"
            :inactive-value="0"
            @change="(val) => handleStatusChange(scope.row, val)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
      <el-table-column label="操作" width="180" align="center" fixed="right">
        <template #default="scope">
          <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(scope.row)">删除</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="isEdit ? '修改员工' : '新增员工'" v-model="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="登录账号" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" placeholder="用于登录系统" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="如：张三" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="11位手机号码" />
        </el-form-item>
        <el-form-item label="系统角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择分配的角色" style="width: 100%">
            <el-option v-for="r in roleList" :key="r.id" :label="r.roleName" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="密码" v-if="!isEdit">
          <el-alert title="初始密码将自动生成为: 123456" type="info" :closable="false" />
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
import { getUserPage, addUser, updateUser, deleteUser, getRoleList } from '@/api/user'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const roleList = ref([])

const queryParams = reactive({
  page: 1,
  pageSize: 10,
  username: '',
  realName: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const form = reactive({
  id: null,
  username: '',
  realName: '',
  phone: '',
  roleId: null,
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const getList = async () => {
  loading.value = true
  try {
    const res = await getUserPage(queryParams)
    tableData.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const loadRoles = async () => {
  const res = await getRoleList()
  roleList.value = res
}

const handleQuery = () => {
  queryParams.page = 1
  getList()
}

const handleAdd = () => {
  isEdit.value = false
  form.id = null
  form.username = ''
  form.realName = ''
  form.phone = ''
  form.roleId = null
  form.status = 1
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleStatusChange = async (row, val) => {
  try {
    await updateUser({ id: row.id, status: val })
    ElMessage.success(val === 1 ? '已启用账号' : '已停用账号')
  } catch (e) {
    row.status = val === 1 ? 0 : 1
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      if (isEdit.value) {
        await updateUser(form)
        ElMessage.success('修改成功')
      } else {
        await addUser(form)
        ElMessage.success('新增成功，初始密码为 123456')
      }
      dialogVisible.value = false
      getList()
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定要删除该系统账号吗？', '警告', { type: 'warning' }).then(async () => {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    getList()
  }).catch(() => {})
}

const getRoleName = (roleId) => {
  const r = roleList.value.find(item => item.id === roleId)
  return r ? r.roleName : '未知'
}

const getRoleTagType = (roleId) => {
  if (roleId === 1) return 'danger'  // Admin
  if (roleId === 2) return 'primary' // Purchaser
  if (roleId === 3) return 'warning' // Warehouse
  return 'info'
}

onMounted(() => {
  loadRoles()
  getList()
})
</script>

<style scoped>
.app-container { padding: 20px; }
.filter-container { margin-bottom: 20px; }
</style>
