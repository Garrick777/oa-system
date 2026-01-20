<template>
  <div class="org-page">
    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-group">
        <div class="segment-control">
          <button :class="{ active: viewMode === 'tree' }" @click="viewMode = 'tree'">
            架构图
          </button>
          <button :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'">
            列表
          </button>
        </div>
      </div>
      
      <div class="toolbar-group">
        <button class="tool-btn" @click="handleZoomOut" :disabled="scale <= 0.5">−</button>
        <span class="zoom-label">{{ Math.round(scale * 100) }}%</span>
        <button class="tool-btn" @click="handleZoomIn" :disabled="scale >= 1.2">+</button>
        <div class="separator"></div>
        <button class="tool-btn text" @click="expandAll">展开全部</button>
        <button class="tool-btn text" @click="collapseAll">收起全部</button>
      </div>
    </div>

    <!-- 架构图 -->
    <div 
      v-if="viewMode === 'tree'" 
      class="canvas-wrapper"
      @wheel.prevent="onWheel"
      @mousedown="startPan"
      @mousemove="onPan"
      @mouseup="endPan"
      @mouseleave="endPan"
    >
      <div 
        class="canvas"
        :style="{
          transform: `translate(${pan.x}px, ${pan.y}px) scale(${scale})`,
          cursor: isPanning ? 'grabbing' : 'grab'
        }"
      >
        <!-- 加载状态 -->
        <div v-if="loading" class="loading-state">
          <div class="spinner"></div>
          <span>加载中...</span>
        </div>

        <!-- 树形结构 -->
        <div v-else class="org-tree">
          <OrgCard 
            :node="treeData" 
            :is-root="true"
            :expanded-map="expandedMap"
            @select="selectNode"
            @toggle="toggleExpand"
          />
        </div>
      </div>
    </div>

    <!-- 列表视图 -->
    <div v-else class="list-wrapper">
      <div class="list-card">
        <table class="data-table">
          <thead>
            <tr>
              <th class="col-name">部门名称</th>
              <th class="col-leader">负责人</th>
              <th class="col-count">人数</th>
              <th class="col-status">状态</th>
            </tr>
          </thead>
          <tbody>
            <template v-for="dept in flattenTree(treeData)" :key="dept.id">
              <tr @click="selectNode(dept)">
                <td class="col-name">
                  <span class="indent" :style="{ width: dept._level * 24 + 'px' }"></span>
                  <span class="folder-icon">{{ dept.children?.length ? '📂' : '📁' }}</span>
                  <span>{{ dept.deptName }}</span>
                </td>
                <td class="col-leader">{{ dept.leaderName || '-' }}</td>
                <td class="col-count">{{ dept.memberCount || 0 }}</td>
                <td class="col-status">
                  <span :class="['status-badge', dept.status === 1 ? 'active' : 'inactive']">
                    {{ dept.status === 1 ? '正常' : '停用' }}
                  </span>
                </td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 详情面板 -->
    <Transition name="panel">
      <div v-if="selectedNode" class="detail-panel">
        <div class="panel-header">
          <div class="panel-title">
            <span class="dept-icon">🏢</span>
            <div class="title-text">
              <h3>{{ selectedNode.deptName }}</h3>
              <span class="dept-code">{{ selectedNode.deptCode || 'N/A' }}</span>
            </div>
          </div>
          <button class="close-btn" @click="selectedNode = null">✕</button>
        </div>
        
        <div class="panel-content">
          <div class="info-section">
            <div class="info-item">
              <span class="info-label">负责人</span>
              <span class="info-value">{{ selectedNode.leaderName || '未设置' }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">员工人数</span>
              <span class="info-value">{{ selectedNode.memberCount || 0 }} 人</span>
            </div>
            <div class="info-item">
              <span class="info-label">状态</span>
              <span :class="['info-value', 'status', selectedNode.status === 1 ? 'active' : 'inactive']">
                {{ selectedNode.status === 1 ? '正常运营' : '已停用' }}
              </span>
            </div>
          </div>

          <div v-if="selectedNode.children?.length" class="children-section">
            <h4>下级部门 · {{ selectedNode.children.length }}</h4>
            <div class="children-list">
              <div 
                v-for="child in selectedNode.children" 
                :key="child.id"
                class="child-item"
                @click="selectNode(child)"
              >
                <span class="child-icon">📁</span>
                <span class="child-name">{{ child.deptName }}</span>
                <span class="child-count">{{ child.memberCount || 0 }}人</span>
                <span class="arrow">›</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, defineComponent, h, computed } from 'vue'
import * as api from '@/api/org'

// 状态
const loading = ref(false)
const viewMode = ref<'tree' | 'list'>('tree')
const scale = ref(0.85)
const isPanning = ref(false)
const pan = reactive({ x: 0, y: 0 })
const panStart = reactive({ x: 0, y: 0 })
const selectedNode = ref<any>(null)
const treeData = ref<any>({})
const expandedMap = ref<Map<number, boolean>>(new Map())

// 组织卡片组件
const OrgCard = defineComponent({
  name: 'OrgCard',
  props: {
    node: { type: Object, required: true },
    isRoot: { type: Boolean, default: false },
    expandedMap: { type: Map, required: true }
  },
  emits: ['select', 'toggle'],
  setup(props, { emit }) {
    return () => {
      const node = props.node
      if (!node?.deptName) return null
      
      const hasChildren = node.children?.length > 0
      const isExpanded = props.expandedMap.get(node.id) !== false
      
      return h('div', { class: 'tree-node' }, [
        // 卡片
        h('div', { 
          class: ['node-card', { root: props.isRoot }],
          onClick: () => emit('select', node)
        }, [
          // 头像
          h('div', { class: 'card-avatar' }, [
            props.isRoot
              ? h('span', { class: 'avatar-icon' }, '🏛️')
              : h('span', { class: 'avatar-text' }, node.deptName?.charAt(0))
          ]),
          // 信息
          h('div', { class: 'card-info' }, [
            h('div', { class: 'card-name' }, node.deptName),
            h('div', { class: 'card-meta' }, [
              h('span', { class: 'meta-leader' }, node.leaderName || '待定'),
              h('span', { class: 'meta-count' }, `${node.memberCount || 0}人`)
            ])
          ]),
          // 展开按钮
          hasChildren && h('button', {
            class: ['expand-toggle', { collapsed: !isExpanded }],
            onClick: (e: Event) => { e.stopPropagation(); emit('toggle', node.id) }
          }, isExpanded ? '▾' : '▸')
        ]),
        
        // 子节点
        hasChildren && isExpanded && h('div', { class: 'node-children' }, [
          // 垂直连接线
          h('div', { class: 'vline' }),
          // 子节点容器
          h('div', { class: 'children-wrap' },
            node.children.map((child: any, index: number) =>
              h('div', { 
                class: 'child-branch',
                key: child.id 
              }, [
                // 水平连接线
                h('div', { class: 'hline' }),
                // 递归子卡片
                h(OrgCard, {
                  node: child,
                  isRoot: false,
                  expandedMap: props.expandedMap,
                  onSelect: (n: any) => emit('select', n),
                  onToggle: (id: number) => emit('toggle', id)
                })
              ])
            )
          )
        ])
      ])
    }
  }
})

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await api.getDeptTree()
    const data = res.data || []
    
    if (data.length > 0) {
      treeData.value = data[0]
      // 默认展开前两级
      initExpanded(data[0], 0)
    } else {
      treeData.value = getDefaultData()
    }
  } catch (error) {
    console.error('加载失败:', error)
    treeData.value = getDefaultData()
  } finally {
    loading.value = false
  }
}

const initExpanded = (node: any, level: number) => {
  if (level < 2) {
    expandedMap.value.set(node.id, true)
    node.children?.forEach((c: any) => initExpanded(c, level + 1))
  }
}

const getDefaultData = () => ({
  id: 1, deptName: '星辰科技有限公司', deptCode: 'ROOT',
  leaderName: 'CEO', memberCount: 128, status: 1,
  children: [
    { id: 2, deptName: '技术研发中心', leaderName: '李明', memberCount: 45, status: 1,
      children: [
        { id: 21, deptName: '前端开发组', leaderName: '王伟', memberCount: 15, status: 1 },
        { id: 22, deptName: '后端开发组', leaderName: '刘强', memberCount: 18, status: 1 },
        { id: 23, deptName: '测试组', leaderName: '张丽', memberCount: 12, status: 1 }
      ]
    },
    { id: 3, deptName: '产品设计部', leaderName: '赵芳', memberCount: 20, status: 1 },
    { id: 4, deptName: '市场营销部', leaderName: '周杰', memberCount: 25, status: 1 },
    { id: 5, deptName: '人力资源部', leaderName: '王丽', memberCount: 12, status: 1 },
    { id: 6, deptName: '财务部', leaderName: '李雪', memberCount: 8, status: 1 }
  ]
})

// 展开树
const flattenTree = (node: any, level = 0): any[] => {
  if (!node?.deptName) return []
  const result = [{ ...node, _level: level }]
  if (node.children?.length) {
    node.children.forEach((c: any) => result.push(...flattenTree(c, level + 1)))
  }
  return result
}

// 缩放
const handleZoomIn = () => { scale.value = Math.min(scale.value + 0.1, 1.2) }
const handleZoomOut = () => { scale.value = Math.max(scale.value - 0.1, 0.5) }
const onWheel = (e: WheelEvent) => {
  const delta = e.deltaY > 0 ? -0.05 : 0.05
  scale.value = Math.min(Math.max(scale.value + delta, 0.5), 1.2)
}

// 平移
const startPan = (e: MouseEvent) => {
  isPanning.value = true
  panStart.x = e.clientX - pan.x
  panStart.y = e.clientY - pan.y
}
const onPan = (e: MouseEvent) => {
  if (!isPanning.value) return
  pan.x = e.clientX - panStart.x
  pan.y = e.clientY - panStart.y
}
const endPan = () => { isPanning.value = false }

// 展开/收起
const toggleExpand = (id: number) => {
  const current = expandedMap.value.get(id)
  expandedMap.value.set(id, current === false)
  expandedMap.value = new Map(expandedMap.value)
}

const expandAll = () => {
  const setAll = (node: any) => {
    expandedMap.value.set(node.id, true)
    node.children?.forEach(setAll)
  }
  setAll(treeData.value)
  expandedMap.value = new Map(expandedMap.value)
}

const collapseAll = () => {
  expandedMap.value = new Map()
}

const selectNode = (node: any) => {
  selectedNode.value = node
}

onMounted(() => { loadData() })
</script>

<style scoped lang="scss">
// Mac 风格变量
$bg: #f5f5f7;
$card-bg: #ffffff;
$border: #d2d2d7;
$text: #1d1d1f;
$text-secondary: #86868b;
$accent: #007aff;
$green: #34c759;
$red: #ff3b30;
$radius: 12px;
$shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
$shadow-lg: 0 8px 32px rgba(0, 0, 0, 0.12);

// 字体
@mixin sf-font {
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'SF Pro Text', 
    'Helvetica Neue', 'PingFang SC', 'Microsoft YaHei', sans-serif;
  -webkit-font-smoothing: antialiased;
}

.org-page {
  @include sf-font;
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
  background: $bg;
  position: relative;
}

// 工具栏 - Mac 风格
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: saturate(180%) blur(20px);
  border-bottom: 0.5px solid rgba(0, 0, 0, 0.1);
  
  .toolbar-group {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  
  .separator {
    width: 1px;
    height: 20px;
    background: $border;
    margin: 0 4px;
  }
}

// 分段控制器 - macOS 风格
.segment-control {
  display: flex;
  background: rgba(0, 0, 0, 0.06);
  border-radius: 8px;
  padding: 2px;
  
  button {
    padding: 6px 16px;
    border: none;
    background: transparent;
    border-radius: 6px;
    font-size: 13px;
    font-weight: 500;
    color: $text-secondary;
    cursor: pointer;
    transition: all 0.2s ease;
    
    &.active {
      background: white;
      color: $text;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    }
  }
}

.tool-btn {
  min-width: 28px;
  height: 28px;
  padding: 0 8px;
  border: none;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  color: $text;
  cursor: pointer;
  transition: all 0.15s;
  
  &:hover:not(:disabled) {
    background: rgba(0, 0, 0, 0.1);
  }
  
  &:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }
  
  &.text {
    font-size: 13px;
    color: $accent;
    background: transparent;
    
    &:hover {
      background: rgba(0, 122, 255, 0.1);
    }
  }
}

.zoom-label {
  font-size: 12px;
  color: $text-secondary;
  min-width: 40px;
  text-align: center;
}

// 画布
.canvas-wrapper {
  flex: 1;
  overflow: hidden;
  position: relative;
}

.canvas {
  position: absolute;
  left: 50%;
  top: 40px;
  transform-origin: top center;
  padding: 40px 80px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 80px;
  color: $text-secondary;
  
  .spinner {
    width: 32px;
    height: 32px;
    border: 3px solid rgba(0, 0, 0, 0.1);
    border-top-color: $accent;
    border-radius: 50%;
    animation: spin 0.8s linear infinite;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// 树结构
.org-tree {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.tree-node {
  display: flex;
  flex-direction: column;
  align-items: center;
}

// 卡片 - Mac 风格
.node-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: $card-bg;
  border-radius: $radius;
  box-shadow: $shadow;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 180px;
  position: relative;
  border: 0.5px solid rgba(0, 0, 0, 0.04);
  
  &:hover {
    box-shadow: $shadow-lg;
    transform: translateY(-2px);
  }
  
  &.root {
    background: linear-gradient(135deg, #1d1d1f 0%, #3a3a3c 100%);
    min-width: 220px;
    padding: 16px 20px;
    
    .card-avatar {
      background: rgba(255, 255, 255, 0.15);
      
      .avatar-icon {
        font-size: 24px;
      }
    }
    
    .card-name {
      color: white;
    }
    
    .card-meta {
      color: rgba(255, 255, 255, 0.7);
    }
    
    .meta-leader {
      color: #64d2ff;
    }
    
    .expand-toggle {
      color: rgba(255, 255, 255, 0.6);
      
      &:hover {
        color: white;
      }
    }
  }
  
  .card-avatar {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, $accent 0%, #5856d6 100%);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    
    .avatar-text {
      color: white;
      font-size: 18px;
      font-weight: 600;
    }
    
    .avatar-icon {
      font-size: 20px;
    }
  }
  
  .card-info {
    flex: 1;
    min-width: 0;
  }
  
  .card-name {
    font-size: 14px;
    font-weight: 600;
    color: $text;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  
  .card-meta {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-top: 2px;
    font-size: 12px;
    color: $text-secondary;
    
    .meta-leader {
      color: $accent;
      font-weight: 500;
    }
    
    .meta-count {
      background: rgba(0, 0, 0, 0.05);
      padding: 1px 6px;
      border-radius: 4px;
    }
  }
  
  .expand-toggle {
    position: absolute;
    right: 8px;
    top: 50%;
    transform: translateY(-50%);
    width: 20px;
    height: 20px;
    border: none;
    background: transparent;
    color: $text-secondary;
    font-size: 12px;
    cursor: pointer;
    border-radius: 4px;
    transition: all 0.15s;
    
    &:hover {
      background: rgba(0, 0, 0, 0.05);
      color: $text;
    }
  }
}

// 连接线
.node-children {
  position: relative;
  padding-top: 24px;
  
  .vline {
    position: absolute;
    top: 0;
    left: 50%;
    width: 1px;
    height: 24px;
    background: $border;
  }
  
  .children-wrap {
    display: flex;
    gap: 16px;
  }
  
  .child-branch {
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
    
    .hline {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 1px;
      background: $border;
      
      &::before {
        content: '';
        position: absolute;
        left: 50%;
        top: 0;
        width: 1px;
        height: 24px;
        background: $border;
      }
    }
    
    .tree-node {
      margin-top: 24px;
    }
    
    &:first-child .hline {
      left: 50%;
    }
    
    &:last-child .hline {
      right: 50%;
    }
    
    &:only-child .hline {
      display: none;
    }
  }
}

// 列表视图
.list-wrapper {
  flex: 1;
  padding: 20px;
  overflow: auto;
}

.list-card {
  background: white;
  border-radius: $radius;
  box-shadow: $shadow;
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  
  th, td {
    padding: 12px 16px;
    text-align: left;
    border-bottom: 0.5px solid $border;
  }
  
  th {
    font-size: 12px;
    font-weight: 600;
    color: $text-secondary;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    background: #fafafa;
  }
  
  tr {
    cursor: pointer;
    transition: background 0.15s;
    
    &:hover {
      background: #f5f5f7;
    }
    
    &:last-child td {
      border-bottom: none;
    }
  }
  
  .col-name {
    display: flex;
    align-items: center;
    gap: 8px;
    font-weight: 500;
    color: $text;
    
    .indent {
      display: inline-block;
    }
    
    .folder-icon {
      font-size: 16px;
    }
  }
  
  .col-leader {
    color: $accent;
  }
  
  .col-count {
    color: $text-secondary;
  }
  
  .status-badge {
    display: inline-block;
    padding: 4px 10px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 500;
    
    &.active {
      background: rgba(52, 199, 89, 0.1);
      color: $green;
    }
    
    &.inactive {
      background: rgba(255, 59, 48, 0.1);
      color: $red;
    }
  }
}

// 详情面板 - Mac 风格
.detail-panel {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 360px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: saturate(180%) blur(20px);
  box-shadow: -1px 0 0 rgba(0, 0, 0, 0.1), -8px 0 32px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  z-index: 100;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-bottom: 0.5px solid rgba(0, 0, 0, 0.1);
  
  .panel-title {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .dept-icon {
      font-size: 32px;
    }
    
    .title-text {
      h3 {
        margin: 0;
        font-size: 17px;
        font-weight: 600;
        color: $text;
      }
      
      .dept-code {
        font-size: 12px;
        color: $text-secondary;
      }
    }
  }
  
  .close-btn {
    width: 28px;
    height: 28px;
    border: none;
    background: rgba(0, 0, 0, 0.06);
    border-radius: 50%;
    color: $text-secondary;
    font-size: 12px;
    cursor: pointer;
    transition: all 0.15s;
    
    &:hover {
      background: rgba(0, 0, 0, 0.1);
      color: $text;
    }
  }
}

.panel-content {
  flex: 1;
  overflow: auto;
  padding: 20px;
}

.info-section {
  background: #fafafa;
  border-radius: 10px;
  padding: 4px 0;
  
  .info-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    
    &:not(:last-child) {
      border-bottom: 0.5px solid rgba(0, 0, 0, 0.06);
    }
    
    .info-label {
      font-size: 14px;
      color: $text-secondary;
    }
    
    .info-value {
      font-size: 14px;
      font-weight: 500;
      color: $text;
      
      &.status.active {
        color: $green;
      }
      
      &.status.inactive {
        color: $red;
      }
    }
  }
}

.children-section {
  margin-top: 24px;
  
  h4 {
    margin: 0 0 12px;
    font-size: 13px;
    font-weight: 600;
    color: $text-secondary;
    text-transform: uppercase;
    letter-spacing: 0.5px;
  }
  
  .children-list {
    background: #fafafa;
    border-radius: 10px;
    overflow: hidden;
  }
  
  .child-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 12px 16px;
    cursor: pointer;
    transition: background 0.15s;
    
    &:hover {
      background: rgba(0, 122, 255, 0.06);
    }
    
    &:not(:last-child) {
      border-bottom: 0.5px solid rgba(0, 0, 0, 0.06);
    }
    
    .child-icon {
      font-size: 16px;
    }
    
    .child-name {
      flex: 1;
      font-size: 14px;
      color: $text;
    }
    
    .child-count {
      font-size: 12px;
      color: $text-secondary;
    }
    
    .arrow {
      color: $text-secondary;
      font-size: 16px;
    }
  }
}

// 面板动画
.panel-enter-active,
.panel-leave-active {
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), 
              opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.panel-enter-from,
.panel-leave-to {
  transform: translateX(100%);
  opacity: 0;
}
</style>
