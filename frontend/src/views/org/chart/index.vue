<template>
  <div class="org-chart-page">
    <!-- 顶部工具栏 -->
    <header class="toolbar">
      <div class="toolbar-left">
        <div class="view-switch">
          <button 
            :class="['switch-btn', { active: viewMode === 'tree' }]" 
            @click="viewMode = 'tree'"
          >
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z"/>
            </svg>
            架构图
          </button>
          <button 
            :class="['switch-btn', { active: viewMode === 'list' }]" 
            @click="viewMode = 'list'"
          >
            <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
              <path d="M3 13h2v-2H3v2zm0 4h2v-2H3v2zm0-8h2V7H3v2zm4 4h14v-2H7v2zm0 4h14v-2H7v2zM7 7v2h14V7H7z"/>
            </svg>
            列表
          </button>
        </div>
        
        <div class="divider"></div>
        
        <div class="zoom-controls">
          <button class="icon-btn" @click="handleZoomOut" :disabled="scale <= 0.4" title="缩小">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M19 13H5v-2h14v2z"/>
            </svg>
          </button>
          <span class="zoom-value">{{ Math.round(scale * 100) }}%</span>
          <button class="icon-btn" @click="handleZoomIn" :disabled="scale >= 1.5" title="放大">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
            </svg>
          </button>
          <button class="icon-btn" @click="handleReset" title="重置">
            <svg viewBox="0 0 24 24" width="20" height="20" fill="currentColor">
              <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
            </svg>
          </button>
        </div>

        <div class="divider"></div>

        <div class="collapse-controls">
          <button class="text-btn" @click="expandAll">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M12 8l-6 6 1.41 1.41L12 10.83l4.59 4.58L18 14z"/>
            </svg>
            展开
          </button>
          <button class="text-btn" @click="collapseAll">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor">
              <path d="M16.59 8.59L12 13.17 7.41 8.59 6 10l6 6 6-6z"/>
            </svg>
            收起
          </button>
        </div>
      </div>
    </header>

    <!-- 架构图视图 -->
    <main 
      v-if="viewMode === 'tree'" 
      class="canvas-container"
      ref="canvasRef"
      @wheel.prevent="handleWheel"
      @mousedown="startDrag"
      @mousemove="onDrag"
      @mouseup="endDrag"
      @mouseleave="endDrag"
    >
      <div 
        class="canvas"
        :style="{
          transform: `translate(${pan.x}px, ${pan.y}px) scale(${scale})`,
          cursor: isDragging ? 'grabbing' : 'grab'
        }"
      >
        <div v-if="loading" class="loading">
          <div class="spinner"></div>
          <span>加载中...</span>
        </div>
        
        <div v-else class="tree">
          <!-- 根节点 -->
          <div class="tree-level root-level">
            <div class="node root-node" @click="openDetail(orgData)">
              <div class="node-badge">总部</div>
              <div class="node-body">
                <div class="node-avatar">
                  <svg viewBox="0 0 24 24" width="28" height="28" fill="currentColor">
                    <path d="M12 7V3H2v18h20V7H12zM6 19H4v-2h2v2zm0-4H4v-2h2v2zm0-4H4V9h2v2zm0-4H4V5h2v2zm4 12H8v-2h2v2zm0-4H8v-2h2v2zm0-4H8V9h2v2zm0-4H8V5h2v2zm10 12h-8v-2h2v-2h-2v-2h2v-2h-2V9h8v10zm-2-8h-2v2h2v-2zm0 4h-2v2h2v-2z"/>
                  </svg>
                </div>
                <div class="node-info">
                  <h3 class="node-title">{{ orgData.deptName }}</h3>
                  <p class="node-subtitle">
                    <span class="leader">{{ orgData.leaderName || 'CEO' }}</span>
                    <span class="count">{{ orgData.memberCount || 0 }}人</span>
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- 子部门 -->
          <template v-if="orgData.children?.length && !collapsedIds.has(0)">
            <div class="connector vertical"></div>
            <div class="tree-level">
              <div class="connector horizontal" :style="{ width: getConnectorWidth(orgData.children) }"></div>
              <div class="level-nodes">
                <TreeNode 
                  v-for="child in orgData.children" 
                  :key="child.id"
                  :node="child"
                  :level="1"
                  :collapsed-ids="collapsedIds"
                  @select="openDetail"
                  @toggle="toggleCollapse"
                />
              </div>
            </div>
          </template>
          
          <!-- 折叠指示器 -->
          <button 
            v-if="orgData.children?.length"
            class="collapse-indicator"
            @click.stop="toggleCollapse(0)"
          >
            {{ collapsedIds.has(0) ? '+' : '−' }}
          </button>
        </div>
      </div>
    </main>

    <!-- 列表视图 -->
    <main v-else class="list-container">
      <el-table
        :data="flatList"
        v-loading="loading"
        row-key="id"
        :tree-props="{ children: 'children' }"
        default-expand-all
        stripe
      >
        <el-table-column prop="deptName" label="部门名称" min-width="220">
          <template #default="{ row }">
            <div class="dept-name-cell">
              <span class="dept-icon">📁</span>
              <span>{{ row.deptName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="leaderName" label="负责人" width="120">
          <template #default="{ row }">
            <span v-if="row.leaderName" class="leader-name">{{ row.leaderName }}</span>
            <span v-else class="empty">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="memberCount" label="人数" width="100" align="center">
          <template #default="{ row }">
            <span class="member-count">{{ row.memberCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <span :class="['status-tag', row.status === 1 ? 'active' : 'inactive']">
              {{ row.status === 1 ? '正常' : '停用' }}
            </span>
          </template>
        </el-table-column>
      </el-table>
    </main>

    <!-- 详情面板 -->
    <Transition name="slide">
      <aside v-if="selectedNode" class="detail-panel">
        <header class="panel-header">
          <h2>{{ selectedNode.deptName }}</h2>
          <button class="close-btn" @click="selectedNode = null">
            <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
              <path d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </header>
        
        <div class="panel-body">
          <div class="info-card">
            <div class="info-row">
              <span class="label">部门编码</span>
              <span class="value">{{ selectedNode.deptCode || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">负责人</span>
              <span class="value highlight">{{ selectedNode.leaderName || '未设置' }}</span>
            </div>
            <div class="info-row">
              <span class="label">部门人数</span>
              <span class="value">
                <strong>{{ selectedNode.memberCount || 0 }}</strong> 人
              </span>
            </div>
            <div class="info-row">
              <span class="label">状态</span>
              <span :class="['value', 'status', selectedNode.status === 1 ? 'active' : 'inactive']">
                {{ selectedNode.status === 1 ? '正常运营' : '已停用' }}
              </span>
            </div>
          </div>

          <div v-if="selectedNode.children?.length" class="sub-depts">
            <h4>下级部门 ({{ selectedNode.children.length }})</h4>
            <div class="sub-list">
              <div 
                v-for="sub in selectedNode.children" 
                :key="sub.id" 
                class="sub-item"
                @click="openDetail(sub)"
              >
                <span class="sub-icon">📂</span>
                <span class="sub-name">{{ sub.deptName }}</span>
                <span class="sub-count">{{ sub.memberCount || 0 }}人</span>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, defineComponent, h, computed } from 'vue'
import * as api from '@/api/org'

const loading = ref(false)
const viewMode = ref<'tree' | 'list'>('tree')
const scale = ref(0.9)
const canvasRef = ref<HTMLElement>()
const selectedNode = ref<any>(null)
const orgData = ref<any>({})
const flatList = ref<any[]>([])
const collapsedIds = ref<Set<number>>(new Set())

// 拖拽
const isDragging = ref(false)
const pan = reactive({ x: 0, y: 0 })
const dragStart = reactive({ x: 0, y: 0 })

// 树节点组件
const TreeNode = defineComponent({
  name: 'TreeNode',
  props: {
    node: { type: Object, required: true },
    level: { type: Number, default: 1 },
    collapsedIds: { type: Set, required: true }
  },
  emits: ['select', 'toggle'],
  setup(props, { emit }) {
    const colors = [
      { bg: '#6366f1', light: '#eef2ff' },  // Indigo
      { bg: '#8b5cf6', light: '#f5f3ff' },  // Violet  
      { bg: '#06b6d4', light: '#ecfeff' },  // Cyan
      { bg: '#10b981', light: '#ecfdf5' },  // Emerald
      { bg: '#f59e0b', light: '#fffbeb' },  // Amber
    ]
    
    return () => {
      const node = props.node
      const hasChildren = node.children?.length > 0
      const isCollapsed = props.collapsedIds.has(node.id)
      const color = colors[props.level % colors.length]
      
      const getWidth = (children: any[]) => {
        const count = children.length
        return `calc(${(count - 1) * 100}% / ${count} + ${(count - 1) * 24}px)`
      }

      return h('div', { class: 'tree-branch' }, [
        // 节点
        h('div', { class: 'branch-wrapper' }, [
          h('div', {
            class: 'node',
            style: { '--node-color': color.bg, '--node-light': color.light },
            onClick: () => emit('select', node)
          }, [
            h('div', { class: 'node-body' }, [
              h('div', { class: 'node-avatar' }, node.deptName?.charAt(0)),
              h('div', { class: 'node-info' }, [
                h('h3', { class: 'node-title' }, node.deptName),
                h('p', { class: 'node-subtitle' }, [
                  node.leaderName && h('span', { class: 'leader' }, node.leaderName),
                  h('span', { class: 'count' }, `${node.memberCount || 0}人`)
                ])
              ])
            ]),
            hasChildren && h('button', {
              class: 'expand-btn',
              onClick: (e: Event) => { e.stopPropagation(); emit('toggle', node.id) }
            }, isCollapsed ? '+' : '−')
          ])
        ]),
        
        // 子节点
        hasChildren && !isCollapsed && h('div', { class: 'branch-children' }, [
          h('div', { class: 'connector vertical' }),
          h('div', { class: 'connector horizontal', style: { width: getWidth(node.children) } }),
          h('div', { class: 'level-nodes' },
            node.children.map((child: any) =>
              h(TreeNode, {
                key: child.id,
                node: child,
                level: props.level + 1,
                collapsedIds: props.collapsedIds,
                onSelect: (n: any) => emit('select', n),
                onToggle: (id: number) => emit('toggle', id)
              })
            )
          )
        ])
      ])
    }
  }
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.getDeptTree()
    const data = res.data || []
    orgData.value = {
      id: 0,
      deptName: data[0]?.deptName || '公司总部',
      deptCode: data[0]?.deptCode || 'HQ',
      leaderName: data[0]?.leaderName || 'CEO',
      memberCount: countMembers(data),
      status: 1,
      children: data[0]?.children || data
    }
    flatList.value = data
  } catch (error) {
    console.error('加载失败:', error)
    orgData.value = getDefaultData()
    flatList.value = orgData.value.children
  } finally {
    loading.value = false
  }
}

const countMembers = (nodes: any[]): number => {
  return nodes.reduce((sum, n) => {
    return sum + (n.memberCount || 0) + (n.children ? countMembers(n.children) : 0)
  }, 0)
}

const getDefaultData = () => ({
  id: 0,
  deptName: '星辰科技有限公司',
  deptCode: 'ROOT',
  leaderName: 'CEO',
  memberCount: 120,
  status: 1,
  children: [
    { id: 1, deptName: '技术研发中心', leaderName: '李明', memberCount: 45, status: 1,
      children: [
        { id: 11, deptName: '前端开发组', leaderName: '王伟', memberCount: 12, status: 1 },
        { id: 12, deptName: '后端开发组', leaderName: '刘强', memberCount: 15, status: 1 },
        { id: 13, deptName: '测试组', leaderName: '张丽', memberCount: 8, status: 1 },
      ]
    },
    { id: 2, deptName: '产品设计部', leaderName: '赵芳', memberCount: 20, status: 1 },
    { id: 3, deptName: '市场营销部', leaderName: '周杰', memberCount: 25, status: 1 },
    { id: 4, deptName: '人力资源部', leaderName: '王丽', memberCount: 12, status: 1 },
    { id: 5, deptName: '财务部', leaderName: '李雪', memberCount: 8, status: 1 },
  ]
})

const getConnectorWidth = (children: any[]) => {
  const count = children.length
  return `calc(${(count - 1) * 100}% / ${count} + ${(count - 1) * 24}px)`
}

// 缩放
const handleZoomIn = () => { scale.value = Math.min(scale.value + 0.1, 1.5) }
const handleZoomOut = () => { scale.value = Math.max(scale.value - 0.1, 0.4) }
const handleReset = () => { scale.value = 0.9; pan.x = 0; pan.y = 0 }
const handleWheel = (e: WheelEvent) => {
  const delta = e.deltaY > 0 ? -0.05 : 0.05
  scale.value = Math.min(Math.max(scale.value + delta, 0.4), 1.5)
}

// 拖拽
const startDrag = (e: MouseEvent) => {
  isDragging.value = true
  dragStart.x = e.clientX - pan.x
  dragStart.y = e.clientY - pan.y
}
const onDrag = (e: MouseEvent) => {
  if (!isDragging.value) return
  pan.x = e.clientX - dragStart.x
  pan.y = e.clientY - dragStart.y
}
const endDrag = () => { isDragging.value = false }

// 折叠
const toggleCollapse = (id: number) => {
  const newSet = new Set(collapsedIds.value)
  newSet.has(id) ? newSet.delete(id) : newSet.add(id)
  collapsedIds.value = newSet
}
const expandAll = () => { collapsedIds.value = new Set() }
const collapseAll = () => {
  const ids = new Set<number>()
  const collect = (nodes: any[]) => {
    nodes.forEach(n => {
      if (n.children?.length) { ids.add(n.id); collect(n.children) }
    })
  }
  ids.add(0)
  if (orgData.value.children) collect(orgData.value.children)
  collapsedIds.value = ids
}

const openDetail = (node: any) => { selectedNode.value = node }

onMounted(() => { loadData() })
</script>

<style scoped lang="scss">
// 变量
$primary: #6366f1;
$bg: #f8fafc;
$border: #e2e8f0;
$text: #1e293b;
$text-muted: #64748b;

.org-chart-page {
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
  background: $bg;
  position: relative;
}

// 工具栏
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 24px;
  background: white;
  border-bottom: 1px solid $border;
  
  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }
  
  .divider {
    width: 1px;
    height: 24px;
    background: $border;
  }
}

.view-switch {
  display: flex;
  background: #f1f5f9;
  border-radius: 8px;
  padding: 4px;
  
  .switch-btn {
    display: flex;
    align-items: center;
    gap: 6px;
    padding: 8px 16px;
    border: none;
    background: transparent;
    border-radius: 6px;
    font-size: 14px;
    color: $text-muted;
    cursor: pointer;
    transition: all 0.2s;
    
    &.active {
      background: white;
      color: $text;
      box-shadow: 0 1px 3px rgba(0,0,0,0.1);
    }
    
    &:hover:not(.active) {
      color: $text;
    }
  }
}

.zoom-controls {
  display: flex;
  align-items: center;
  gap: 8px;
  
  .zoom-value {
    width: 48px;
    text-align: center;
    font-size: 13px;
    font-weight: 500;
    color: $text-muted;
  }
}

.icon-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid $border;
  background: white;
  border-radius: 8px;
  color: $text-muted;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover:not(:disabled) {
    border-color: $primary;
    color: $primary;
  }
  
  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
}

.collapse-controls {
  display: flex;
  gap: 8px;
}

.text-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 12px;
  border: none;
  background: transparent;
  font-size: 13px;
  color: $text-muted;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
  
  &:hover {
    background: #f1f5f9;
    color: $text;
  }
}

// 画布
.canvas-container {
  flex: 1;
  overflow: hidden;
  position: relative;
  background: 
    radial-gradient(circle at 1px 1px, #e2e8f0 1px, transparent 0);
  background-size: 24px 24px;
}

.canvas {
  position: absolute;
  left: 50%;
  top: 60px;
  transform-origin: top center;
  transition: transform 0.1s ease-out;
  padding: 40px 100px;
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 80px;
  color: $text-muted;
  
  .spinner {
    width: 40px;
    height: 40px;
    border: 3px solid #e2e8f0;
    border-top-color: $primary;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// 树结构
.tree {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.tree-level {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.level-nodes {
  display: flex;
  gap: 24px;
}

.connector {
  background: #cbd5e1;
  
  &.vertical {
    width: 2px;
    height: 32px;
  }
  
  &.horizontal {
    height: 2px;
    position: relative;
    margin-bottom: -2px;
  }
}

// 节点
.node {
  --node-color: #{$primary};
  --node-light: #eef2ff;
  
  position: relative;
  background: white;
  border-radius: 16px;
  padding: 4px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1), 0 2px 4px -2px rgba(0,0,0,0.1);
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1), 0 8px 10px -6px rgba(0,0,0,0.1);
  }
  
  .node-body {
    display: flex;
    align-items: center;
    gap: 14px;
    padding: 16px 20px;
    background: var(--node-light);
    border-radius: 12px;
    min-width: 200px;
  }
  
  .node-avatar {
    width: 48px;
    height: 48px;
    background: var(--node-color);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 20px;
    font-weight: 600;
    flex-shrink: 0;
  }
  
  .node-info {
    flex: 1;
    min-width: 0;
  }
  
  .node-title {
    font-size: 15px;
    font-weight: 600;
    color: $text;
    margin: 0 0 4px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .node-subtitle {
    display: flex;
    align-items: center;
    gap: 8px;
    margin: 0;
    font-size: 13px;
    color: $text-muted;
    
    .leader {
      color: var(--node-color);
      font-weight: 500;
    }
    
    .count {
      padding: 2px 8px;
      background: rgba(0,0,0,0.05);
      border-radius: 10px;
      font-size: 12px;
    }
  }
  
  .expand-btn {
    position: absolute;
    bottom: -14px;
    left: 50%;
    transform: translateX(-50%);
    width: 28px;
    height: 28px;
    background: white;
    border: 2px solid var(--node-color);
    border-radius: 50%;
    color: var(--node-color);
    font-size: 18px;
    font-weight: bold;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    
    &:hover {
      background: var(--node-color);
      color: white;
    }
  }
}

// 根节点
.root-node {
  --node-color: #0f172a;
  --node-light: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  
  .node-body {
    background: var(--node-light);
  }
  
  .node-avatar {
    background: rgba(255,255,255,0.15);
  }
  
  .node-title, .node-subtitle {
    color: white;
  }
  
  .node-subtitle .leader {
    color: #a5b4fc;
  }
  
  .node-subtitle .count {
    background: rgba(255,255,255,0.15);
    color: white;
  }
  
  .node-badge {
    position: absolute;
    top: -10px;
    left: 20px;
    background: linear-gradient(135deg, #f59e0b, #d97706);
    color: white;
    font-size: 11px;
    font-weight: 600;
    padding: 4px 12px;
    border-radius: 20px;
    box-shadow: 0 2px 4px rgba(245,158,11,0.3);
  }
}

.collapse-indicator {
  position: absolute;
  bottom: -45px;
  left: 50%;
  transform: translateX(-50%);
  width: 28px;
  height: 28px;
  background: white;
  border: 2px solid #0f172a;
  border-radius: 50%;
  color: #0f172a;
  font-size: 18px;
  font-weight: bold;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  
  &:hover {
    background: #0f172a;
    color: white;
  }
}

// 分支
.tree-branch {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 50%;
    width: 2px;
    height: 32px;
    background: #cbd5e1;
    transform: translateY(-100%);
  }
  
  .branch-wrapper {
    position: relative;
    z-index: 1;
  }
  
  .branch-children {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding-top: 16px;
  }
}

// 列表视图
.list-container {
  flex: 1;
  padding: 24px;
  overflow: auto;
  
  .dept-name-cell {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .dept-icon {
      font-size: 16px;
    }
  }
  
  .leader-name {
    color: $primary;
    font-weight: 500;
  }
  
  .empty {
    color: #cbd5e1;
  }
  
  .member-count {
    font-weight: 600;
    color: $text;
  }
  
  .status-tag {
    display: inline-block;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 500;
    
    &.active {
      background: #dcfce7;
      color: #16a34a;
    }
    
    &.inactive {
      background: #fee2e2;
      color: #dc2626;
    }
  }
}

// 详情面板
.detail-panel {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 380px;
  background: white;
  box-shadow: -4px 0 24px rgba(0,0,0,0.1);
  display: flex;
  flex-direction: column;
  z-index: 100;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid $border;
  
  h2 {
    margin: 0;
    font-size: 18px;
    font-weight: 600;
    color: $text;
  }
  
  .close-btn {
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: none;
    background: transparent;
    color: $text-muted;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      background: #f1f5f9;
      color: $text;
    }
  }
}

.panel-body {
  flex: 1;
  overflow: auto;
  padding: 24px;
}

.info-card {
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  
  .info-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #e2e8f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .label {
      font-size: 14px;
      color: $text-muted;
    }
    
    .value {
      font-size: 14px;
      color: $text;
      font-weight: 500;
      
      &.highlight {
        color: $primary;
      }
      
      &.status.active {
        color: #16a34a;
      }
      
      &.status.inactive {
        color: #dc2626;
      }
    }
  }
}

.sub-depts {
  margin-top: 24px;
  
  h4 {
    margin: 0 0 12px;
    font-size: 14px;
    font-weight: 600;
    color: $text;
  }
  
  .sub-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  
  .sub-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 16px;
    background: #f8fafc;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.2s;
    
    &:hover {
      background: #eef2ff;
    }
    
    .sub-icon {
      font-size: 16px;
    }
    
    .sub-name {
      flex: 1;
      font-size: 14px;
      color: $text;
    }
    
    .sub-count {
      font-size: 12px;
      color: $text-muted;
      background: white;
      padding: 2px 8px;
      border-radius: 10px;
    }
  }
}

// 动画
.slide-enter-active,
.slide-leave-active {
  transition: transform 0.3s ease, opacity 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  transform: translateX(100%);
  opacity: 0;
}
</style>
