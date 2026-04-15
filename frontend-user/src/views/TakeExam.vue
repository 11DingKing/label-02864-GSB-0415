<template>
  <div class="exam-container">
    <div class="exam-rules">
      <el-alert type="warning" :closable="false" show-icon>
        <template #title>
          <span class="rules-title">考试规则</span>
        </template>
        <div class="rules-content">
          <span>1. 考试开始后请勿刷新或离开页面，否则将自动提交试卷</span>
          <span>2. 考试时间结束后系统将自动提交</span>
          <span>3. 每场考试仅有一次答题机会，请认真作答</span>
        </div>
      </el-alert>
    </div>

    <div class="exam-header">
      <h2>{{ exam?.title }}</h2>
      <div class="timer" :class="{ warning: remainingTime < 300 }">
        <el-icon><Clock /></el-icon>
        <span>剩余时间: {{ formatTime(remainingTime) }}</span>
      </div>
    </div>

    <div class="exam-content">
      <div class="question-list">
        <div v-for="(q, index) in questions" :key="q.id" class="question-card">
          <div class="question-header">
            <span class="question-num">{{ index + 1 }}.</span>
            <el-tag size="small" :type="getTypeTag(q.type)">{{
              getTypeText(q.type)
            }}</el-tag>
            <span class="question-score">({{ q.score }}分)</span>
          </div>
          <div class="question-content">{{ q.content }}</div>
          <div class="question-options">
            <template v-if="q.type === 1 || q.type === 3">
              <el-radio-group v-model="answers[q.id]" class="options-group">
                <el-radio
                  v-for="opt in q.options"
                  :key="opt.id"
                  :value="opt.optionKey"
                  class="option-item"
                >
                  <span class="option-key">{{ opt.optionKey }}.</span>
                  <span>{{ opt.content }}</span>
                </el-radio>
              </el-radio-group>
            </template>
            <template v-else-if="q.type === 2">
              <el-checkbox-group
                v-model="multiAnswers[q.id]"
                class="options-group"
              >
                <el-checkbox
                  v-for="opt in q.options"
                  :key="opt.id"
                  :value="opt.optionKey"
                  class="option-item"
                >
                  <span class="option-key">{{ opt.optionKey }}.</span>
                  <span>{{ opt.content }}</span>
                </el-checkbox>
              </el-checkbox-group>
            </template>
            <template v-else-if="q.type === 4">
              <el-input
                v-model="answers[q.id]"
                type="textarea"
                :rows="6"
                placeholder="请输入您的答案"
                class="essay-input"
              />
            </template>
          </div>
        </div>
      </div>

      <div class="answer-sheet">
        <div class="sheet-title">答题卡</div>
        <div class="sheet-grid">
          <div
            v-for="(q, index) in questions"
            :key="q.id"
            :class="['sheet-item', { answered: isAnswered(q) }]"
            @click="scrollToQuestion(index)"
          >
            {{ index + 1 }}
          </div>
        </div>
        <div class="sheet-stats">
          已答: {{ answeredCount }} / {{ questions.length }}
        </div>
        <el-button
          type="primary"
          size="large"
          @click="handleSubmit"
          :loading="submitting"
        >
          提交试卷
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {
  ref,
  reactive,
  computed,
  onMounted,
  onUnmounted,
  onBeforeUnmount,
} from "vue";
import { useRoute, useRouter, onBeforeRouteLeave } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import {
  getExamById,
  getQuestionsByExamId,
  startExam,
  submitExam,
} from "../api";

const route = useRoute();
const router = useRouter();
const examId = route.params.id;

const exam = ref(null);
const questions = ref([]);
const recordId = ref(null);
const answers = reactive({});
const multiAnswers = reactive({});
const remainingTime = ref(0);
const submitting = ref(false);
let timer = null;

const answeredCount = computed(() => {
  return questions.value.filter((q) => isAnswered(q)).length;
});

const isAnswered = (q) => {
  if (q.type === 2) {
    return multiAnswers[q.id] && multiAnswers[q.id].length > 0;
  }
  return !!answers[q.id] && answers[q.id].trim() !== "";
};

const loadData = async () => {
  try {
    exam.value = await getExamById(examId);
    questions.value = await getQuestionsByExamId(examId);

    // 初始化多选答案
    questions.value.forEach((q) => {
      if (q.type === 2) {
        multiAnswers[q.id] = [];
      }
    });

    // 开始考试
    const record = await startExam(examId);
    recordId.value = record.id;

    // 设置倒计时
    remainingTime.value = exam.value.duration * 60;
    startTimer();
  } catch (e) {
    ElMessage.error("无法开始考试");
    router.push("/exam");
  }
};

const startTimer = () => {
  timer = setInterval(() => {
    remainingTime.value--;
    if (remainingTime.value <= 0) {
      clearInterval(timer);
      handleSubmit(true);
    }
  }, 1000);
};

const formatTime = (seconds) => {
  const m = Math.floor(seconds / 60);
  const s = seconds % 60;
  return `${m.toString().padStart(2, "0")}:${s.toString().padStart(2, "0")}`;
};

const handleSubmit = async (auto = false) => {
  if (!auto) {
    try {
      await ElMessageBox.confirm(
        `您已完成 ${answeredCount.value}/${questions.value.length} 道题，确定要提交吗？`,
        "提交确认",
      );
    } catch {
      return;
    }
  }

  submitting.value = true;
  clearInterval(timer);

  try {
    const answerList = questions.value.map((q) => ({
      questionId: q.id,
      answer:
        q.type === 2
          ? (multiAnswers[q.id] || []).sort().join(",")
          : answers[q.id] || "",
    }));

    const result = await submitExam({
      recordId: recordId.value,
      answers: answerList,
    });

    ElMessage.success(`提交成功！得分: ${result.totalScore}`);
    router.push("/records");
  } catch (e) {
    submitting.value = false;
    startTimer();
  }
};

const scrollToQuestion = (index) => {
  const el = document.querySelectorAll(".question-card")[index];
  if (el) {
    el.scrollIntoView({ behavior: "smooth", block: "center" });
  }
};

const getTypeText = (type) =>
  ({ 1: "单选", 2: "多选", 3: "判断", 4: "简答" })[type];
const getTypeTag = (type) =>
  ({ 1: "", 2: "warning", 3: "info", 4: "success" })[type];

// 页面离开时自动提交
onBeforeRouteLeave(async (to, from, next) => {
  if (!submitting.value && recordId.value) {
    try {
      await ElMessageBox.confirm(
        "离开页面将自动提交试卷，确定要离开吗？",
        "离开确认",
        { type: "warning" },
      );
      await handleSubmit(true);
    } catch {
      // 用户取消离开
      next(false);
      return;
    }
  }
  next();
});

// 浏览器刷新/关闭时提示
const handleBeforeUnload = (e) => {
  if (recordId.value && !submitting.value) {
    e.preventDefault();
    e.returnValue = "离开页面将自动提交试卷，确定要离开吗？";
    return e.returnValue;
  }
};

onMounted(() => {
  loadData();
  window.addEventListener("beforeunload", handleBeforeUnload);
});

onUnmounted(() => {
  if (timer) clearInterval(timer);
  window.removeEventListener("beforeunload", handleBeforeUnload);
});
</script>

<style lang="scss" scoped>
.exam-container {
  max-width: 1400px;
  margin: 0 auto;
}

.exam-rules {
  margin-bottom: 16px;

  .rules-title {
    font-weight: 600;
  }

  .rules-content {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    margin-top: 8px;
    font-size: 13px;

    span {
      color: #606266;
    }
  }
}

.exam-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #fff;
  padding: 16px 24px;
  border-radius: 12px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  h2 {
    font-size: 20px;
    color: #303133;
  }
}

.timer {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #409eff;

  &.warning {
    color: #f56c6c;
    animation: blink 1s infinite;
  }
}

@keyframes blink {
  50% {
    opacity: 0.5;
  }
}

.exam-content {
  display: flex;
  gap: 20px;
}

.question-list {
  flex: 1;
}

.essay-input {
  width: 100%;
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
  width: 100%;
}

.options-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%;
}

.options-group :deep(.el-radio),
.options-group :deep(.el-checkbox) {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
  margin-right: 0 !important;
  width: 100% !important;
  height: auto !important;
  box-sizing: border-box;
}

.options-group :deep(.el-radio:hover),
.options-group :deep(.el-checkbox:hover) {
  background: #ecf5ff;
}

.options-group :deep(.el-radio.is-checked),
.options-group :deep(.el-checkbox.is-checked) {
  background: #ecf5ff;
  border: 1px solid #409eff;
}

.options-group :deep(.el-radio__label),
.options-group :deep(.el-checkbox__label) {
  white-space: normal;
  line-height: 1.5;
}

.option-key {
  font-weight: 500;
  margin-right: 8px;
}

.answer-sheet {
  width: 200px;
  min-width: 200px;
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 84px;
  height: fit-content;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.sheet-title {
  font-weight: 600;
  margin-bottom: 16px;
  text-align: center;
}

.sheet-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
  margin-bottom: 16px;
  width: 100%;
  justify-items: center;
}

.sheet-item {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s;

  &:hover {
    border-color: #409eff;
  }

  &.answered {
    background: #409eff;
    border-color: #409eff;
    color: #fff;
  }
}

.sheet-stats {
  text-align: center;
  color: #606266;
  font-size: 14px;
  margin-bottom: 16px;
  padding: 8px 0;
  border-top: 1px solid #ebeef5;
  width: 100%;
}

.answer-sheet :deep(.el-button) {
  width: 100%;
}
</style>
