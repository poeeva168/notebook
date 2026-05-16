<template>
  <div class="category-list">
    <el-card class="action-bar">
      <div class="bar-left">
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建分类
        </el-button>
      </div>
    </el-card>

    <el-table :data="categories" v-loading="loading" stripe style="width: 100%">
      <el-table-column prop="name" label="分类名称" width="200" />
      <el-table-column prop="sortOrder" label="排序" width="100" />
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
        <el-form-item label="分类名称">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
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
import { useCategoryStore } from '@/stores/categoryStore'

const categoryStore = useCategoryStore()

const loading = ref(false)
const categories = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = reactive({
  id: null,
  name: '',
  sortOrder: 0
})

const loadData = async () => {
  loading.value = true
  try {
    await categoryStore.fetchCategories()
    categories.value = categoryStore.categories
  } catch (error) {
    ElMessage.error('加载分类失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = () => {
  dialogTitle.value = '新建分类'
  form.id = null
  form.name = ''
  form.sortOrder = 0
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑分类'
  form.id = row.id
  form.name = row.name
  form.sortOrder = row.sortOrder
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    if (form.id) {
      await categoryStore.updateCategory(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await categoryStore.createCategory(form)
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
    await ElMessageBox.confirm('确定要删除这个分类吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await categoryStore.deleteCategory(row.id)
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
.category-list {
  height: 100%;
}

.action-bar {
  margin-bottom: 20px;
}
</style>
