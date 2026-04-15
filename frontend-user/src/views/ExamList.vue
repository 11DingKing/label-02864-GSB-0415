<template>
  <div class="page-container">
    <div class="page-header">
      <h2>可参加的考试</h2>
    </div>
    
    <div v-if="loading" class="loading">
      <el-icon class="is-loading"><Loading /></el-icon>
      加载中...
    </div>
    
    <div v-else-if="examList.length === 0" class="empty">
      <el-empty description="暂无可参加的考试" />
    </div>
    
    <div v-else class="exam-grid">
      <div v-for="exam in examList" :key="exam.id" class="exam-card">
        <div class="exam-title">{{ exam.title }}</div>
        <div class="exam-desc">{{ exam.description || '暂无描述' }}</div>
        <div class="exam-info">
          <div class="info-item">
            <el-icon><Clock /></el-icon>
            <span>{{ exam.duration }}分钟</span>
          </div>
          <div class="info-item">
            <el-icon><Document /></el-icon>
            <span>满分{{ exam.totalScore }}分</span>
          </div>
        </div>
        <div class="exam-time">
          <span>考试时间: {{ formatTime(exam.startTime) }} ~ {{ formatTime(exam.endTime) }}</span>
        </div>
        <div class="exam-actions">
          <el-button 
            v-if="!takenExams.has(exam.id) && canTake(exam)" 
            type="primary" 
            @click="goExam(exam)"
          >
            开始考试
          </el-button>
          <el-tag v-else-if="takenExams.has(exam.id)" type="info">已参加</el-tag>
          <el-tag v-else type="warning">不在考试时间内</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getExamList, getMyRecords } from '../api'

const router = useRouter()
const loading = ref(true)
const examList = ref([])
const takenExams = ref(new Set())

const loadData = async () => {
  try {
    const [exams, records] = await Promise.all([
      getExamList({ page: 1, size: 100 }),
      getMyRecords({ page: 1, size: 100 })
    ])
    examList.value = exams.records
    takenExams.value = new Set(records.records.map(r => r.examId))
  } finally {
    loading.value = false
  }
}

const canTake = (exam) => {
  const now = new Date()
  const start = new Date(exam.startTime)
  const end = new Date(exam.endTime)
  return now >= start && now <= end
}

const goExam = (exam) => {
  router.push(`/exam/${exam.id}`)
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.loading, .empty {
  text-align: center;
  padding: 60px;
  color: #909399;
}

.exam-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

.exam-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  min-height: 220px;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
}

.exam-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.exam-desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 16px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 42px;
}

.exam-info {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #606266;
  font-size: 14px;
}

.exam-time {
  font-size: 12px;
  color: #909399;
  margin-bottom: 16px;
}

.exam-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: auto;
}
</style>
