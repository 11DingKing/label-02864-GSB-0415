<template>
  <div class="layout">
    <header class="header">
      <div class="logo">
        <el-icon><Reading /></el-icon>
        <span>考试管理系统</span>
      </div>
      <nav class="nav-menu">
        <router-link to="/exam" class="nav-item" :class="{ active: $route.path.startsWith('/exam') }">
          <el-icon><Document /></el-icon>考试管理
        </router-link>
        <router-link to="/student" class="nav-item" :class="{ active: $route.path === '/student' }">
          <el-icon><User /></el-icon>学生管理
        </router-link>
      </nav>
      <div class="user-info">
        <span>{{ userStore.userInfo?.realName }}</span>
        <el-button type="text" @click="handleLogout">退出登录</el-button>
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
import { logout } from '../api/auth'

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
  justify-content: space-between;
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
  color: #409eff;
  
  .el-icon {
    font-size: 24px;
  }
}

.nav-menu {
  display: flex;
  gap: 8px;
  margin-left: 48px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 16px;
  border-radius: 6px;
  color: #606266;
  text-decoration: none;
  transition: all 0.3s;
  
  &:hover {
    background: #f5f7fa;
    color: #409eff;
  }
  
  &.active {
    background: #ecf5ff;
    color: #409eff;
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
  
  span {
    color: #606266;
  }
}

.main {
  min-height: calc(100vh - 60px);
}
</style>
