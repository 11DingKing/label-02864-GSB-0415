<template>
  <div class="layout">
    <header class="header">
      <div class="logo">
        <el-icon><Reading /></el-icon>
        <span>在线考试系统</span>
      </div>
      <nav class="nav">
        <router-link to="/exam" class="nav-item">考试列表</router-link>
        <router-link to="/records" class="nav-item">我的成绩</router-link>
      </nav>
      <div class="user-info">
        <el-icon><User /></el-icon>
        <span>{{ userStore.userInfo?.realName }}</span>
        <el-button type="text" @click="handleLogout">退出</el-button>
      </div>
    </header>
    <main class="main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'
import { logout } from '../api'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = async () => {
  await logout()
  userStore.clearUser()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style lang="scss" scoped>
.layout {
  min-height: 100vh;
}

.header {
  height: 60px;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  display: flex;
  align-items: center;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #11998e;
  
  .el-icon {
    font-size: 24px;
  }
}

.nav {
  display: flex;
  margin-left: 48px;
  gap: 32px;
}

.nav-item {
  color: #606266;
  text-decoration: none;
  font-size: 15px;
  padding: 8px 0;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
  
  &:hover, &.router-link-active {
    color: #11998e;
    border-bottom-color: #11998e;
  }
}

.user-info {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 8px;
  
  span {
    color: #606266;
  }
}

.main {
  min-height: calc(100vh - 60px);
  padding: 24px;
}
</style>
