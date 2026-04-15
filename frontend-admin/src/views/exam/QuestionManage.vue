<template>
  <div class="page-container">
    <div class="page-header">
      <h2>题目管理 - {{ exam?.title }}</h2>
      <div>
        <el-button type="primary" @click="openDialog()">
          <el-icon><Plus /></el-icon>
          添加题目
        </el-button>
        <el-button @click="$router.back()">返回</el-button>
      </div>
    </div>

    <div class="card">
      <div v-if="questions.length === 0" class="empty-tip">
        暂无题目，请点击上方按钮添加
      </div>
      <div v-for="(q, index) in questions" :key="q.id" class="question-item">
        <div class="question-header">
          <span class="question-num">{{ index + 1 }}.</span>
          <el-tag size="small" :type="getTypeTag(q.type)">{{
            getTypeText(q.type)
          }}</el-tag>
          <span class="question-score">{{ q.score }}分</span>
          <div class="question-actions" v-if="exam?.status === 0">
            <el-button type="primary" link size="small" @click="openDialog(q)"
              >编辑</el-button
            >
            <el-button type="danger" link size="small" @click="handleDelete(q)"
              >删除</el-button
            >
          </div>
        </div>
        <div class="question-content">{{ q.content }}</div>
        <div class="question-options">
          <div
            v-for="opt in q.options"
            :key="opt.id"
            :class="['option-item', { correct: opt.isCorrect }]"
          >
            <span class="option-key">{{ opt.optionKey }}.</span>
            <span>{{ opt.content }}</span>
            <el-icon v-if="opt.isCorrect" class="correct-icon"
              ><Check
            /></el-icon>
          </div>
        </div>
      </div>
    </div>

    <el-dialog
      v-model="dialogVisible"
      :title="editingQuestion ? '编辑题目' : '添加题目'"
      width="600px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="题目类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio :value="1">单选题</el-radio>
            <el-radio :value="2">多选题</el-radio>
            <el-radio :value="3">判断题</el-radio>
            <el-radio :value="4">简答题</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题目内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="分值" prop="score">
          <el-input-number v-model="form.score" :min="1" :max="100" />
        </el-form-item>
        <!-- 简答题相关字段 -->
        <el-form-item
          v-if="form.type === 4"
          label="参考答案"
          prop="referenceAnswer"
        >
          <el-input
            v-model="form.referenceAnswer"
            type="textarea"
            :rows="4"
            placeholder="请输入参考答案"
          />
        </el-form-item>
        <el-form-item v-if="form.type === 4" label="关键词" prop="keywords">
          <el-input
            v-model="form.keywords"
            placeholder="多个关键词用逗号分隔，系统将根据关键词命中情况自动评分"
          />
        </el-form-item>
        <!-- 客观题选项 -->
        <el-form-item v-if="form.type !== 4" label="选项">
          <div class="options-editor">
            <div
              v-for="(opt, idx) in form.options"
              :key="idx"
              class="option-row"
            >
              <el-checkbox
                v-model="opt.isCorrectBool"
                :disabled="form.type === 1 && hasOtherCorrect(idx)"
              >
                正确
              </el-checkbox>
              <span class="opt-key">{{ opt.optionKey }}.</span>
              <el-input
                v-model="opt.content"
                placeholder="选项内容"
                style="flex: 1"
              />
              <el-button
                v-if="form.options.length > 2"
                type="danger"
                link
                @click="removeOption(idx)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </div>
            <el-button
              v-if="form.type !== 3 && form.options.length < 6"
              type="primary"
              link
              @click="addOption"
            >
              <el-icon><Plus /></el-icon> 添加选项
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave"
          >保存</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted } from "vue";
import { useRoute } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { getExamById } from "../../api/exam";
import {
  getQuestionsByExamId,
  createQuestion,
  updateQuestion,
  deleteQuestion,
} from "../../api/question";

const route = useRoute();
const examId = route.params.id;

const exam = ref(null);
const questions = ref([]);
const dialogVisible = ref(false);
const editingQuestion = ref(null);
const formRef = ref();
const saving = ref(false);

const defaultOptions = () => [
  {
    optionKey: "A",
    content: "",
    isCorrect: 0,
    isCorrectBool: false,
    sortOrder: 0,
  },
  {
    optionKey: "B",
    content: "",
    isCorrect: 0,
    isCorrectBool: false,
    sortOrder: 1,
  },
  {
    optionKey: "C",
    content: "",
    isCorrect: 0,
    isCorrectBool: false,
    sortOrder: 2,
  },
  {
    optionKey: "D",
    content: "",
    isCorrect: 0,
    isCorrectBool: false,
    sortOrder: 3,
  },
];

const judgmentOptions = () => [
  {
    optionKey: "A",
    content: "正确",
    isCorrect: 0,
    isCorrectBool: false,
    sortOrder: 0,
  },
  {
    optionKey: "B",
    content: "错误",
    isCorrect: 0,
    isCorrectBool: false,
    sortOrder: 1,
  },
];

const form = reactive({
  type: 1,
  content: "",
  referenceAnswer: "",
  keywords: "",
  score: 10,
  options: defaultOptions(),
});

const rules = {
  type: [{ required: true, message: "请选择题目类型" }],
  content: [{ required: true, message: "请输入题目内容", trigger: "blur" }],
  score: [{ required: true, message: "请输入分值" }],
};

watch(
  () => form.type,
  (val) => {
    if (val === 3) {
      form.options = judgmentOptions();
    } else if (val === 4) {
      form.options = [];
    } else if (
      form.options.length === 0 ||
      (form.options.length === 2 && form.options[0].content === "正确")
    ) {
      form.options = defaultOptions();
    }
  },
);

const loadData = async () => {
  exam.value = await getExamById(examId);
  questions.value = await getQuestionsByExamId(examId);
};

const openDialog = (q = null) => {
  editingQuestion.value = q;
  if (q) {
    form.type = q.type;
    form.content = q.content;
    form.referenceAnswer = q.referenceAnswer || "";
    form.keywords = q.keywords || "";
    form.score = q.score;
    form.options = q.options
      ? q.options.map((o) => ({
          ...o,
          isCorrectBool: o.isCorrect === 1,
        }))
      : [];
  } else {
    form.type = 1;
    form.content = "";
    form.referenceAnswer = "";
    form.keywords = "";
    form.score = 10;
    form.options = defaultOptions();
  }
  dialogVisible.value = true;
};

const hasOtherCorrect = (idx) => {
  return form.options.some((o, i) => i !== idx && o.isCorrectBool);
};

const addOption = () => {
  const keys = "ABCDEF";
  form.options.push({
    optionKey: keys[form.options.length],
    content: "",
    isCorrect: 0,
    isCorrectBool: false,
    sortOrder: form.options.length,
  });
};

const removeOption = (idx) => {
  form.options.splice(idx, 1);
  form.options.forEach((o, i) => {
    o.optionKey = "ABCDEF"[i];
    o.sortOrder = i;
  });
};

const handleSave = async () => {
  await formRef.value.validate();

  // 客观题需要选择正确答案
  if (form.type !== 4) {
    const hasCorrect = form.options.some((o) => o.isCorrectBool);
    if (!hasCorrect) {
      ElMessage.warning("请至少选择一个正确答案");
      return;
    }
  }

  saving.value = true;
  try {
    const data = {
      examId: Number(examId),
      type: form.type,
      content: form.content,
      score: form.score,
      referenceAnswer: form.type === 4 ? form.referenceAnswer : null,
      keywords: form.type === 4 ? form.keywords : null,
      options:
        form.type !== 4
          ? form.options.map((o) => ({
              ...o,
              isCorrect: o.isCorrectBool ? 1 : 0,
            }))
          : [],
    };

    if (editingQuestion.value) {
      await updateQuestion(editingQuestion.value.id, data);
      ElMessage.success("更新成功");
    } else {
      await createQuestion(data);
      ElMessage.success("添加成功");
    }
    dialogVisible.value = false;
    loadData();
  } finally {
    saving.value = false;
  }
};

const handleDelete = async (q) => {
  await ElMessageBox.confirm("确定要删除此题目吗？", "提示", {
    type: "warning",
  });
  await deleteQuestion(q.id);
  ElMessage.success("删除成功");
  loadData();
};

const getTypeText = (type) =>
  ({ 1: "单选", 2: "多选", 3: "判断", 4: "简答" })[type];
const getTypeTag = (type) =>
  ({ 1: "", 2: "warning", 3: "info", 4: "success" })[type];

onMounted(loadData);
</script>

<style lang="scss" scoped>
.empty-tip {
  text-align: center;
  padding: 40px;
  color: #909399;
}

.question-item {
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 16px;

  &:hover {
    border-color: #409eff;
  }
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

.question-actions {
  margin-left: auto;
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
}

.option-key {
  font-weight: 500;
}

.correct-icon {
  margin-left: auto;
}

.options-editor {
  width: 100%;
}

.option-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.opt-key {
  width: 20px;
  font-weight: 500;
}
</style>
