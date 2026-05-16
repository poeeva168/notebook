<template>
  <div class="note-list">
    <el-card class="action-bar">
      <div class="bar-left">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建笔记
        </el-button>
      </div>
      <div class="bar-right">
        <el-select v-model="queryParams.isArchived" placeholder="笔记状态" style="width: 150px; margin-right: 10px;" @change="handleQuery">
          <el-option label="全部笔记" :value="null" />
          <el-option label="正常笔记" :value="false" />
          <el-option label="已归档" :value="true" />
        </el-select>
        <el-select v-model="sortBy" placeholder="排序方式" style="width: 150px;" @change="handleQuery">
          <el-option label="按更新时间" value="updatedAt" />
          <el-option label="按创建时间" value="createdAt" />
          <el-option label="按标题" value="title" />
        </el-select>
      </div>
    </el-card>

    <div class="notes-container" v-loading="loading">
      <el-empty v-if="notes.length === 0" description="暂无笔记">
        <el-button type="primary" @click="handleCreate">创建第一篇笔记</el-button>
      </el-empty>

      <el-row :gutter="20" v-else>
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="note in notes" :key="note.id">
          <el-card class="note-card" shadow="hover" @click="handleEdit(note.id)">
            <template #header>
              <div class="note-header">
                <h3>{{ note.title }}</h3>
                <el-dropdown @command="handleCommand($event, note)">
                  <el-button text circle size="small">
                    <el-icon><MoreFilled /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="edit">编辑</el-dropdown-item>
                      <el-dropdown-item :command="note.isArchived ? 'unarchive' : 'archive'">
                        {{ note.isArchived ? '取消归档' : '归档' }}
                      </el-dropdown-item>
                      <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
            <div class="note-content">
              <p>{{ note.summary || '暂无摘要' }}</p>
            </div>
            <template #footer>
              <div class="note-footer">
                <span class="time">{{ formatTime(note.updatedAt) }}</span>
                <div class="tags">
                  <el-tag v-if="note.categoryName" size="small" type="info" style="margin-right: 5px;">
                    {{ note.categoryName }}
                  </el-tag>
                  <el-tag v-for="tag in note.tags" :key="tag.id" size="small" :color="tag.color" style="margin-right: 5px;">
                    {{ tag.name }}
                  </el-tag>
                </div>
              </div>
            </template>
          </el-card>
        </el-col>
      </el-row>

      <el-pagination
        v-if="pagination.total > 0"
        :current-page="pagination.page"
        :page-size="pagination.size"
        :total="pagination.total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
        style="margin-top: 20px; text-align: center;"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MoreFilled } from '@element-plus/icons-vue'
import { useNoteStore } from '@/stores/noteStore'

const router = useRouter()
const noteStore = useNoteStore()

const loading = ref(false)
const sortBy = ref('updatedAt')
const notes = ref([])
const pagination = ref({
  page: 1,
  size: 20,
  total: 0
})

const queryParams = reactive({
  isArchived: false
})

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString()
}

const handleQuery = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      isArchived: queryParams.isArchived
    }
    const response = await noteStore.fetchNotes(params)
    notes.value = response.data.records
    pagination.value.total = response.data.total
    pagination.value.page = response.data.page
  } catch (error) {
    ElMessage.error('加载笔记失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page) => {
  pagination.value.page = page
  handleQuery()
}

const handleCreate = () => {
  router.push('/notes/new')
}

const handleEdit = (id) => {
  router.push(`/notes/${id}`)
}

const handleCommand = async (command, note) => {
  switch (command) {
    case 'edit':
      handleEdit(note.id)
      break
    case 'archive':
      try {
        await noteStore.archiveNote(note.id, true)
        ElMessage.success('归档成功')
        handleQuery()
      } catch (error) {
        ElMessage.error('归档失败')
      }
      break
    case 'unarchive':
      try {
        await noteStore.archiveNote(note.id, false)
        ElMessage.success('取消归档成功')
        handleQuery()
      } catch (error) {
        ElMessage.error('取消归档失败')
      }
      break
    case 'delete':
      try {
        await ElMessageBox.confirm('确定要删除这篇笔记吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await noteStore.deleteNote(note.id)
        ElMessage.success('删除成功')
        handleQuery()
      } catch (error) {
        if (error !== 'cancel') {
          ElMessage.error('删除失败')
        }
      }
      break
  }
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.note-list {
  height: 100%;
}

.action-bar {
  margin-bottom: 20px;
}

.action-bar :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.note-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: transform 0.2s;
}

.note-card:hover {
  transform: translateY(-5px);
}

.note-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.note-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.note-content {
  height: 80px;
  overflow: hidden;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
}

.note-content p {
  margin: 0;
}

.note-footer {
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.time {
  font-size: 12px;
  color: #999;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
}

.notes-container {
  min-height: calc(100vh - 200px);
}
</style>
