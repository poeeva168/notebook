<template>
  <div class="note-editor">
    <el-card class="editor-header">
      <el-input
        v-model="noteTitle"
        placeholder="请输入笔记标题"
        class="title-input"
        size="large"
      />

      <div class="editor-toolbar">
        <el-button type="primary" @click="handleSave" :loading="saving">
          <el-icon><DocumentChecked /></el-icon>
          保存
        </el-button>
        <el-button @click="handleBack">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>
    </el-card>

    <el-card class="editor-meta">
      <el-form :inline="true">
        <el-form-item label="分类">
          <el-select v-model="noteCategoryId" placeholder="选择分类" clearable style="width: 200px;">
            <el-option
              v-for="category in categoryTree"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="标签">
          <el-select v-model="noteTagIds" multiple placeholder="选择标签" style="width: 300px;">
            <el-option
              v-for="tag in tags"
              :key="tag.id"
              :label="tag.name"
              :value="tag.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="editor-content">
      <div class="editor-wrapper">
        <div class="editor-pane">
          <div class="pane-header">编辑区</div>
          <el-input
            v-model="noteContent"
            type="textarea"
            placeholder="在这里编写 Markdown 内容..."
            class="content-textarea"
            :rows="25"
          />
        </div>

        <div class="preview-pane">
          <div class="pane-header">预览区</div>
          <div class="preview-content markdown-body" v-html="renderedContent"></div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DocumentChecked, ArrowLeft } from '@element-plus/icons-vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import { useNoteStore } from '@/stores/noteStore'
import { useCategoryStore } from '@/stores/categoryStore'
import { useTagStore } from '@/stores/tagStore'

const route = useRoute()
const router = useRouter()
const noteStore = useNoteStore()
const categoryStore = useCategoryStore()
const tagStore = useTagStore()

const noteId = ref(null)
const noteTitle = ref('')
const noteContent = ref('')
const noteCategoryId = ref(null)
const noteTagIds = ref([])
const saving = ref(false)

const categoryTree = ref([])
const tags = ref([])

const md = new MarkdownIt({
  html: false,
  linkify: true,
  typographer: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' +
               hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
               '</code></pre>'
      } catch (__) {}
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  }
})

const renderedContent = computed(() => {
  return md.render(noteContent.value || '')
})

const handleSave = async () => {
  if (!noteTitle.value) {
    ElMessage.warning('请输入笔记标题')
    return
  }

  saving.value = true
  try {
    const data = {
      title: noteTitle.value,
      content: noteContent.value,
      categoryId: noteCategoryId.value,
      tagIds: noteTagIds.value
    }

    if (noteId.value) {
      await noteStore.updateNote(noteId.value, data)
      ElMessage.success('保存成功')
    } else {
      const response = await noteStore.createNote(data)
      noteId.value = response.data.id
      ElMessage.success('创建成功')
    }
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleBack = () => {
  router.push('/notes')
}

const loadData = async () => {
  await Promise.all([
    categoryStore.fetchCategories(),
    tagStore.fetchTags()
  ])

  categoryTree.value = categoryStore.categories
  tags.value = tagStore.tags

  if (route.params.id && route.params.id !== 'new') {
    noteId.value = route.params.id
    try {
      await noteStore.fetchNoteById(noteId.value)
      const note = noteStore.currentNote

      noteTitle.value = note.title
      noteContent.value = note.content
      noteCategoryId.value = note.categoryId
      noteTagIds.value = note.tags ? note.tags.map(t => t.id) : []
    } catch (error) {
      ElMessage.error('加载笔记失败')
    }
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.note-editor {
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.editor-header :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-input :deep(.el-input__inner) {
  font-size: 18px;
  font-weight: 600;
  border: none;
  padding-left: 0;
}

.title-input :deep(.el-input__inner:focus) {
  border: none;
  box-shadow: none;
}

.editor-toolbar {
  display: flex;
  gap: 10px;
}

.editor-meta {
  flex-shrink: 0;
}

.editor-content {
  flex: 1;
  min-height: 0;
}

.editor-wrapper {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  height: 100%;
}

.editor-pane,
.preview-pane {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.pane-header {
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px 4px 0 0;
  font-weight: 600;
  color: #333;
}

.content-textarea {
  flex: 1;
}

.content-textarea :deep(.el-textarea__inner) {
  height: 100%;
  resize: none;
  border-radius: 0 0 4px 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.6;
}

.preview-content {
  flex: 1;
  overflow-y: auto;
  padding: 15px;
  background: white;
  border: 1px solid #e4e7ed;
  border-top: none;
  border-radius: 0 0 4px 4px;
}

.markdown-body {
  color: #333;
  line-height: 1.8;
}

.markdown-body :deep(h1) {
  border-bottom: 2px solid #e4e7ed;
  padding-bottom: 10px;
  margin-top: 20px;
}

.markdown-body :deep(h2) {
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 8px;
  margin-top: 18px;
}

.markdown-body :deep(code) {
  background: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
}

.markdown-body :deep(pre) {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  overflow-x: auto;
}

.markdown-body :deep(pre code) {
  background: transparent;
  padding: 0;
}

.markdown-body :deep(blockquote) {
  border-left: 4px solid #409eff;
  margin: 15px 0;
  padding: 10px 15px;
  background: #f5f7fa;
  color: #666;
}
</style>
