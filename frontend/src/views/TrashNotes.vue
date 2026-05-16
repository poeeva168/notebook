<template>
  <div class="trash-notes">
    <el-card class="action-bar">
      <div class="bar-left">
        <h3>回收站</h3>
      </div>
      <div class="bar-right">
        <el-button type="danger" @click="handleEmptyTrash">
          <el-icon><Delete /></el-icon>
          清空回收站
        </el-button>
      </div>
    </el-card>

    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <div v-else-if="filteredNotes.length === 0" class="empty-container">
      <el-empty description="回收站是空的" />
    </div>

    <div v-else class="notes-grid">
      <div v-for="note in filteredNotes" :key="note.id" class="note-card">
        <el-card class="note-content" shadow="hover">
          <div class="note-header">
            <h4 class="note-title">
              {{ note.title || '无标题' }}
            </h4>
            <el-dropdown @command="(command) => handleMenuCommand(command, note)">
              <el-button type="primary" link>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="restore">恢复</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>
                    <span style="color: #f56c6c">永久删除</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          <div class="note-preview">{{ note.summary || note.content?.substring(0, 100) || '无内容' }}</div>
          <div class="note-footer">
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
import { Loading, MoreFilled, Delete } from '@element-plus/icons-vue'
import { useNoteStore } from '@/stores/noteStore'

const noteStore = useNoteStore()

const loading = ref(false)

const filteredNotes = computed(() => {
  return noteStore.notes.filter(note => note.isDeleted)
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

const handleMenuCommand = async (command, note) => {
  if (command === 'restore') {
    try {
      await noteStore.restoreNote(note.id)
      ElMessage.success('已恢复')
      loadData()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  } else if (command === 'delete') {
    try {
      await ElMessageBox.confirm('确定要永久删除这个笔记吗？此操作不可恢复！', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await noteStore.deleteNotePermanently(note.id)
      ElMessage.success('已永久删除')
      loadData()
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }
}

const handleEmptyTrash = async () => {
  if (filteredNotes.value.length === 0) {
    ElMessage.warning('回收站是空的')
    return
  }
  try {
    await ElMessageBox.confirm('确定要清空回收站吗？所有笔记将被永久删除！', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    for (const note of filteredNotes.value) {
      await noteStore.deleteNotePermanently(note.id)
    }
    ElMessage.success('回收站已清空')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.trash-notes {
  height: 100%;
  overflow-y: auto;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  color: #666;
}

.note-preview {
  color: #888;
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
