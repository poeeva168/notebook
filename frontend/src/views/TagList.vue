<template>
  <div class="tag-list">
    <el-card class="action-bar">
      <div class="bar-left">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建标签
        </el-button>
      </div>
    </el-card>

    <el-table :data="tags" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="name" label="标签名称" width="200" />
      <el-table-column label="颜色" width="150">
        <template #default="scope">
          <el-tag :color="scope.row.color">{{ scope.row.name }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" />
      <el-table-column label="操作" width="200">
        <template #default="scope">
          <el-button type="primary" link @click="handleEdit(scope.row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标签名称">
          <el-input v-model="form.name" placeholder="请输入标签名称" />
        </el-form-item>
        <el-form-item label="颜色">
          <el-color-picker v-model="form.color" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useTagStore } from '@/stores/tagStore'

const tagStore = useTagStore()

const loading = ref(false)
const tags = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = reactive({
  id: null,
  name: '',
  color: '#409eff'
})

const loadData = async () => {
  loading.value = true
  try {
    await tagStore.fetchTags()
    tags.value = tagStore.tags
  } catch (error) {
    ElMessage.error('加载标签失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  dialogTitle.value = '新建标签'
  form.id = null
  form.name = ''
  form.color = '#409eff'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑标签'
  form.id = row.id
  form.name = row.name
  form.color = row.color
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (form.id) {
      await tagStore.updateTag(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await tagStore.createTag(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm('确定要删除这个标签吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await tagStore.deleteTag(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.tag-list {
  height: 100%;
}

.action-bar {
  margin-bottom: 20px;
}
</style>
