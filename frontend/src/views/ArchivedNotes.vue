<template>
  <div class="archived-notes">
    <el-card class="action-bar">
      <div class="bar-left">
        <h3>归档笔记</h3>
      </div>
    </el-card>

    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <div v-else-if="filteredNotes.length === 0" class="empty-container">
      <el-empty description="暂无归档笔记" />
    </div>

    <div v-else class="notes-grid">
      <div v-for="note in filteredNotes" :key="note.id" class="note-card">
        <el-card class="note-content" shadow="hover">
          <div class="note-header">
            <h4 class="note-title" @click="handleView(note)">
              {{ note.title || '无标题' }}
            </h4>
            <el-dropdown @command="(command) => handleMenuCommand(command, note)">
              <el-button type="primary" link>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="unarchive">取消归档</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <span style="color: #f56c6c">删除</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="note-preview">{{ note.summary || note.content?.substring(0, 100) || '无内容' }}</div>
          <div class="note-footer">
            <el-tag size="small" v-if="note.category">
              {{ note.category.name }}
            </el-tag>
            <span class="note-time">{{ formatTime(note.updatedAt) }}</span>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Loading, MoreFilled } from '@element-plus/icons-vue'
import { useNoteStore } from '@/stores/noteStore'
import { useRouter } from 'vue-router'

const router = useRouter()
const noteStore = useNoteStore()

const loading = ref(false)

const filteredNotes = computed(() => {
  return noteStore.notes.filter(note => note.isArchived)
})

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const loadData = async () => {
  loading.value = true
  try {
    await noteStore.fetchNotes()
  } catch (error) {
    ElMessage.error('加载笔记失败')
  } finally {
    loading.value = false
  }
}

const handleView = (note) => {
  router.push(`/notes/${note.id}`)
}

const handleMenuCommand = async (command, note) => {
  if (command === 'unarchive') {
    try {
      await noteStore.archiveNote(note.id, false)
      ElMessage.success('已取消归档')
      loadData()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  } else if (command === 'delete') {
    try {
      await ElMessageBox.confirm('确定要删除这个笔记吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await noteStore.deleteNote(note.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.archived-notes {
  height: 100%;
  overflow-y: auto;
}

.action-bar {
  margin-bottom: 20px;
}

.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 0;
}

.notes-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.note-card {
  cursor: pointer;
}

.note-content {
  height: 100%;
}

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 10px;
}

.note-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.note-preview {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  max-height: 60px;
  overflow: hidden;
  margin-bottom: 12px;
}

.note-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.note-time {
  color: #999;
  font-size: 12px;
}
</style>
