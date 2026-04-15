<template>
  <div class="page-container">
    <div class="page-header">
      <h2>{{ isEdit ? '编辑考试' : '创建考试' }}</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>
    
    <div class="card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width: 600px">
        <el-form-item label="考试名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入考试名称" />
        </el-form-item>
        <el-form-item label="考试描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入考试描述" />
        </el-form-item>
        <el-form-item label="考试时长" prop="duration">
          <el-input-number v-model="form.duration" :min="1" :max="300" />
          <span style="margin-left: 8px; color: #909399">分钟</span>
        </el-form-item>
        <el-form-item label="及格分数" prop="passScore">
          <el-input-number v-model="form.passScore" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">保存</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getExamById, createExam, updateExam } from '../../api/exam'

const route = useRoute()
const router = useRouter()
const formRef = ref()
const loading = ref(false)

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  description: '',
  duration: 60,
  passScore: 60,
  startTime: '',
  endTime: ''
})

const rules = {
  title: [{ required: true, message: '请输入考试名称', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入考试时长', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const loadExam = async () => {
  if (!isEdit.value) return
  const res = await getExamById(route.params.id)
  Object.assign(form, res)
}

const handleSubmit = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    if (isEdit.value) {
      await updateExam(route.params.id, form)
      ElMessage.success('更新成功')
    } else {
      await createExam(form)
      ElMessage.success('创建成功')
    }
    router.push('/exam')
  } finally {
    loading.value = false
  }
}

onMounted(loadExam)
</script>
