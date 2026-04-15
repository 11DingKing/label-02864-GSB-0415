<template>
  <div class="page-container">
    <div class="page-header">
      <h2>考试管理</h2>
      <el-button type="primary" @click="$router.push('/exam/create')">
        <el-icon><Plus /></el-icon>
        创建考试
      </el-button>
    </div>
    
    <div class="card">
      <el-table :data="examList" v-loading="loading" stripe>
        <el-table-column prop="title" label="考试名称" min-width="200" />
        <el-table-column prop="duration" label="时长(分钟)" width="100" align="center" />
        <el-table-column prop="totalScore" label="总分" width="80" align="center" />
        <el-table-column label="考试时间" width="320">
          <template #default="{ row }">
            {{ formatTime(row.startTime) }} ~ {{ formatTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :class="['status-tag', getStatusClass(row.status)]">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" link size="small" @click="$router.push(`/exam/${row.id}/questions`)">题目管理</el-button>
              <el-button type="primary" link size="small" @click="$router.push(`/exam/${row.id}/records`)">成绩查看</el-button>
              <el-button v-if="row.status === 0" type="success" link size="small" @click="handlePublish(row)">发布</el-button>
              <el-button v-if="row.status === 0" type="primary" link size="small" @click="$router.push(`/exam/edit/${row.id}`)">编辑</el-button>
              <el-button v-if="row.status === 0" type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @change="loadExamList"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getExamList, deleteExam, publishExam } from '../../api/exam'

const loading = ref(false)
const examList = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })

const loadExamList = async () => {
  loading.value = true
  try {
    const res = await getExamList({ page: pagination.page, size: pagination.size })
    examList.value = res.records
    pagination.total = res.total
  } finally {
    loading.value = false
  }
}

const handlePublish = async (row) => {
  await ElMessageBox.confirm('确定要发布此考试吗？发布后将无法修改', '提示')
  await publishExam(row.id)
  ElMessage.success('发布成功')
  loadExamList()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除此考试吗？', '提示', { type: 'warning' })
  await deleteExam(row.id)
  ElMessage.success('删除成功')
  loadExamList()
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

const getStatusText = (status) => {
  const map = { 0: '草稿', 1: '已发布', 2: '已结束' }
  return map[status] || '未知'
}

const getStatusClass = (status) => {
  const map = { 0: 'draft', 1: 'published', 2: 'ended' }
  return map[status] || ''
}

onMounted(loadExamList)
</script>

<style lang="scss" scoped>
.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.action-buttons {
  display: flex;
  flex-wrap: nowrap;
  gap: 4px;
}
</style>
