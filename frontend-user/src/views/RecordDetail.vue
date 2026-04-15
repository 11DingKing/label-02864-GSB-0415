<template>
  <div class="page-container">
    <div class="page-header">
      <h2>答卷详情</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>
    
    <div class="score-card">
      <div class="score-main">
        <div class="score-value" :class="record?.totalScore >= 60 ? 'pass' : 'fail'">
          {{ record?.totalScore }}
        </div>
        <div class="score-label">我的得分</div>
      </div>
      <div class="score-info">
        <div class="info-row">
          <span>考试名称:</span>
          <span>{{ exam?.title }}</span>
        </div>
        <div class="info-row">
          <span>提交时间:</span>
          <span>{{ formatTime(record?.submitTime) }}</span>
        </div>
        <div class="info-row">
          <span>是否及格:</span>
          <el-tag :type="record?.totalScore >= 60 ? 'success' : 'danger'">
            {{ record?.totalScore >= 60 ? '及格' : '不及格' }}
          </el-tag>
        </div>
      </div>
    </div>
    
    <div class="questions-review">
      <div v-for="(q, index) in questions" :key="q.id" class="question-card">
        <div class="question-header">
          <span class="question-num">{{ index + 1 }}.</span>
          <el-tag size="small" :type="getTypeTag(q.type)">{{ getTypeText(q.type) }}</el-tag>
          <span class="question-score">({{ q.score }}分)</span>
          <el-tag 
            :type="getAnswerResult(q.id) ? 'success' : 'danger'" 
            size="small"
            style="margin-left: auto;"
          >
            {{ getAnswerResult(q.id) ? '正确' : '错误' }}
            +{{ getAnswerScore(q.id) }}分
          </el-tag>
        </div>
        <div class="question-content">{{ q.content }}</div>
        <div class="question-options">
          <div 
            v-for="opt in q.options" 
            :key="opt.id" 
            :class="['option-item', getOptionClass(q.id, opt)]"
          >
            <span class="option-key">{{ opt.optionKey }}.</span>
            <span>{{ opt.content }}</span>
            <el-icon v-if="opt.isCorrect" class="correct-icon"><Check /></el-icon>
            <el-icon v-if="isUserAnswer(q.id, opt.optionKey) && !opt.isCorrect" class="wrong-icon"><Close /></el-icon>
          </div>
        </div>
        <div class="answer-info">
          <span>我的答案: {{ getUserAnswer(q.id) || '未作答' }}</span>
          <span>正确答案: {{ getCorrectAnswer(q) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getRecordDetail, getAnswers, getExamById, getQuestionsWithAnswer } from '../api'

const route = useRoute()
const recordId = route.params.id

const record = ref(null)
const exam = ref(null)
const questions = ref([])
const answerMap = ref({})

const loadData = async () => {
  record.value = await getRecordDetail(recordId)
  exam.value = await getExamById(record.value.examId)
  questions.value = await getQuestionsWithAnswer(record.value.examId)
  
  const answers = await getAnswers(recordId)
  answerMap.value = {}
  answers.forEach(a => {
    answerMap.value[a.questionId] = a
  })
}

const getUserAnswer = (qId) => answerMap.value[qId]?.answer || ''
const getAnswerResult = (qId) => answerMap.value[qId]?.isCorrect === 1
const getAnswerScore = (qId) => answerMap.value[qId]?.score || 0

const isUserAnswer = (qId, optKey) => {
  const answer = getUserAnswer(qId)
  return answer.split(',').includes(optKey)
}

const getCorrectAnswer = (q) => {
  return q.options.filter(o => o.isCorrect).map(o => o.optionKey).join(',')
}

const getOptionClass = (qId, opt) => {
  const classes = []
  if (opt.isCorrect) classes.push('correct')
  if (isUserAnswer(qId, opt.optionKey)) {
    classes.push(opt.isCorrect ? 'user-correct' : 'user-wrong')
  }
  return classes
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

const getTypeText = (type) => ({ 1: '单选', 2: '多选', 3: '判断' }[type])
const getTypeTag = (type) => ({ 1: '', 2: 'warning', 3: 'info' }[type])

onMounted(loadData)
</script>

<style lang="scss" scoped>
.score-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  display: flex;
  align-items: center;
  gap: 48px;
  margin-bottom: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.score-main {
  text-align: center;
  padding: 0 32px;
  border-right: 1px solid #ebeef5;
}

.score-value {
  font-size: 56px;
  font-weight: 700;
  
  &.pass { color: #67c23a; }
  &.fail { color: #f56c6c; }
}

.score-label {
  color: #909399;
  margin-top: 8px;
}

.score-info {
  flex: 1;
}

.info-row {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  
  span:first-child {
    color: #909399;
    width: 80px;
  }
}

.question-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 16px;
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
}

.question-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  border: 2px solid transparent;
  
  &.correct {
    background: #f0f9eb;
  }
  
  &.user-correct {
    border-color: #67c23a;
  }
  
  &.user-wrong {
    border-color: #f56c6c;
    background: #fef0f0;
  }
}

.option-key {
  font-weight: 500;
}

.correct-icon {
  margin-left: auto;
  color: #67c23a;
}

.wrong-icon {
  margin-left: auto;
  color: #f56c6c;
}

.answer-info {
  display: flex;
  gap: 32px;
  padding-top: 12px;
  border-top: 1px dashed #ebeef5;
  font-size: 14px;
  color: #606266;
}
</style>
