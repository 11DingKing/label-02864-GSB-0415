<template>
  <div class="page-container">
    <div class="page-header">
      <h2>答卷详情 - {{ record?.realName }}</h2>
      <div>
        <el-tag :type="getStatusType(record)" size="large">{{
          getStatusText(record)
        }}</el-tag>
        <span class="total-score">总分: {{ record?.totalScore }}分</span>
        <el-button @click="$router.back()" style="margin-left: 16px"
          >返回</el-button
        >
      </div>
    </div>

    <div
      class="card question-card"
      v-for="(item, index) in questionWithAnswers"
      :key="item.question.id"
    >
      <div class="question-header">
        <span class="question-num">{{ index + 1 }}.</span>
        <el-tag size="small" :type="getTypeTag(item.question.type)">{{
          getTypeText(item.question.type)
        }}</el-tag>
        <span class="question-score">({{ item.question.score }}分)</span>
        <span class="user-score" v-if="item.answer">
          得分:
          <el-input-number
            v-if="item.question.type === 4 && record.status === 1"
            v-model="item.answer.score"
            :min="0"
            :max="item.question.score"
            size="small"
            @change="updateScore(item.answer.id, item.answer.score)"
          />
          <span v-else>{{ item.answer?.score || 0 }}</span>
        </span>
      </div>
      <div class="question-content">{{ item.question.content }}</div>

      <!-- 客观题选项 -->
      <div class="question-options" v-if="item.question.type !== 4">
        <div
          v-for="opt in item.question.options"
          :key="opt.id"
          :class="[
            'option-item',
            { correct: opt.isCorrect, selected: isSelected(opt, item.answer) },
          ]"
        >
          <span class="option-key">{{ opt.optionKey }}.</span>
          <span>{{ opt.content }}</span>
          <el-icon v-if="opt.isCorrect" class="correct-icon"><Check /></el-icon>
          <el-icon
            v-if="isSelected(opt, item.answer) && !opt.isCorrect"
            class="wrong-icon"
            ><Close
          /></el-icon>
        </div>
      </div>

      <!-- 简答题答案 -->
      <div v-if="item.question.type === 4" class="essay-container">
        <div class="answer-section">
          <div class="section-title">学生答案:</div>
          <div class="essay-content">{{ item.answer?.answer || "未作答" }}</div>
        </div>
        <div class="reference-section">
          <div class="section-title">参考答案:</div>
          <div class="essay-content">
            {{ item.question.referenceAnswer || "无" }}
          </div>
        </div>
        <div class="keywords-section">
          <div class="section-title">关键词:</div>
          <div class="keywords-list">
            <el-tag
              v-for="kw in item.question.keywords?.split(',')"
              :key="kw"
              size="small"
              type="info"
              >{{ kw.trim() }}</el-tag
            >
            <span v-if="!item.question.keywords">无</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from "vue";
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import { Check, Close } from "@element-plus/icons-vue";
import {
  getRecordDetail,
  getRecordAnswers,
  updateAnswerScore,
} from "../../api/record";
import { getQuestionsWithAnswer } from "../../api/question";

const route = useRoute();
const examId = route.params.id;
const recordId = route.params.recordId;

const record = ref(null);
const questions = ref([]);
const answers = ref([]);
const updating = ref(false);

const questionWithAnswers = computed(() => {
  return questions.value.map((q) => {
    const answer = answers.value.find((a) => a.questionId === q.id);
    return { question: q, answer };
  });
});

const loadData = async () => {
  const [recordRes, questionsRes, answersRes] = await Promise.all([
    getRecordDetail(recordId),
    getQuestionsWithAnswer(examId),
    getRecordAnswers(recordId),
  ]);
  record.value = recordRes;
  questions.value = questionsRes;
  answers.value = answersRes;
};

const isSelected = (opt, answer) => {
  if (!answer?.answer) return false;
  const selectedKeys = answer.answer.split(",");
  return selectedKeys.includes(opt.optionKey);
};

const updateScore = async (answerId, score) => {
  if (updating.value) return;
  updating.value = true;
  try {
    await updateAnswerScore(answerId, score);
    ElMessage.success("分数更新成功");
    // 重新加载记录信息更新总分
    record.value = await getRecordDetail(recordId);
  } finally {
    updating.value = false;
  }
};

const getStatusText = (row) => {
  if (row?.status === 1) return "待批改";
  if (row?.status === 2) return "已完成";
  return "进行中";
};

const getStatusType = (row) => {
  const text = getStatusText(row);
  if (text === "已完成") return "success";
  if (text === "待批改") return "warning";
  return "info";
};

const getTypeText = (type) =>
  ({ 1: "单选", 2: "多选", 3: "判断", 4: "简答" })[type];
const getTypeTag = (type) =>
  ({ 1: "", 2: "warning", 3: "info", 4: "success" })[type];

onMounted(loadData);
</script>

<style lang="scss" scoped>
.total-score {
  margin-left: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #e6a23c;
}

.question-card {
  margin-bottom: 20px;
  padding: 20px;
  border-radius: 8px;
  background: #fff;
}

.question-header {
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
  font-size: 12px;
}

.user-score {
  margin-left: auto;
  font-weight: 500;
}

.question-content {
  color: #303133;
  line-height: 1.6;
  margin-bottom: 12px;
}

.question-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;

  &.correct {
    background: #f0f9eb;
    color: #67c23a;
  }

  &.selected {
    border: 1px solid #409eff;
  }

  &.selected.wrong {
    background: #fef0f0;
    color: #f56c6c;
    border-color: #f56c6c;
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

.essay-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-top: 12px;
}

.section-title {
  font-weight: 600;
  margin-bottom: 8px;
  color: #303133;
}

.essay-content {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  min-height: 80px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.keywords-list {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}
</style>
