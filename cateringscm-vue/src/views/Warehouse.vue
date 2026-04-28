<template>
  <div class="app-container">
    <el-alert title="仓库温区配置直接影响库存保质期，非系统运维人员请勿修改！" type="warning" show-icon style="margin-bottom: 20px;" />

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
    </el-table>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getWarehouseList } from '@/api/warehouse' // 复用之前写的全量查询接口

const loading = ref(false)
const tableData = ref([])

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

onMounted(() => { loadData() })
</script>
<style scoped>.app-container { padding: 20px; }</style>