<template>
  <div class="page-container">
    <div class="page-header">
      <h2>成绩查看 - {{ exam?.title }}</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>

    <div class="card stats-card">
      <div class="stat-item">
        <div class="stat-value">{{ stats.total }}</div>
        <div class="stat-label">参考人数</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.submittedCount }}</div>
        <div class="stat-label">已提交</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.passRate }}%</div>
        <div class="stat-label">及格率</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.avgScore }}</div>
        <div class="stat-label">平均分</div>
      </div>
      <div class="stat-item">
        <div class="stat-value">{{ stats.maxScore }}</div>
        <div class="stat-label">最高分</div>
      </div>
    </div>

    <div class="card">
      <el-table :data="records" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="realName" label="学生姓名" min-width="120" />
        <el-table-column prop="userName" label="用户名" min-width="120" />
        <el-table-column label="得分" min-width="100" align="center">
          <template #default="{ row }">
            <span
              :class="
                row.totalScore >= (exam?.passScore || 60)
                  ? 'score-pass'
                  : 'score-fail'
              "
            >
              {{ row.totalScore }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="是否及格" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag
              :type="
                row.totalScore >= (exam?.passScore || 60) ? 'success' : 'danger'
              "
            >
              {{
                row.totalScore >= (exam?.passScore || 60) ? "及格" : "不及格"
              }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开始时间" min-width="180">
          <template #default="{ row }">{{
            formatTime(row.startTime)
          }}</template>
        </el-table-column>
        <el-table-column label="提交时间" min-width="180">
          <template #default="{ row }">{{
            formatTime(row.submitTime)
          }}</template>
        </el-table-column>
        <el-table-column label="状态" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row)">
              {{ getStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="120" align="center">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              @click="goToDetail(row.id)"
            >
              查看详情
            </el-button>
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
import { ref, reactive, onMounted } from "vue";
import { useRoute, useRouter } from "vue-router";
import { getExamById } from "../../api/exam";
import { getExamRecords, getExamStats } from "../../api/record";

const route = useRoute();
const router = useRouter();
const examId = route.params.id;

const exam = ref(null);
const records = ref([]);
const loading = ref(false);
const pagination = reactive({ page: 1, size: 10, total: 0 });
const stats = ref({
  total: 0,
  submittedCount: 0,
  passRate: 0,
  avgScore: 0,
  maxScore: 0,
});

const loadStats = async () => {
  try {
    const res = await getExamStats(examId);
    const total = res.total || 0;
    const submittedCount = res.submitted_count || 0;
    const passCount = res.pass_count || 0;
    stats.value = {
      total,
      submittedCount,
      passRate:
        submittedCount > 0 ? Math.round((passCount / submittedCount) * 100) : 0,
      avgScore: res.avg_score || 0,
      maxScore: res.max_score || 0,
    };
  } catch (e) {
    console.error("Failed to load stats", e);
  }
};

const loadRecords = async () => {
  loading.value = true;
  try {
    const res = await getExamRecords(examId, {
      page: pagination.page,
      size: pagination.size,
    });
    records.value = res.records;
    pagination.total = res.total;
  } finally {
    loading.value = false;
  }
};

const formatTime = (time) => {
  if (!time) return "-";
  return time.replace("T", " ").substring(0, 19);
};

const getStatusText = (row) => {
  if (row.status === 1) return "待批改";
  if (row.status === 2) return "已完成";
  // 检查是否超过考试时间
  if (exam.value?.endTime) {
    const endTime = new Date(exam.value.endTime);
    if (new Date() > endTime) return "未提交";
  }
  // 检查是否超过考试时长
  if (row.startTime && exam.value?.duration) {
    const startTime = new Date(row.startTime);
    const deadline = new Date(
      startTime.getTime() + exam.value.duration * 60 * 1000,
    );
    if (new Date() > deadline) return "未提交";
  }
  return "进行中";
};

const getStatusType = (row) => {
  const text = getStatusText(row);
  if (text === "已完成") return "success";
  if (text === "待批改") return "warning";
  if (text === "未提交") return "danger";
  return "info";
};

const goToDetail = (recordId) => {
  router.push(`/exam/${examId}/record/${recordId}`);
};

onMounted(async () => {
  exam.value = await getExamById(examId);
  await Promise.all([loadStats(), loadRecords()]);
});
</script>

<style lang="scss" scoped>
.stats-card {
  display: flex;
  justify-content: space-around;
  padding: 24px;
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

.pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
