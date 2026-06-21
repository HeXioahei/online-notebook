<template>
  <div class="note-detail-container">
    <header class="note-header">
      <div class="header-content">
        <button @click="goBack" class="btn btn-back">
          ← 返回笔记列表
        </button>
        <h1 v-if="loading">加载中...</h1>
        <h1 v-else>{{ note?.title }}</h1>
        <div class="header-actions">
          <button @click="editNote" class="btn btn-primary">
            ✎ 编辑
          </button>
          <button @click="deleteNote" class="btn btn-danger">
            ✕ 删除
          </button>
        </div>
      </div>
    </header>
    
    <main class="note-content" v-if="!loading && note">
      <div class="note-meta">
        <span class="note-path">{{ getDirectoryPath(note.categoryId) }}</span>
        <span class="note-date">{{ formatDate(note.updatedAt) }}</span>
      </div>
      
      <article class="markdown-body" v-html="renderMarkdown(note.content)"></article>
    </main>
    
    <div v-else-if="error" class="error-state">
      <p>😔 {{ error }}</p>
      <button @click="goBack" class="btn btn-primary">返回列表</button>
    </div>
    
    <div v-if="showConfirmModal" class="modal-overlay" @click="cancelConfirm">
      <div class="modal-content modal-confirm card" @click.stop>
        <header class="modal-header">
          <h2>{{ confirmTitle }}</h2>
          <button @click="cancelConfirm" class="close-btn">&times;</button>
        </header>
        
        <div class="confirm-body">
          <div class="confirm-icon">⚠️</div>
          <p class="confirm-message">{{ confirmMessage }}</p>
        </div>
        
        <footer class="modal-footer confirm-footer">
          <button @click="cancelConfirm" class="btn btn-cancel">
            取消
          </button>
          <button @click="confirmAction" class="btn btn-danger btn-confirm-yes">
            确定删除
          </button>
        </footer>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '../utils/api'
import { marked } from 'marked'

export default {
  name: 'NoteDetail',
  setup() {
    const router = useRouter()
    const route = useRoute()
    const note = ref(null)
    const loading = ref(true)
    const error = ref('')
    
    const showConfirmModal = ref(false)
    const confirmTitle = ref('')
    const confirmMessage = ref('')
    let confirmCallback = null
    
    const fetchNote = async () => {
      const noteId = route.params.id
      if (!noteId) {
        error.value = '无效的笔记ID'
        loading.value = false
        return
      }
      
      try {
        const userId = localStorage.getItem('userId')
        const response = await api.get(`/notes/user/${userId}`)
        
        if (response.data.success) {
          const foundNote = response.data.data.find(n => n.id === parseInt(noteId))
          
          if (foundNote) {
            note.value = foundNote
          } else {
            error.value = '笔记不存在或无权访问'
          }
        } else {
          error.value = response.data.message || '获取笔记失败'
        }
      } catch (err) {
        console.error('获取笔记详情失败:', err)
        error.value = err.response?.data?.message || '获取笔记失败，请稍后重试'
      } finally {
        loading.value = false
      }
    }
    
    const goBack = () => {
      router.push('/notes')
    }
    
    const editNote = () => {
      router.push({ name: 'NoteList', query: { action: 'edit', noteId: note.value.id } })
    }
    
    const showConfirm = (title, message, callback) => {
      confirmTitle.value = title
      confirmMessage.value = message
      confirmCallback = callback
      showConfirmModal.value = true
    }
    
    const cancelConfirm = () => {
      showConfirmModal.value = false
      confirmTitle.value = ''
      confirmMessage.value = ''
      confirmCallback = null
    }
    
    const confirmAction = () => {
      if (confirmCallback) {
        confirmCallback()
      }
      cancelConfirm()
    }
    
    const deleteNote = () => {
      showConfirm(
        '删除笔记',
        '确定要删除这篇笔记吗？此操作无法撤销。',
        async () => {
          const userId = localStorage.getItem('userId')
          
          try {
            const response = await api.delete(`/notes/${note.value.id}`, {
              params: { userId }
            })
            
            if (response.data.success) {
              router.push('/notes')
            } else {
              alert(response.data.message)
            }
          } catch (err) {
            alert('删除失败，请稍后重试')
          }
        }
      )
    }
    
    const getDirectoryPath = (categoryId) => {
      if (!categoryId) return '未分类'
      
      const categoriesStr = localStorage.getItem('categories')
      if (!categoriesStr) return ''
      
      let categories = []
      try {
        categories = JSON.parse(categoriesStr)
      } catch (e) {
        return ''
      }
      
      const category = categories.find(cat => cat.id === categoryId)
      if (!category) return ''
      
      const path = [category.name]
      let current = category
      
      while (current.parentId) {
        const parent = categories.find(cat => cat.id === current.parentId)
        if (parent) {
          path.unshift(parent.name)
          current = parent
        } else {
          break
        }
      }
      
      return path.join(' / ')
    }
    
    const renderMarkdown = (content) => {
      if (!content) return '<p class="empty-content">暂无内容</p>'
      try {
        return marked(content)
      } catch (err) {
        return content
      }
    }
    
    const formatDate = (dateString) => {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
    
    onMounted(() => {
      fetchNote()
    })
    
    return {
      note,
      loading,
      error,
      showConfirmModal,
      confirmTitle,
      confirmMessage,
      goBack,
      editNote,
      deleteNote,
      showConfirm,
      cancelConfirm,
      confirmAction,
      getDirectoryPath,
      renderMarkdown,
      formatDate
    }
  }
}
</script>

<style scoped>
.note-detail-container {
  min-height: 100vh;
  background: #f7fafc;
}

.note-header {
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 20px 0;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 30px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.note-header h1 {
  color: #667eea;
  font-size: 24px;
  flex: 1;
  margin: 0;
}

.btn-back {
  background: #e2e8f0;
  color: #4a5568;
  padding: 10px 20px;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-back:hover {
  background: #cbd5e0;
  transform: translateX(-3px);
}

.header-actions {
  display: flex;
  gap: 10px;
}

.note-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 40px 30px;
  background: white;
  margin-top: 30px;
  border-radius: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  min-height: calc(100vh - 200px);
}

.note-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e2e8f0;
}

.note-path {
  background: #ebf4ff;
  color: #667eea;
  padding: 6px 16px;
  border-radius: 12px;
  font-size: 13px;
  font-weight: 500;
}

.note-date {
  color: #a0aec0;
  font-size: 13px;
}

.markdown-body {
  color: #4a5568;
  line-height: 1.8;
  font-size: 16px;
}

.error-state {
  text-align: center;
  padding: 80px 20px;
  color: #718096;
}

.error-state p {
  font-size: 18px;
  margin-bottom: 20px;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3 {
  color: #2d3748;
  margin-top: 30px;
  margin-bottom: 15px;
}

.markdown-body h1 {
  font-size: 32px;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 10px;
}

.markdown-body h2 {
  font-size: 26px;
}

.markdown-body h3 {
  font-size: 22px;
}

.markdown-body p {
  margin-bottom: 18px;
}

.markdown-body code {
  background: #f7fafc;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
}

.markdown-body pre {
  background: #2d3748;
  color: #e2e8f0;
  padding: 20px;
  border-radius: 8px;
  overflow-x: auto;
  margin-bottom: 20px;
}

.markdown-body pre code {
  background: none;
  color: inherit;
  padding: 0;
}

.markdown-body ul,
.markdown-body ol {
  margin-bottom: 18px;
  padding-left: 35px;
}

.markdown-body li {
  margin-bottom: 8px;
}

.markdown-body blockquote {
  border-left: 4px solid #667eea;
  padding-left: 20px;
  margin: 20px 0;
  color: #718096;
  background: #f7fafc;
  padding: 15px 20px;
  border-radius: 5px;
}

.markdown-body a {
  color: #667eea;
  text-decoration: none;
}

.markdown-body a:hover {
  text-decoration: underline;
}

.markdown-body img {
  max-width: 100% !important;
  height: auto !important;
  display: block;
  border-radius: 8px;
  margin: 25px auto;
}

.markdown-body table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 20px;
}

.markdown-body th,
.markdown-body td {
  border: 1px solid #e2e8f0;
  padding: 12px;
  text-align: left;
}

.markdown-body th {
  background: #f7fafc;
  font-weight: 600;
}

@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }
  
  .note-header h1 {
    font-size: 20px;
  }
  
  .note-content {
    padding: 20px 15px;
    margin: 20px 15px;
  }
  
  .markdown-body {
    font-size: 14px;
  }
  
  .note-meta {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.modal-confirm {
  max-width: 480px;
  text-align: center;
}

.card {
  border: none;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #f7fafc;
}

.modal-header h2 {
  color: #2d3748;
  font-size: 22px;
  font-weight: 600;
  margin: 0;
}

.close-btn {
  background: #fed7d7;
  color: #c53030;
  border: none;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #fc8181;
  transform: rotate(90deg);
}

.confirm-body {
  padding: 30px 20px;
}

.confirm-icon {
  font-size: 48px;
  margin-bottom: 15px;
}

.confirm-message {
  color: #4a5568;
  font-size: 16px;
  line-height: 1.6;
  margin: 0;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.confirm-footer {
  justify-content: center;
  padding-top: 0;
}

.btn {
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5568d3;
  transform: translateY(-2px);
}

.btn-danger {
  background: #fc8181;
  color: white;
}

.btn-danger:hover {
  background: #f56565;
  transform: translateY(-2px);
}

.btn-cancel {
  background: #e2e8f0;
  color: #4a5568;
  min-width: 100px;
}

.btn-cancel:hover {
  background: #cbd5e0;
}

.btn-confirm-yes {
  min-width: 120px;
}
</style>