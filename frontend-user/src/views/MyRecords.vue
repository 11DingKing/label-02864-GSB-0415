<template>
  <div class="page-container">
    <div class="page-header">
      <h2>我的成绩</h2>
    </div>
    
    <div class="card">
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="examTitle" label="考试名称" min-width="200" />
        <el-table-column label="得分" width="120" align="center">
          <template #default="{ row }">
            <span :class="row.totalScore >= 60 ? 'score-pass' : 'score-fail'" style="font-size: 18px;">
              {{ row.totalScore }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="是否及格" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.totalScore >= 60 ? 'success' : 'danger'">
              {{ row.totalScore >= 60 ? '及格' : '不及格' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" width="180">
          <template #default="{ row }">{{ formatTime(row.startTime) }}</template>
        </el-table-column>
        <el-table-column label="提交时间" width="180">
          <template #default="{ row }">{{ formatTime(row.submitTime) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 2 ? 'success' : 'danger'">
              {{ row.status === 2 ? '已完成' : '未提交' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <el-button v-if="row.status === 2" type="primary" link @click="$router.push(`/record/${row.id}`)">
              查看详情
            </el-button>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          layout="total, prev, pager, next"
          @change="loadRecords"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMyRecords } from '../api'

const loading = ref(false)
const records = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })

const loadRecords = async () => {
  loading.value = true
  try {
    const res = await getMyRecords({ page: pagination.page, size: pagination.size })
    records.value = res.records
    pagination.total = res.total
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

onMounted(loadRecords)
</script>

<style lang="scss" scoped>
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.text-muted {
  color: #909399;
}
</style>
