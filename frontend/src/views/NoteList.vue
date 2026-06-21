<template>
  <div class="notes-container">
    <header class="notes-header">
      <div class="header-content">
        <h1>我的笔记本</h1>
        <div class="user-info">
          <span>欢迎，{{ username }}</span>
          <button @click="handleLogout" class="btn btn-danger btn-sm">退出</button>
        </div>
      </div>
    </header>
    
    <div class="notes-body">
      <aside class="sidebar">
        <div class="sidebar-header">
          <h3>📂 文件浏览器</h3>
          <div class="header-actions">
            <button @click="showCategoryModal(null)" class="btn btn-primary btn-sm">📁 新建根目录</button>
            <button @click="showCreateModal" class="btn btn-success btn-sm">✏️ 新建笔记</button>
          </div>
        </div>
        
        <div class="global-search">
          <div class="search-input-group">
            <input
              v-model="globalSearchKeyword"
              type="text"
              class="input global-search-input"
              placeholder="🔍 全局搜索笔记..."
              @keyup.enter="performGlobalSearch"
            />
            <button @click="performGlobalSearch" class="btn btn-primary btn-search">
              搜索
            </button>
          </div>
        </div>
        
        <nav class="category-tree">
          <DirectoryTree
            :items="rootItems"
            :level="0"
            :selectedCategoryId="selectedCategoryId"
            :currentNoteId="currentNoteId"
            :expandedDirectories="expandedDirectories"
            :hasChildren="hasChildren"
            :getChildrenItems="getChildrenItems"
            @toggle-directory="toggleDirectory"
            @enter-directory="enterDirectory"
            @show-sub-directory-modal="showSubDirectoryModal"
            @add-note-to-directory="addNoteToDirectory"
            @edit-directory="editDirectory"
            @delete-directory="deleteDirectory"
            @view-note-by-id="viewNoteById"
            @edit-note-by-id="editNoteById"
            @delete-note="deleteNote"
          />
        </nav>
      </aside>
      
      <main class="notes-main">
        <section v-if="!selectedCategoryId" class="welcome-section">
          <div class="welcome-content card">
            <h2>👋 欢迎使用在线笔记本</h2>
            <p>这是一个文件管理器式的笔记本系统</p>
            <div class="welcome-features">
              <div class="feature">
                <span class="feature-icon">📁</span>
                <span>多级目录管理</span>
              </div>
              <div class="feature">
                <span class="feature-icon">📄</span>
                <span>Markdown 笔记</span>
              </div>
              <div class="feature">
                <span class="feature-icon">🔍</span>
                <span>混合浏览模式</span>
              </div>
            </div>
          </div>
        </section>
        
        <template v-else>
          <section class="content-toolbar">
            <div class="toolbar-left">
              <button 
                v-if="getParentDirectory(selectedCategoryId)" 
                @click="goBackToParent()" 
                class="btn btn-back"
                title="返回上级目录"
              >
                ← 返回上级
              </button>
              
              <h3 class="current-directory-title">{{ getDirectoryName(selectedCategoryId) }}</h3>
              <span class="item-count">{{ currentItems.length }} 个项目</span>
            </div>
            
            <div class="toolbar-right">
              <select v-model="sortBy" class="input sort-select">
                <option value="updatedAt-desc">修改时间 ↓</option>
                <option value="updatedAt-asc">修改时间 ↑</option>
                <option value="name-asc">名称 A-Z</option>
                <option value="name-desc">名称 Z-A</option>
              </select>
              <button @click="showCreateModal" class="btn btn-primary btn-sm">
                ✏️ 新建笔记
              </button>
              <button @click="showSubDirectoryModal(getCurrentDirectory())" class="btn btn-success btn-sm">
                📁 新建子目录
              </button>
            </div>
          </section>
          
          <section class="content-list">
            <div v-if="loading" class="loading">加载中...</div>
            
            <div v-else-if="currentItems.length === 0" class="empty-state">
              <p>该目录下还没有内容</p>
              <div class="empty-actions">
                <button @click="showCreateModal" class="btn btn-primary">创建第一篇笔记</button>
                <button @click="showSubDirectoryModal(getCurrentDirectory())" class="btn btn-success">创建子目录</button>
              </div>
            </div>
            
            <div v-else class="items-grid">
              <article
                v-for="item in sortedItems"
                :key="item.id"
                class="item-card card"
                :class="{
                  'directory-card': item.type === 'directory',
                  'note-card': item.type === 'note'
                }"
                @click="handleItemClick(item)"
              >
                <header class="item-header">
                  <div class="item-type-icon">
                    <span v-if="item.type === 'directory'" class="icon-large">📁</span>
                    <span v-else class="icon-large">📄</span>
                  </div>
                  <h3>{{ item.type === 'directory' ? item.name : item.title }}</h3>
                </header>
                
                <div class="item-body">
                  <p v-if="item.type === 'directory'" class="item-description">
                    {{ getChildCount(item.id) }} 个子项目
                  </p>
                  <div v-else class="markdown-preview" v-html="truncateMarkdown(item.content)"></div>
                </div>
                
                <footer class="item-footer">
                  <span class="item-date">{{ formatDate(item.updatedAt) }}</span>
                  <div class="item-badge">
                    <span v-if="item.type === 'directory'" class="badge badge-directory">目录</span>
                    <span v-else class="badge badge-note">笔记</span>
                  </div>
                </footer>
              </article>
            </div>
          </section>
        </template>
      </main>
    </div>
    
    <div v-if="showNoteModal" class="modal-overlay" @click="closeNoteModal">
      <div class="modal-content modal-large card" @click.stop>
        <header class="modal-header">
          <h2>{{ isEditing ? '编辑笔记' : '新建笔记' }}</h2>
          <button @click="closeNoteModal" class="close-btn">&times;</button>
        </header>
        
        <form @submit.prevent="saveNote" class="modal-form">
          <section class="form-group">
            <label for="noteTitle">标题</label>
            <input
              id="noteTitle"
              v-model="noteForm.title"
              type="text"
              class="input"
              placeholder="请输入笔记标题"
              required
            />
          </section>
          
          <section class="form-group">
            <label for="noteDirectory">所属目录</label>
            <select id="noteDirectory" v-model="noteForm.categoryId" class="input">
              <option v-for="cat in flatCategories" :key="cat.id" :value="cat.id">
                {{ getDirectoryPath(cat.id) }}
              </option>
            </select>
          </section>
          
          <section class="form-group markdown-editor-group">
            <label>内容（支持 Markdown）</label>
            
            <div class="markdown-container split-view">
              <textarea
                v-model="noteForm.content"
                class="markdown-input"
                placeholder="支持 Markdown 格式编写..."
                rows="25"
              ></textarea>
              
              <div class="markdown-preview preview-panel" v-html="renderMarkdown(noteForm.content)"></div>
            </div>
          </section>
          
          <div v-if="modalError" class="error-message">{{ modalError }}</div>
          
          <footer class="modal-footer">
            <button type="button" @click="closeNoteModal" class="btn">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="modalLoading">
              {{ modalLoading ? '保存中...' : '保存' }}
            </button>
          </footer>
        </form>
      </div>
    </div>
    
    <div v-if="showGlobalSearchModal" class="modal-overlay" @click="closeGlobalSearchModal">
      <div class="modal-content modal-extra-large card" @click.stop>
        <header class="modal-header">
          <h2>🔍 全局搜索结果</h2>
          <button @click="closeGlobalSearchModal" class="close-btn">&times;</button>
        </header>
        
        <div class="search-info">
          搜索关键词: "<strong>{{ globalSearchKeyword }}</strong>" - 找到 {{ globalSearchResults.length }} 个结果
        </div>
        
        <div class="search-results">
          <div v-if="globalSearchResults.length === 0" class="no-results">
            <p>😔 未找到匹配的笔记</p>
            <p class="hint">请尝试其他关键词</p>
          </div>
          
          <div v-else class="results-grid">
            <article
              v-for="note in globalSearchResults"
              :key="note.id"
              class="result-card card"
              @click="openNoteInNewTab(note)"
            >
              <header class="result-header">
                <h3>{{ note.title }}</h3>
                <span class="result-path">{{ getDirectoryPath(note.categoryId) }}</span>
              </header>
              
              <div class="result-content">
                <div class="markdown-preview" v-html="truncateMarkdown(note.content)"></div>
              </div>
              
              <footer class="result-footer">
                <span class="result-date">{{ formatDate(note.updatedAt) }}</span>
                <button class="btn btn-sm btn-primary">查看详情 →</button>
              </footer>
            </article>
          </div>
        </div>
      </div>
    </div>
    
    <div v-if="showViewModal" class="modal-overlay" @click="closeViewModal">
      <div class="modal-content modal-large card" @click.stop>
        <header class="modal-header">
          <h2>{{ currentNote?.title }}</h2>
          <button @click="closeViewModal" class="close-btn">&times;</button>
        </header>
        
        <div class="note-view">
          <p class="note-view-date">{{ formatDate(currentNote?.updatedAt) }}</p>
          <div class="markdown-preview note-view-content" v-html="renderMarkdown(currentNote?.content)"></div>
        </div>
        
        <footer class="modal-footer">
          <button @click="closeViewModal" class="btn">关闭</button>
          <button @click="editNote(currentNote)" class="btn btn-primary">编辑</button>
        </footer>
      </div>
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
    
    <div v-if="showDirectoryModalFlag" class="modal-overlay" @click="closeDirectoryModal">
      <div class="modal-content card" @click.stop>
        <header class="modal-header">
          <h2>{{ isEditingDirectory ? '编辑目录' : '新建目录' }}</h2>
          <button @click="closeDirectoryModal" class="close-btn">&times;</button>
        </header>
        
        <form @submit.prevent="saveDirectory" class="modal-form">
          <section class="form-group">
            <label for="directoryName">目录名称</label>
            <input
              id="directoryName"
              v-model="directoryForm.name"
              type="text"
              class="input"
              placeholder="请输入目录名称（不同级目录可重名）"
              required
            />
          </section>
          
          <section class="form-group" v-if="currentDirectory && !isEditingDirectory">
            <label>父级目录</label>
            <div class="parent-info">
              📁 {{ getDirectoryPath(currentDirectory.id) || currentDirectory.name }}
            </div>
          </section>
          
          <section class="form-group" v-else-if="isEditingDirectory">
            <label for="parentDirectory">父级目录（可选）</label>
            <select id="parentDirectory" v-model="directoryForm.parentId" class="input">
              <option :value="null">无父级目录（顶级目录）</option>
              <option v-for="cat in availableParentDirectories" :key="cat.id" :value="cat.id">
                {{ getDirectoryPath(cat.id) }}
              </option>
            </select>
          </section>
          
          <div v-if="directoryError" class="error-message">{{ directoryError }}</div>
          
          <footer class="modal-footer">
            <button type="button" @click="closeDirectoryModal" class="btn">取消</button>
            <button type="submit" class="btn btn-primary" :disabled="directoryLoading">
              {{ directoryLoading ? '保存中...' : '保存' }}
            </button>
          </footer>
        </form>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import api from '../utils/api'
import { marked } from 'marked'
import DirectoryTree from '../components/DirectoryTree.vue'

export default {
  name: 'NoteList',
  components: {
    DirectoryTree
  },
  setup() {
    const router = useRouter()
    const notes = ref([])
    const categories = ref([])
    const loading = ref(true)
    const globalSearchKeyword = ref('')
    const sortBy = ref('updatedAt-desc')
    const username = ref(localStorage.getItem('username') || '')
    const selectedCategoryId = ref(null)
    const currentNoteId = ref(null)
    const expandedDirectories = ref([])
    
    const showNoteModal = ref(false)
    const showViewModal = ref(false)
    const showGlobalSearchModal = ref(false)
    const showConfirmModal = ref(false)
    const confirmTitle = ref('')
    const confirmMessage = ref('')
    let confirmCallback = null
    const showDirectoryModalFlag = ref(false)
    const isEditing = ref(false)
    const isEditingDirectory = ref(false)
    const currentNote = ref(null)
    const currentDirectory = ref(null)
    
    const noteForm = ref({
      title: '',
      content: '',
      categoryId: null
    })
    
    const directoryForm = ref({
      name: '',
      parentId: null
    })
    
    const modalError = ref('')
    const modalLoading = ref(false)
    const directoryError = ref('')
    const directoryLoading = ref(false)
    const globalSearchResults = ref([])
    
    const rootItems = computed(() => {
      const rootDirs = categories.value.filter(cat => !cat.parentId).map(dir => ({
        ...dir,
        type: 'directory'
      }))
      
      const rootNotes = notes.value.filter(note => !note.categoryId).map(note => ({
        ...note,
        type: 'note'
      }))
      
      return [...rootDirs, ...rootNotes].sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt))
    })
    
    const currentItems = computed(() => {
      if (!selectedCategoryId.value) return []
      
      const subDirs = categories.value.filter(cat => cat.parentId === selectedCategoryId.value).map(dir => ({
        ...dir,
        type: 'directory'
      }))
      
      const subNotes = notes.value.filter(note => note.categoryId === selectedCategoryId.value).map(note => ({
        ...note,
        type: 'note'
      }))
      
      return [...subDirs, ...subNotes]
    })
    
    const sortedItems = computed(() => {
      const items = [...currentItems.value]
      
      switch (sortBy.value) {
        case 'updatedAt-desc':
          return items.sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt))
        case 'updatedAt-asc':
          return items.sort((a, b) => new Date(a.updatedAt) - new Date(b.updatedAt))
        case 'name-asc':
          return items.sort((a, b) => {
            const nameA = a.type === 'directory' ? a.name : a.title
            const nameB = b.type === 'directory' ? b.name : b.title
            return nameA.localeCompare(nameB, 'zh-CN')
          })
        case 'name-desc':
          return items.sort((a, b) => {
            const nameA = a.type === 'directory' ? a.name : a.title
            const nameB = b.type === 'directory' ? b.name : b.title
            return nameB.localeCompare(nameA, 'zh-CN')
          })
        default:
          return items
      }
    })
    
    const flatCategories = computed(() => {
      return categories.value
    })
    
    const availableParentDirectories = computed(() => {
      if (isEditingDirectory.value && currentDirectory.value) {
        return categories.value.filter(cat => cat.id !== currentDirectory.value.id)
      }
      return categories.value
    })
    
    const fetchCategories = async () => {
      const userId = localStorage.getItem('userId')
      try {
        const response = await api.get(`/categories/user/${userId}`)
        if (response.data.success) {
          categories.value = response.data.data
        }
      } catch (err) {
        console.error('获取目录失败:', err)
      }
    }
    
    const fetchNotes = async () => {
      const userId = localStorage.getItem('userId')
      if (!userId) {
        router.push('/login')
        return
      }
      
      loading.value = true
      try {
        const response = await api.get(`/notes/user/${userId}`)
        if (response.data.success) {
          notes.value = response.data.data
        }
      } catch (err) {
        console.error('获取笔记失败:', err)
      } finally {
        loading.value = false
      }
    }
    
    const toggleDirectory = (directory) => {
      const index = expandedDirectories.value.indexOf(directory.id)
      if (index > -1) {
        expandedDirectories.value.splice(index, 1)
      } else {
        expandedDirectories.value.push(directory.id)
        expandAllParents(directory)
      }
    }
    
    const expandAllParents = (directory) => {
      if (!directory.parentId) return
      
      const parent = categories.value.find(cat => cat.id === directory.parentId)
      if (parent && !expandedDirectories.value.includes(parent.id)) {
        expandedDirectories.value.push(parent.id)
        expandAllParents(parent)
      }
    }
    
    const enterDirectory = (directory) => {
      selectedCategoryId.value = directory.id
      currentNoteId.value = null
      
      if (!expandedDirectories.value.includes(directory.id)) {
        expandedDirectories.value.push(directory.id)
      }
      expandAllParents(directory)
    }
    
    const goBackToParent = () => {
      const currentDir = categories.value.find(cat => cat.id === selectedCategoryId.value)
      if (currentDir && currentDir.parentId) {
        enterDirectory(categories.value.find(cat => cat.id === currentDir.parentId))
      }
    }
    
    const getParentDirectory = (directoryId) => {
      const directory = categories.value.find(cat => cat.id === directoryId)
      if (!directory || !directory.parentId) return null
      return categories.value.find(cat => cat.id === directory.parentId)
    }
    
    const getCurrentDirectory = () => {
      return categories.value.find(cat => cat.id === selectedCategoryId.value)
    }
    
    const hasChildren = (directory) => {
      const hasSubDirs = categories.value.some(cat => cat.parentId === directory.id)
      const hasNotes = notes.value.some(note => note.categoryId === directory.id)
      return hasSubDirs || hasNotes
    }
    
    const getChildrenItems = (directory) => {
      const subDirs = categories.value.filter(cat => cat.parentId === directory.id).map(dir => ({
        ...dir,
        type: 'directory'
      }))
      
      const subNotes = notes.value.filter(note => note.categoryId === directory.id).map(note => ({
        ...note,
        type: 'note'
      }))
      
      return [...subDirs, ...subNotes].sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt))
    }
    
    const getChildren = (directory) => {
      return categories.value.filter(cat => cat.parentId === directory.id)
    }
    
    const getDirectoryLevel = (directory) => {
      let level = 0
      let current = directory
      while (current.parentId || current.categoryId) {
        const parentId = current.parentId || current.categoryId
        const parent = categories.value.find(cat => cat.id === parentId)
        if (parent) {
          level++
          current = parent
        } else {
          break
        }
      }
      return level
    }
    
    const getDirectoryPath = (directoryId) => {
      const directory = categories.value.find(cat => cat.id === directoryId)
      if (!directory) return ''
      
      const path = [directory.name]
      let current = directory
      while (current.parentId) {
        const parent = categories.value.find(cat => cat.id === current.parentId)
        if (parent) {
          path.unshift(parent.name)
          current = parent
        } else {
          break
        }
      }
      return path.join(' / ')
    }
    
    const getDirectoryName = (directoryId) => {
      const directory = categories.value.find(cat => cat.id === directoryId)
      return directory ? directory.name : ''
    }
    
    const getChildCount = (directoryId) => {
      const dirCount = categories.value.filter(cat => cat.parentId === directoryId).length
      const noteCount = notes.value.filter(note => note.categoryId === directoryId).length
      return dirCount + noteCount
    }
    
    const handleItemClick = (item) => {
      if (item.type === 'directory') {
        enterDirectory(item)
      } else {
        viewNoteById(item.id)
      }
    }
    
    const showCreateModal = () => {
      isEditing.value = false
      noteForm.value = { 
        title: '', 
        content: '', 
        categoryId: selectedCategoryId.value 
      }
      modalError.value = ''
      showNoteModal.value = true
    }
    
    const addNoteToDirectory = (directory) => {
      isEditing.value = false
      noteForm.value = { 
        title: '', 
        content: '', 
        categoryId: directory.id 
      }
      modalError.value = ''
      selectedCategoryId.value = directory.id
      showNoteModal.value = true
    }
    
    const closeNoteModal = () => {
      showNoteModal.value = false
      modalError.value = ''
    }
    
    const closeViewModal = () => {
      showViewModal.value = false
      currentNote.value = null
    }
    
    const viewNoteById = async (noteId) => {
      const routeData = router.resolve({
        name: 'NoteDetail',
        params: { id: noteId }
      })
      window.open(routeData.href, '_blank')
    }
    
    const viewNote = (note) => {
      const routeData = router.resolve({
        name: 'NoteDetail',
        params: { id: note.id }
      })
      window.open(routeData.href, '_blank')
    }
    
    const editNoteById = (noteId) => {
      const note = notes.value.find(n => n.id === noteId)
      if (note) {
        editNote(note)
      }
    }
    
    const editNote = (note) => {
      showViewModal.value = false
      showNoteModal.value = false
      isEditing.value = true
      currentNote.value = note
      noteForm.value = {
        title: note.title,
        content: note.content || '',
        categoryId: note.categoryId
      }
      modalError.value = ''
      showNoteModal.value = true
    }
    
    const saveNote = async () => {
      modalError.value = ''
      
      if (!noteForm.value.title.trim()) {
        modalError.value = '请输入笔记标题'
        return
      }
      
      modalLoading.value = true
      const userId = localStorage.getItem('userId')
      
      try {
        let response
        if (isEditing.value) {
          response = await api.put(`/notes/${currentNote.value.id}`, {
            ...noteForm.value,
            userId: parseInt(userId)
          })
        } else {
          response = await api.post('/notes', {
            ...noteForm.value,
            userId: parseInt(userId)
          })
        }
        
        if (response.data.success) {
          await fetchNotes()
          closeNoteModal()
        } else {
          modalError.value = response.data.message
        }
      } catch (err) {
        modalError.value = err.response?.data?.message || '保存失败，请稍后重试'
      } finally {
        modalLoading.value = false
      }
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
    
    const deleteNote = async (noteId) => {
      showConfirm(
        '删除笔记',
        '确定要删除这篇笔记吗？此操作无法撤销。',
        async () => {
          const userId = localStorage.getItem('userId')
          
          try {
            const response = await api.delete(`/notes/${noteId}`, {
              params: { userId }
            })
            
            if (response.data.success) {
              await fetchNotes()
              if (currentNoteId.value === noteId) {
                currentNoteId.value = null
              }
            } else {
              alert(response.data.message)
            }
          } catch (err) {
            alert('删除失败，请稍后重试')
          }
        }
      )
    }
    
    const showDirectoryModal = (parentDirectory) => {
      isEditingDirectory.value = false
      currentDirectory.value = parentDirectory
      directoryForm.value = { 
        name: '', 
        parentId: parentDirectory ? parentDirectory.id : null 
      }
      directoryError.value = ''
      showDirectoryModalFlag.value = true
    }
    
    const showSubDirectoryModal = (parentDirectory) => {
      isEditingDirectory.value = false
      currentDirectory.value = parentDirectory
      directoryForm.value = { 
        name: '', 
        parentId: parentDirectory ? parentDirectory.id : null 
      }
      directoryError.value = ''
      showDirectoryModalFlag.value = true
    }
    
    const closeDirectoryModal = () => {
      showDirectoryModalFlag.value = false
      directoryError.value = ''
    }
    
    const editDirectory = (directory) => {
      isEditingDirectory.value = true
      currentDirectory.value = directory
      directoryForm.value = { 
        name: directory.name,
        parentId: directory.parentId
      }
      directoryError.value = ''
      showDirectoryModalFlag.value = true
    }
    
    const saveDirectory = async () => {
      directoryError.value = ''
      
      if (!directoryForm.value.name.trim()) {
        directoryError.value = '请输入目录名称'
        return
      }
      
      directoryLoading.value = true
      const userId = localStorage.getItem('userId')
      
      try {
        let response
        if (isEditingDirectory.value) {
          response = await api.put(`/categories/${currentDirectory.value.id}`, {
            ...directoryForm.value,
            userId: parseInt(userId)
          })
        } else {
          response = await api.post('/categories', {
            ...directoryForm.value,
            userId: parseInt(userId)
          })
        }
        
        if (response.data.success) {
          await fetchCategories()
          closeDirectoryModal()
        } else {
          directoryError.value = response.data.message
        }
      } catch (err) {
        directoryError.value = err.response?.data?.message || '保存失败，请稍后重试'
      } finally {
        directoryLoading.value = false
      }
    }
    
    const deleteDirectory = async (directoryId) => {
      showConfirm(
        '⚠️ 删除目录',
        '确定要删除此目录吗？\n\n此操作将级联删除：\n• 该目录下的所有子目录\n• 所有子目录下的笔记\n\n⚠️ 此操作无法撤销！',
        async () => {
          const userId = localStorage.getItem('userId')
          
          try {
            const response = await api.delete(`/categories/${directoryId}`, {
              params: { userId }
            })
            
            if (response.data.success) {
              await fetchCategories()
              if (selectedCategoryId.value === directoryId) {
                selectedCategoryId.value = null
              }
            } else {
              alert(response.data.message)
            }
          } catch (err) {
            alert('删除失败，请稍后重试')
          }
        }
      )
    }
    
    const performGlobalSearch = () => {
      if (!globalSearchKeyword.value.trim()) {
        showConfirm('提示', '请输入搜索关键词', () => {})
        return
      }
      
      const keyword = globalSearchKeyword.value.toLowerCase()
      const results = notes.value.filter(note => 
        note.title.toLowerCase().includes(keyword) ||
        (note.content && note.content.toLowerCase().includes(keyword))
      ).sort((a, b) => new Date(b.updatedAt) - new Date(a.updatedAt))
      
      globalSearchResults.value = results
      showGlobalSearchModal.value = true
    }
    
    const closeGlobalSearchModal = () => {
      showGlobalSearchModal.value = false
    }
    
    const openNoteInNewTab = (note) => {
      const routeData = router.resolve({
        name: 'NoteDetail',
        params: { id: note.id }
      })
      window.open(routeData.href, '_blank')
    }
    
    const handleLogout = () => {
      localStorage.removeItem('userId')
      localStorage.removeItem('username')
      router.push('/login')
    }
    
    const renderMarkdown = (content) => {
      if (!content) return '<p class="empty-content">暂无内容</p>'
      try {
        return marked(content)
      } catch (err) {
        return content
      }
    }
    
    const truncateMarkdown = (content) => {
      if (!content) return '<p class="empty-content">暂无内容</p>'
      
      let text = content
      const maxLength = 200
      
      if (text.length > maxLength) {
        text = text.substring(0, maxLength)
        if (text.includes('<table')) {
          const lastTableEnd = text.lastIndexOf('</table>')
          if (lastTableEnd > -1) {
            text = text.substring(0, lastTableEnd + 8)
          } else {
            text = text.substring(0, text.lastIndexOf('<table'))
          }
        }
        if (text.includes('![')) {
          const lastImgEnd = text.lastIndexOf(')')
          if (lastImgEnd > -1) {
            text = text.substring(0, lastImgEnd + 1)
          } else {
            text = text.substring(0, text.lastIndexOf('!['))
          }
        }
        text += '...'
      }
      
      try {
        return marked(text)
      } catch (err) {
        return text
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
      fetchCategories()
      fetchNotes()
    })
    
    return {
      notes,
      categories,
      loading,
      globalSearchKeyword,
      sortBy,
      filteredNotes: currentItems,
      username,
      selectedCategoryId,
      currentNoteId,
      expandedDirectories,
      rootItems,
      currentItems,
      sortedItems,
      flatCategories,
      availableParentDirectories,
      showNoteModal,
      showViewModal,
      showGlobalSearchModal,
      globalSearchResults,
      showDirectoryModalFlag,
      isEditing,
      isEditingDirectory,
      showConfirmModal,
      confirmTitle,
      confirmMessage,
      currentNote,
      currentDirectory,
      noteForm,
      directoryForm,
      modalError,
      modalLoading,
      directoryError,
      directoryLoading,
      toggleDirectory,
      expandAllParents,
      enterDirectory,
      goBackToParent,
      getParentDirectory,
      getCurrentDirectory,
      hasChildren,
      getChildrenItems,
      getChildren,
      getDirectoryLevel,
      getDirectoryPath,
      getDirectoryName,
      getChildCount,
      handleItemClick,
      showCreateModal,
      addNoteToDirectory,
      closeNoteModal,
      closeViewModal,
      viewNoteById,
      viewNote,
      editNoteById,
      editNote,
      saveNote,
      deleteNote,
      showDirectoryModal,
      showSubDirectoryModal,
      closeDirectoryModal,
      editDirectory,
      saveDirectory,
      deleteDirectory,
      performGlobalSearch,
      closeGlobalSearchModal,
      openNoteInNewTab,
      showConfirm,
      cancelConfirm,
      confirmAction,
      handleLogout,
      renderMarkdown,
      truncateMarkdown,
      formatDate
    }
  }
}
</script>

<style scoped>
.notes-container {
  min-height: 100vh;
  background: #f7fafc;
}

.notes-header {
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  padding: 20px 0;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h1 {
  color: #667eea;
  font-size: 24px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info span {
  color: #4a5568;
  font-weight: 500;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

.btn-success {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
}

.btn-success:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(72, 187, 120, 0.3);
}

.notes-body {
  display: flex;
  max-width: 1400px;
  margin: 0 auto;
  padding: 30px 20px;
  gap: 20px;
  height: calc(100vh - 100px);
}

.sidebar {
  width: 320px;
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.sidebar-header {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e2e8f0;
}

.sidebar-header h3 {
  color: #2d3748;
  font-size: 18px;
  margin: 0;
}

.header-actions {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.global-search {
  margin-bottom: 15px;
}

.global-search-input {
  width: 100%;
  padding: 10px 12px;
  font-size: 13px;
  border: 2px solid #e2e8f0;
  border-radius: 8px;
  transition: all 0.3s;
  background: white;
}

.global-search-input:focus {
  outline: none;
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.global-search-input::placeholder {
  color: #a0aec0;
}

.search-input-group {
  display: flex;
  gap: 8px;
}

.btn-search {
  flex-shrink: 0;
  padding: 10px 16px;
  font-size: 13px;
}

.modal-extra-large {
  max-width: 1200px;
  max-height: 85vh;
}

.search-info {
  background: #f7fafc;
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 20px;
  color: #4a5568;
  font-size: 14px;
}

.search-results {
  max-height: calc(85vh - 200px);
  overflow-y: auto;
}

.no-results {
  text-align: center;
  padding: 60px 20px;
  color: #718096;
}

.no-results p {
  margin: 5px 0;
  font-size: 16px;
}

.no-results .hint {
  font-size: 14px;
  color: #a0aec0;
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.result-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border-left: 4px solid #48bb78;
}

.result-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.result-header {
  margin-bottom: 10px;
}

.result-header h3 {
  color: #2d3748;
  font-size: 16px;
  margin-bottom: 5px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-path {
  display: inline-block;
  background: #ebf4ff;
  color: #667eea;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 500;
}

.result-content {
  color: #4a5568;
  font-size: 13px;
  line-height: 1.6;
  margin-bottom: 15px;
  max-height: 100px;
  overflow: hidden;
}

.result-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
}

.result-date {
  color: #a0aec0;
  font-size: 11px;
}

.category-tree {
  display: flex;
  flex-direction: column;
  gap: 5px;
  flex: 1;
}

.category-wrapper,
.note-item-wrapper {
  width: 100%;
}

.category-item,
.note-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: default;
  transition: all 0.2s;
  background: #f7fafc;
  position: relative;
}

.category-item:hover,
.note-item:hover {
  background: #edf2f7;
}

.category-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.note-item.active {
  background: linear-gradient(135deg, #48bb78 0%, #38a169 100%);
  color: white;
}

.toggle-icon,
.folder-icon,
.file-icon {
  width: 24px;
  min-width: 24px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  cursor: pointer;
  margin-right: 6px;
  flex-shrink: 0;
  color: #4a5568;
  transition: transform 0.2s;
  text-align: center;
  line-height: 1;
}

.toggle-icon.expanded {
  transform: rotate(90deg);
}

.toggle-icon:hover {
  color: #667eea;
}

.item-name {
  font-size: 14px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  min-width: 0;
}

.item-name.clickable {
  cursor: pointer;
  color: inherit;
  transition: all 0.2s;
}

.item-name.clickable:hover {
  color: #667eea;
  font-weight: 600;
  text-decoration: underline;
}

.category-item.active .item-name.clickable:hover,
.note-item.active .item-name.clickable:hover {
  color: white;
}

.item-actions {
  display: flex;
  gap: 1px;
  opacity: 0.6;
  transition: opacity 0.2s;
  margin-left: 2px;
  flex-shrink: 0;
}

.category-item:hover .item-actions,
.note-item:hover .item-actions {
  opacity: 1;
}

.btn-icon {
  background: none !important;
  border: none !important;
  color: #4a5568 !important;
  cursor: pointer !important;
  padding: 1px 1px !important;
  font-size: 12px !important;
  transition: all 0.2s !important;
  line-height: 1 !important;
  border-radius: 6px !important;
  position: relative !important;
  z-index: 1000 !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  min-width: 24px !important;
  min-height: 24px !important;
}

.btn-icon:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: scale(1.05);
}

.folder-btn {
  color: #667eea;
  font-weight: bold;
}

.note-btn {
  color: #48bb78;
  font-weight: bold;
}

.note-btn:hover {
  background: rgba(72, 187, 120, 0.15);
}

.edit-btn {
  color: #4299e1;
}

.delete-btn {
  color: #fc8181;
}

.empty-items {
  text-align: center;
  padding: 40px 20px;
  color: #718096;
}

.empty-items p {
  margin: 5px 0;
}

.empty-items .hint {
  font-size: 13px;
  color: #a0aec0;
}

.notes-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.welcome-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.welcome-content {
  max-width: 700px;
  text-align: center;
  padding: 60px 40px;
}

.welcome-content h2 {
  color: #2d3748;
  margin-bottom: 15px;
  font-size: 28px;
}

.welcome-content > p {
  color: #718096;
  font-size: 16px;
  margin-bottom: 30px;
}

.welcome-features {
  display: flex;
  justify-content: center;
  gap: 30px;
  margin-top: 30px;
}

.feature {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.feature-icon {
  width: 60px;
  height: 60px;
  border-radius: 15px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.feature span:last-child {
  color: #4a5568;
  font-size: 13px;
  font-weight: 600;
}

.content-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  gap: 15px;
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 15px;
  flex: 1;
  min-width: 250px;
}

.btn-back {
  background: #e2e8f0;
  color: #4a5568;
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 13px;
  transition: all 0.2s;
}

.btn-back:hover {
  background: #cbd5e0;
  transform: translateX(-2px);
}

.current-directory-title {
  color: #2d3748;
  font-size: 18px;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-count {
  color: #a0aec0;
  font-size: 13px;
  background: #edf2f7;
  padding: 4px 12px;
  border-radius: 12px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: nowrap;
}

.sort-select {
  width: 140px;
  min-width: 120px;
  font-size: 13px;
}

.content-list {
  flex: 1;
  overflow-y: auto;
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.loading,
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #718096;
  font-size: 16px;
}

.empty-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
  margin-top: 20px;
}

.items-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}

.item-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  border-left: 4px solid transparent;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.item-card > * {
  overflow: hidden;
}

.directory-card {
  border-left-color: #667eea;
}

.note-card {
  border-left-color: #48bb78;
}

.item-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.item-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.icon-large {
  font-size: 32px;
  line-height: 1;
}

.item-header h3 {
  color: #2d3748;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.item-body {
  color: #4a5568;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 15px;
  min-height: 60px;
  max-height: 120px;
  overflow: hidden;
  position: relative;
}

.item-body::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 40px;
  background: linear-gradient(to bottom, transparent, white);
  pointer-events: none;
}

.item-body img,
.item-body table,
.item-body pre,
.item-body iframe {
  max-width: 100% !important;
  height: auto !important;
  display: block;
  overflow: hidden;
}

.item-body table {
  width: 100% !important;
  table-layout: fixed;
  word-wrap: break-word;
}

.item-body td,
.item-body th {
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  padding: 8px !important;
  font-size: 12px !important;
}

.item-description {
  color: #718096;
  font-style: italic;
}

.item-body .empty-content {
  color: #a0aec0;
  font-style: italic;
}

.item-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #e2e8f0;
}

.item-date {
  color: #a0aec0;
  font-size: 11px;
}

.badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.badge-directory {
  background: #ebf4ff;
  color: #667eea;
}

.badge-note {
  background: #f0fff4;
  color: #48bb78;
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
  width: 100%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-large {
  max-width: 900px;
}

.modal-confirm {
  max-width: 480px;
  text-align: center;
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

.confirm-footer {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 0;
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

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e2e8f0;
}

.modal-header h2 {
  color: #2d3748;
  font-size: 20px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #a0aec0;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.close-btn:hover {
  color: #4a5568;
}

.modal-form {
  margin-bottom: 20px;
}

.markdown-editor-group {
  margin-bottom: 20px;
}

.markdown-container {
  display: flex;
  gap: 20px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.split-view {
  display: grid;
  grid-template-columns: 1fr 1fr;
}

.markdown-input {
  padding: 15px;
  border: none;
  font-family: 'Courier New', monospace;
  font-size: 14px;
  resize: vertical;
  min-height: 400px;
  background: #f7fafc;
}

.markdown-input:focus {
  outline: none;
  background: white;
}

.preview-panel {
  padding: 15px;
  background: white;
  overflow-y: auto;
  max-height: 400px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #e2e8f0;
}

.note-view {
  margin-bottom: 20px;
}

.note-view-date {
  color: #a0aec0;
  font-size: 14px;
  margin-bottom: 15px;
}

.note-view-content {
  color: #4a5568;
  line-height: 1.8;
  max-height: 500px;
  overflow-y: auto;
}

.parent-info {
  background: #f7fafc;
  padding: 12px;
  border-radius: 6px;
  color: #4a5568;
  font-size: 14px;
  border: 1px solid #e2e8f0;
}

.markdown-preview {
  word-wrap: break-word;
}

.markdown-preview h1,
.markdown-preview h2,
.markdown-preview h3 {
  color: #2d3748;
  margin-top: 20px;
  margin-bottom: 10px;
}

.markdown-preview h1 {
  font-size: 24px;
  border-bottom: 2px solid #e2e8f0;
  padding-bottom: 10px;
}

.markdown-preview h2 {
  font-size: 20px;
}

.markdown-preview h3 {
  font-size: 16px;
}

.markdown-preview p {
  margin-bottom: 15px;
}

.markdown-preview code {
  background: #f7fafc;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 14px;
}

.markdown-preview pre {
  background: #2d3748;
  color: #e2e8f0;
  padding: 15px;
  border-radius: 8px;
  overflow-x: auto;
  margin-bottom: 15px;
}

.markdown-preview pre code {
  background: none;
  color: inherit;
  padding: 0;
}

.markdown-preview ul,
.markdown-preview ol {
  margin-bottom: 15px;
  padding-left: 30px;
}

.markdown-preview li {
  margin-bottom: 5px;
}

.markdown-preview blockquote {
  border-left: 4px solid #667eea;
  padding-left: 15px;
  margin: 15px 0;
  color: #718096;
  background: #f7fafc;
  padding: 10px 15px;
  border-radius: 5px;
}

.markdown-preview a {
  color: #667eea;
  text-decoration: none;
}

.markdown-preview a:hover {
  text-decoration: underline;
}

.markdown-preview img {
  max-width: 100% !important;
  height: auto !important;
  display: block;
  border-radius: 8px;
  margin: 15px auto;
}

.markdown-preview table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 15px;
}

.markdown-preview th,
.markdown-preview td {
  border: 1px solid #e2e8f0;
  padding: 10px;
  text-align: left;
}

.markdown-preview th {
  background: #f7fafc;
  font-weight: 600;
}

@media (max-width: 1024px) {
  .items-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  }
}

@media (max-width: 768px) {
  .notes-body {
    flex-direction: column;
    height: auto;
  }
  
  .sidebar {
    width: 100%;
    max-height: 450px;
  }
  
  .header-content {
    flex-direction: column;
    gap: 15px;
  }
  
  .content-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  
  .toolbar-left {
    flex-direction: column;
    align-items: stretch;
    gap: 10px;
  }
  
  .toolbar-right {
    flex-wrap: wrap;
    gap: 8px;
  }
  
  .sort-select {
    width: 100%;
    min-width: unset;
    max-width: 100%;
  }
  
  .split-view {
    grid-template-columns: 1fr;
  }
  
  .items-grid {
    grid-template-columns: 1fr;
  }
}
</style>