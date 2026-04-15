<template>
  <div class="page-container">
    <div class="page-header">
      <h2>批改试卷 - {{ studentInfo?.realName || '未知学生' }}</h2>
      <div>
        <el-button @click="$router.back()">返回</el-button>
        <el-button 
          type="primary" 
          @click="handleSave" 
          :loading="saving"
          :disabled="record?.status === 2 && !canEdit
        >
          {{ record?.status === 2 ? '已批改' : '保存评分' }}
        </el-button>
      </div>
    </div>
    
    <div class="card stats-card">
      <div class="stat-item">
        <div class="stat-value">{{ totalScore }}</div>
        <div class="stat-label">总分</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ autoScore }}</div>
        <div class="stat-label">自动评分</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ manualScore }}</div>
        <div class="stat-label">手动评分</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ essayQuestions.length }}</div>
        <div class="stat-label">简答题数</div>
      </div>
    </div>
    
    <div class="question-list">
      <div v-for="(q, index) in essayQuestions" :key="q.id" class="question-card">
        <div class="question-header">
          <span class="question-num">{{ index + 1 }}.</span>
          <el-tag size="small" type="danger">简答</el-tag>
          <span class="question-score">(满分: {{ q.score }}分)</span>
        </div>
        <div class="question-content">{{ q.content }}</div>
        
        <div class="section-title">参考答案：</div>
        <div class="reference-answer">{{ q.referenceAnswer || '无' }}</div>
        
        <div class="section-title">关键词：</div>
        <div class="keywords">
          <el-tag v-for="(kw, i) in getKeywords(q)" :key="i" size="small" type="info">
            {{ kw }}
          </el-tag>
        </div>
        
        <div class="section-title">学生答案：</div>
        <div class="student-answer">{{ getStudentAnswer(q.id) || '未作答' }}</div>
        
        <div class="section-title">自动评分：</div>
        <div class="auto-grade">
          <span>命中关键词：{{ getMatchedKeywords(q.id) }} 个</span>
          <span class="auto-score">自动得分: {{ getAutoScore(q.id) }} 分</span>
        </div>
        
        <div class="grade-section">
          <div class="grade-input">
            <span class="grade-label">评分：</span>
            <el-input-number
              v-model="grades[q.id].score"
              :min="0"
              :max="q.score"
              :disabled="record?.status === 2"
              @change="updateTotal"
            />
            <span class="score-max">/ {{ q.score }} 分</span>
          </div>
          <div class="comment-input">
            <span class="grade-label">评语：</span>
            <el-input
              v-model="grades[q.id].comment"
              type="textarea"
              :rows="3"
              placeholder="请输入评语（可选）"
              :disabled="record?.status === 2"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getExamById } from '../../api/exam'
import { getQuestionsByExamId } from '../../api/question'
import { getRecordDetail, getAnswers, gradeEssay } from '../../api/record'

const route = useRoute()
const router = useRouter()
const examId = route.params.examId
const recordId = route.params.recordId

const exam = ref(null)
const record = ref(null)
const studentInfo = ref({})
const questions = ref([])
const answers = ref([])
const saving = ref(false)
const grades = reactive({})

const canEdit = computed(() => record.value?.status !== 2)

const essayQuestions = computed(() => {
  return questions.value.filter(q => q.type === 4)
})

const totalScore = computed(() => {
  return Object.values(grades).reduce((sum, g) => sum + (g.score || 0), 0)
})

const autoScore = computed(() => {
  return answers.value.filter(a => a.questionType !== 4).reduce((sum, a) => sum + (a.score || 0), 0)
})

const manualScore = computed(() => {
  return Object.values(grades).reduce((sum, g) => sum + (g.score || 0), 0)
})

const loadData = async () => {
  try {
    exam.value = await getExamById(examId)
    questions.value = await getQuestionsByExamId(examId)
    record.value = await getRecordDetail(recordId)
    studentInfo.value = record.value.user || {}
    
    answers.value = await getAnswers(recordId)
    
    initGrades()
  } catch (e) {
    ElMessage.error('加载数据失败')
    router.back()
  }
}

const initGrades = () => {
  essayQuestions.value.forEach(q => {
    const answer = answers.value.find(a => a.questionId === q.id)
    grades[q.id] = {
      answerRecordId: answer?.id,
      score: answer?.score || 0,
      comment: answer?.comment || ''
    }
  })
}

const getKeywords = (q) => {
  if (!q.keywords) return []
  return q.keywords.split(',').map(k => k.trim()).filter(k => k)
}

const getStudentAnswer = (questionId) => {
  const answer = answers.value.find(a => a.questionId === questionId)
  return answer?.answer || ''
}

const getMatchedKeywords = (questionId) => {
  const answer = answers.value.find(a => a.questionId === questionId)
  return answer?.matchedKeywords || 0
}

const getAutoScore = (questionId) => {
  const answer = answers.value.find(a => a.questionId === questionId)
  return answer?.autoScore || 0
}

const updateTotal = () => {
}

const handleSave = async () => {
  if (!canEdit.value) {
    ElMessage.info('该试卷已批改完成，无法修改')
    return
  }
  
  saving.value = true
  try {
    const gradesList = Object.entries(grades).map(([questionId, g]) => ({
      answerRecordId: g.answerRecordId,
      score: g.score,
      comment: g.comment
    })).filter(g => g.answerRecordId)
    
    await gradeEssay({
      recordId: recordId,
      grades: gradesList
    })
    
    ElMessage.success('保存成功')
    record.value.status = 2
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style lang="scss" scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.stats-card {
  display: flex;
  justify-content: space-around;
  padding: 24px;
  margin-bottom: 20px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: 600;
  color: #409eff;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 8px;
}

.question-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.question-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.question-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.question-num {
  font-weight: 600;
  font-size: 16px;
}

.question-score {
  color: #e6a23c;
  font-size: 14px;
}

.question-content {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  margin-bottom: 16px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
}

.section-title {
  font-weight: 600;
  color: #303133;
  margin: 16px 0 8px;
  font-size: 14px;
}

.reference-answer {
  padding: 12px;
  background: #f0f9ff;
  border-radius: 8px;
  line-height: 1.8;
  color: #606266;
}

.keywords {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.student-answer {
  padding: 12px;
  background: #fff7e6;
  border-radius: 8px;
  line-height: 1.8;
  color: #606266;
  min-height: 60px;
}

.auto-grade {
  display: flex;
  justify-content: space-between;
  padding: 12px;
  background: #f0fff4;
  border-radius: 8px;
}

.auto-score {
  font-weight: 600;
  color: #67c23a;
}

.grade-section {
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.grade-input {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.grade-label {
  font-weight: 600;
  color: #303133;
  min-width: 50px;
}

.score-max {
  color: #909399;
  margin-left: 8px;
}

.comment-input {
  display: flex;
  gap: 12px;
}
</style>
