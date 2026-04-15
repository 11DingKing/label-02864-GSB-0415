<template>
  <div class="page-container">
    <div class="page-header">
      <h2>学生管理</h2>
      <el-button type="primary" @click="showDialog()">
        <el-icon><Plus /></el-icon>添加学生
      </el-button>
    </div>

    <el-table :data="studentList" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" min-width="150" />
      <el-table-column prop="realName" label="姓名" min-width="120" />
      <el-table-column prop="status" label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" min-width="180">
        <template #default="{ row }">
          {{ formatTime(row.createTime) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <div class="action-buttons">
            <el-button size="small" @click="showDialog(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @change="loadData"
      />
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑学生' : '添加学生'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!editingId" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item v-if="!editingId" label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserList, createUser, updateUser, deleteUser, resetPassword } from '../../api/user'

const loading = ref(false)
const studentList = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const editingId = ref(null)
const submitting = ref(false)
const formRef = ref(null)

const form = reactive({
  username: '',
  realName: '',
  password: '',
  status: 1
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await getUserList({ page: page.value, size: size.value, role: 0 })
    studentList.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

const showDialog = (row = null) => {
  editingId.value = row?.id || null
  form.username = row?.username || ''
  form.realName = row?.realName || ''
  form.password = ''
  form.status = row?.status ?? 1
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value.validate()
  submitting.value = true
  try {
    if (editingId.value) {
      await updateUser(editingId.value, { realName: form.realName, status: form.status })
      ElMessage.success('更新成功')
    } else {
      await createUser({ ...form, role: 0 })
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } finally {
    submitting.value = false
  }
}

const handleResetPwd = async (row) => {
  await ElMessageBox.confirm(`确定要重置 ${row.realName} 的密码为 123456 吗？`, '重置密码')
  await resetPassword(row.id)
  ElMessage.success('密码已重置为 123456')
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm(`确定要删除学生 ${row.realName} 吗？`, '删除确认')
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  loadData()
}

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

onMounted(loadData)
</script>

<style lang="scss" scoped>
.action-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: nowrap;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
