<template>
  <el-container class="main-layout">
    <el-aside width="250px" class="sidebar">
      <div class="logo">
        <h3>📝 个人记事本</h3>
      </div>

      <el-menu
        :default-active="currentRoute"
        class="sidebar-menu"
        router
      >
        <el-menu-item index="/notes">
          <el-icon><Document /></el-icon>
          <span>全部笔记</span>
        </el-menu-item>

        <el-menu-item index="/categories">
          <el-icon><Folder /></el-icon>
          <span>分类管理</span>
        </el-menu-item>

        <el-menu-item index="/tags">
          <el-icon><Collection /></el-icon>
          <span>标签管理</span>
        </el-menu-item>

        <el-menu-item index="/archive">
          <el-icon><Box /></el-icon>
          <span>归档笔记</span>
        </el-menu-item>

        <el-menu-item index="/trash">
          <el-icon><Delete /></el-icon>
          <span>回收站</span>
        </el-menu-item>
      </el-menu>

      <div class="sidebar-footer">
        <div class="user-info" v-if="authStore.user">
          <el-avatar :size="32" :icon="UserFilled" />
          <span class="username">{{ authStore.user.username }}</span>
        </div>
        <el-button type="danger" size="small" @click="handleLogout">
          退出登录
        </el-button>
      </div>
    </el-aside>

    <el-container>
      <el-header height="60px" class="header">
        <div class="header-left">
          <h2>{{ pageTitle }}</h2>
        </div>

        <div class="header-right">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索笔记..."
            :prefix-icon="Search"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
          />
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Folder, Collection, Box, Delete, Search, UserFilled } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/authStore'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const searchKeyword = ref('')

const currentRoute = computed(() => route.path)

const pageTitle = computed(() => {
  const titles = {
    '/notes': '全部笔记',
    '/categories': '分类管理',
    '/tags': '标签管理',
    '/archive': '归档笔记',
    '/trash': '回收站'
  }
  return titles[route.path] || '笔记'
})

const handleSearch = () => {
  if (searchKeyword.value) {
    ElMessage.info(`搜索：${searchKeyword.value}`)
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    authStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 用户取消
  }
}
</script>

<style scoped>
.main-layout {
  height: 100vh;
}

.sidebar {
  background: #f5f7fa;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
}

.logo {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #e4e7ed;
}

.logo h3 {
  margin: 0;
  color: #333;
  font-size: 18px;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  background: transparent;
}

.sidebar-footer {
  padding: 20px;
  border-top: 1px solid #e4e7ed;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.username {
  font-size: 14px;
  color: #333;
}

.header {
  background: white;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header-left h2 {
  margin: 0;
  font-size: 20px;
  color: #333;
}

.header-right {
  display: flex;
  align-items: center;
}

.search-input {
  width: 300px;
}

.main-content {
  background: #f5f7fa;
  padding: 20px;
}
</style>
