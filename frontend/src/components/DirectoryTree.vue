<template>
  <div class="directory-tree">
    <template v-for="item in items" :key="item.id">
      <!-- 目录项 -->
      <div 
        v-if="item.type === 'directory'"
        class="category-wrapper"
        :style="{ paddingLeft: 8 + 'px' }"
      >
        <div
          class="category-item"
          :class="{ active: selectedCategoryId === item.id }"
        >
          <span 
            class="folder-icon clickable"
            @click.stop="$emit('toggle-directory', item)"
          >
            {{ expandedDirectories.includes(item.id) ? '📂' : '📁' }}
          </span>
          
          <span 
            class="item-name clickable"
            @click.stop="$emit('enter-directory', item)"
          >{{ item.name }}</span>
          
          <div class="item-actions">
            <button @click.stop="$emit('show-sub-directory-modal', item)" class="btn-icon folder-btn" title="新建子目录">📁+</button>
            <button @click.stop="$emit('add-note-to-directory', item)" class="btn-icon note-btn" title="在此添加笔记">✏️</button>
            <button @click.stop="$emit('edit-directory', item)" class="btn-icon edit-btn" title="编辑目录">✎</button>
            <button @click.stop="$emit('delete-directory', item.id)" class="btn-icon delete-btn" title="删除目录">✕</button>
          </div>
        </div>
        
        <!-- 递归渲染子项 -->
        <DirectoryTree
          v-if="expandedDirectories.includes(item.id) && props.hasChildren(item)"
          :items="props.getChildrenItems(item)"
          :level="level + 1"
          :selectedCategoryId="selectedCategoryId"
          :currentNoteId="currentNoteId"
          :expandedDirectories="expandedDirectories"
          :hasChildren="props.hasChildren"
          :getChildrenItems="props.getChildrenItems"
          @toggle-directory="(dir) => $emit('toggle-directory', dir)"
          @enter-directory="(dir) => $emit('enter-directory', dir)"
          @show-sub-directory-modal="(dir) => $emit('show-sub-directory-modal', dir)"
          @add-note-to-directory="(dir) => $emit('add-note-to-directory', dir)"
          @edit-directory="(dir) => $emit('edit-directory', dir)"
          @delete-directory="(id) => $emit('delete-directory', id)"
          @view-note-by-id="(id) => $emit('view-note-by-id', id)"
          @edit-note-by-id="(id) => $emit('edit-note-by-id', id)"
          @delete-note="(id) => $emit('delete-note', id)"
        />
      </div>
      
      <!-- 笔记项 -->
      <div 
        v-else
        class="note-item-wrapper"
        :style="{ paddingLeft: 8 + 'px' }"
      >
        <div
          class="note-item"
          :class="{ active: currentNoteId === item.id }"
        >
          <span class="file-icon">📄</span>
          
          <span 
            class="item-name clickable"
            @click.stop="$emit('view-note-by-id', item.id)"
          >{{ item.title }}</span>
          
          <div class="item-actions">
            <button @click.stop="$emit('edit-note-by-id', item.id)" class="btn-icon edit-btn" title="编辑笔记">✎</button>
            <button @click.stop="$emit('delete-note', item.id)" class="btn-icon delete-btn" title="删除笔记">✕</button>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
const props = defineProps({
  items: {
    type: Array,
    required: true,
    default: () => []
  },
  level: {
    type: Number,
    default: 0
  },
  selectedCategoryId: {
    type: [Number, String],
    default: null
  },
  currentNoteId: {
    type: [Number, String],
    default: null
  },
  expandedDirectories: {
    type: Array,
    required: true
  },
  hasChildren: {
    type: Function,
    required: true
  },
  getChildrenItems: {
    type: Function,
    required: true
  }
})

const emit = defineEmits([
  'toggle-directory',
  'enter-directory',
  'show-sub-directory-modal',
  'add-note-to-directory',
  'edit-directory',
  'delete-directory',
  'view-note-by-id',
  'edit-note-by-id',
  'delete-note'
])
</script>

<style scoped>
.directory-tree {
  width: 100%;
}

.category-wrapper,
.note-item-wrapper {
  width: 100%;
}

.category-item,
.note-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.2s;
  cursor: pointer;
  background: rgba(247, 250, 252, 0.95);
  margin-bottom: 2px;
}

.category-item:hover,
.note-item:hover {
  background: rgba(102, 126, 234, 0.12);
}

.category-item.active,
.note-item.active {
  background: rgba(102, 126, 234, 0.15);
  font-weight: 600;
}

.folder-icon,
.file-icon {
  margin-right: 8px;
  font-size: 14px;
  flex-shrink: 0;
}

.item-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.clickable {
  cursor: pointer;
}

.clickable:hover {
  color: #667eea;
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
  border-radius: 4px !important;
  position: relative !important;
  z-index: 1000 !important;
  display: inline-flex !important;
  align-items: center !important;
  justify-content: center !important;
  min-width: 18px !important;
  min-height: 18px !important;
}

.btn-icon:hover {
  background: rgba(102, 126, 234, 0.1);
  transform: scale(1.05);
}
</style>