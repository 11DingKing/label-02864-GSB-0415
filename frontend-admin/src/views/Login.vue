<template>
  <div class="login-container">
    <div class="login-bg">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>
    <div class="login-card">
      <div class="login-header">
        <div class="logo-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1>考试管理系统</h1>
        <p>教师端登录</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin" class="login-form">
        <el-form-item prop="username">
          <el-input v-model.trim="form.username" placeholder="请输入用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" native-type="submit" class="login-btn">
            <span v-if="!loading">登 录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-tip">
        <el-divider>测试账号</el-divider>
        <p><span class="label">用户名:</span> teacher <span class="label">密码:</span> 123456</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api/auth'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await login(form)
    if (res.role !== 1) {
      ElMessage.error('请使用教师账号登录')
      return
    }
    userStore.setUser(res, res.token)
    ElMessage.success('登录成功')
    router.push('/')
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  
  .shape {
    position: absolute;
    border-radius: 50%;
    filter: blur(60px);
    opacity: 0.5;
    animation: float 8s ease-in-out infinite;
  }
  
  .shape-1 {
    width: 400px;
    height: 400px;
    background: linear-gradient(135deg, #667eea, #764ba2);
    top: -100px;
    right: -100px;
    animation-delay: 0s;
  }
  
  .shape-2 {
    width: 300px;
    height: 300px;
    background: linear-gradient(135deg, #f093fb, #f5576c);
    bottom: -50px;
    left: -50px;
    animation-delay: 2s;
  }
  
  .shape-3 {
    width: 200px;
    height: 200px;
    background: linear-gradient(135deg, #4facfe, #00f2fe);
    top: 50%;
    left: 50%;
    animation-delay: 4s;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-20px) rotate(5deg); }
}

.login-card {
  width: 420px;
  padding: 48px 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  position: relative;
  z-index: 1;
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.login-header {
  text-align: center;
  margin-bottom: 36px;
  
  .logo-icon {
    width: 64px;
    height: 64px;
    margin: 0 auto 16px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
    
    svg {
      width: 32px;
      height: 32px;
      color: white;
    }
  }
  
  h1 {
    font-size: 26px;
    font-weight: 700;
    color: #1a1a2e;
    margin-bottom: 8px;
    letter-spacing: -0.5px;
  }
  
  p {
    color: #64748b;
    font-size: 15px;
    font-weight: 500;
  }
}

.login-form {
  :deep(.el-input__wrapper) {
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
    border: 2px solid #e2e8f0;
    transition: all 0.3s ease;
    
    &:hover {
      border-color: #cbd5e1;
    }
    
    &.is-focus {
      border-color: #667eea;
      box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
    }
  }
  
  :deep(.el-input__inner) {
    font-size: 15px;
  }
}

.login-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 15px 35px rgba(102, 126, 234, 0.5);
  }
  
  &:active {
    transform: translateY(0);
  }
}

.login-tip {
  margin-top: 24px;
  
  :deep(.el-divider__text) {
    background: rgba(255, 255, 255, 0.95);
    color: #94a3b8;
    font-size: 12px;
  }
  
  p {
    text-align: center;
    color: #64748b;
    font-size: 13px;
    background: #f8fafc;
    padding: 12px 16px;
    border-radius: 8px;
    
    .label {
      color: #94a3b8;
      margin-right: 4px;
    }
  }
}
</style>
