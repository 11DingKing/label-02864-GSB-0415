<template>
  <div class="page-container">
    <div class="page-header">
      <h2>批改简答题 - {{ studentName }}</h2>
      <div>
        <el-button @click="$router.back()">返回</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">
          保存评分
        </el-button>
      </div>
    </div>

    <div class="card">
      <div v-if="loading" class="loading-tip">加载中...</div>
      <div v-else-if="answers.length === 0" class="empty-tip">暂无简答题需要批改</div>
      <div v-for="(item, index) in answers" :key="item.id" class="answer-item">
        <div class="answer-header">
          <span class="question-num">{{ index + 1 }}.</span>
          <el-tag size="small" type="success">简答</el-tag>
          <span class="question-score">({{ item.question.score }}分)</span>
        </div>
        <div class="question-content">{{ item.question.content }}</div>
        
        <div class="section">
          <div class="section-title">学生答案</div>
          <div class="user-answer">{{ item.answer || '（未作答）' }}</div>
        </div>

        <div class="section">
          <div class="section-title">参考答案</div>
          <div class="reference-answer">{{ item.question.referenceAnswer }}</div>
        </div>

        <div class="section">
          <div class="section-title">
            <span>关键词</span>
            <span class="keywords-hit">
              命中: {{ getHitKeywords(item).length }}/{{ item.question.keywords.split(',').length }}
            </span>
          </div>
          <div class="keywords">
            <el-tag
              v-for="kw in item.question.keywords.split(',')"
              :key="kw"
              size="small"
              :type="isKeywordHit(item, kw.trim()) ? 'success' : 'info'"
            >
              {{ kw.trim() }}
            </el-tag>
          </div>
        </div>

        <div class="grading-section">
          <div class="grading-row">
            <div class="grading-item">
              <span class="grading-label">系统自动评分:</span>
              <span class="auto-score">{{ item.autoScore }} 分</span>
            </div>
            <div class="grading-item">
              <span class="grading-label">最终得分:</span>
              <el-input-number
                v-model="item.finalScore"
                :min="0"
                :max="item.question.score"
                :step="1"
                size="large"
              />
              <span class="score-max">/ {{ item.question.score }} 分</span>
            </div>
          </div>
          <div class="comment-section">
            <span class="grading-label">教师评语:</span>
            <el-input
              v-model="item.teacherComment"
              type="textarea"
              :rows="2"
              placeholder="请输入评语（可选）"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAnswersWithQuestions, gradeEssay } from '../../api/record'

const route = useRoute()
const router = useRouter()
const recordId = route.params.id

const loading = ref(false)
const saving = ref(false)
const answers = ref([])
const studentName = ref('')

const loadData = async () => {
  loading.value = true
  try {
    const data = await getAnswersWithQuestions(recordId)
    answers.value = data
      .filter(item => item.question.type === 4)
      .map(item => ({
        ...item,
        finalScore: item.finalScore ?? item.autoScore,
        teacherComment: item.teacherComment || ''
      }))
  } finally {
    loading.value = false
  }
}

const isKeywordHit = (item, keyword) => {
  if (!item.answer) return false
  return item.answer.toLowerCase().includes(keyword.toLowerCase())
}

const getHitKeywords = (item) => {
  if (!item.answer || !item.question.keywords) return []
  return item.question.keywords.split(',').filter(kw => 
    item.answer.toLowerCase().includes(kw.trim().toLowerCase())
  )
}

const handleSave = async () => {
  saving.value = true
  try {
    await gradeEssay({
      recordId: Number(recordId),
      grades: answers.value.map(item => ({
        answerRecordId: item.id,
        finalScore: item.finalScore,
        teacherComment: item.teacherComment
      }))
    })
    ElMessage.success('保存成功')
    router.back()
  } finally {
    saving.value = false
  }
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.loading-tip,
.empty-tip {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.answer-item {
  padding: 20px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 20px;

  &:last-child {
    margin-bottom: 0;
  }
}

.answer-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.question-num {
  font-weight: 600;
  color: #303133;
}

.question-score {
  color: #e6a23c;
  font-size: 14px;
}

.question-content {
  font-size: 15px;
  line-height: 1.6;
  color: #303133;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.section {
  margin-bottom: 16px;
}

.section-title {
  font-weight: 500;
  color: #606266;
  margin-bottom: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.keywords-hit {
  font-size: 12px;
  color: #909399;
}

.user-answer,
.reference-answer {
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 6px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.reference-answer {
  background: #f0f9eb;
  color: #67c23a;
}

.keywords {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.grading-section {
  background: #f9fafc;
  padding: 16px;
  border-radius: 8px;
  margin-top: 16px;
}

.grading-row {
  display: flex;
  align-items: center;
  gap: 40px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.grading-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.grading-label {
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
}

.auto-score {
  font-size: 18px;
  font-weight: 600;
  color: #909399;
}

.score-max {
  color: #909399;
  margin-left: 4px;
}

.comment-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
</style>
