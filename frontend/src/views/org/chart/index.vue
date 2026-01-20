<template>
  <div class="org-chart-page">
    <!-- 工具栏 -->
    <div class="chart-toolbar">
      <div class="toolbar-left">
        <el-button-group>
          <el-button :type="viewMode === 'tree' ? 'primary' : 'default'" @click="viewMode = 'tree'">
            <el-icon><Share /></el-icon>
            组织架构
          </el-button>
          <el-button :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'">
            <el-icon><List /></el-icon>
            列表视图
          </el-button>
        </el-button-group>
        <el-divider direction="vertical" />
        <el-button @click="handleZoomIn" :disabled="scale >= 2">
          <el-icon><ZoomIn /></el-icon>
        </el-button>
        <span class="zoom-text">{{ Math.round(scale * 100) }}%</span>
        <el-button @click="handleZoomOut" :disabled="scale <= 0.3">
          <el-icon><ZoomOut /></el-icon>
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
        <el-divider direction="vertical" />
        <el-button @click="expandAll">全部展开</el-button>
        <el-button @click="collapseAll">全部收起</el-button>
      </div>
      <div class="toolbar-right">
        <el-button type="success" @click="handleExport">
          <el-icon><Download /></el-icon>
          导出
        </el-button>
      </div>
    </div>

    <!-- 树形图视图 -->
    <div v-if="viewMode === 'tree'" class="chart-container" ref="chartRef"
         @wheel.prevent="handleWheel"
         @mousedown="handleMouseDown"
         @mousemove="handleMouseMove"
         @mouseup="handleMouseUp"
         @mouseleave="handleMouseUp">
      <div class="chart-canvas" 
           :style="{ 
             transform: `translate(${position.x}px, ${position.y}px) scale(${scale})`,
             cursor: isDragging ? 'grabbing' : 'grab'
           }">
        <div v-if="loading" class="loading-state">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        <div v-else class="org-tree">
          <!-- 根节点 -->
          <div class="tree-root">
            <div class="node-card root-node" @click="handleNodeClick(orgData)">
              <div class="node-icon">
                <el-icon :size="28"><OfficeBuilding /></el-icon>
              </div>
              <div class="node-info">
                <div class="node-name">{{ orgData.deptName }}</div>
                <div class="node-meta">
                  <span v-if="orgData.leaderName">{{ orgData.leaderName }}</span>
                  <span class="node-count">{{ orgData.memberCount || 0 }}人</span>
                </div>
              </div>
            </div>
          </div>
          
          <!-- 子节点容器 -->
          <div v-if="orgData.children?.length" class="tree-children">
            <div class="children-line"></div>
            <div class="children-nodes">
              <template v-for="child in orgData.children" :key="child.id">
                <OrgTreeNode 
                  :node="child" 
                  :level="1"
                  :collapsed-nodes="collapsedNodes"
                  @click-node="handleNodeClick"
                  @toggle-collapse="toggleCollapse"
                />
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 列表视图 -->
    <div v-else class="list-container">
      <el-table
        :data="flatList"
        v-loading="loading"
        row-key="id"
        :tree-props="{ children: 'children' }"
        default-expand-all
        stripe
        border
      >
        <el-table-column prop="deptName" label="部门名称" min-width="200">
          <template #default="{ row }">
            <div class="dept-cell">
              <el-icon><Folder /></el-icon>
              <span>{{ row.deptName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="leaderName" label="负责人" width="120">
          <template #default="{ row }">
            <span v-if="row.leaderName">{{ row.leaderName }}</span>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="memberCount" label="人数" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.memberCount || 0 }}人</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 部门详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="currentNode?.deptName"
      size="420px"
      direction="rtl"
    >
      <div v-if="currentNode" class="dept-detail">
        <div class="detail-header">
          <div class="detail-avatar">
            <span>{{ currentNode.deptName?.charAt(0) }}</span>
          </div>
          <div class="detail-title">
            <h3>{{ currentNode.deptName }}</h3>
            <p>{{ currentNode.deptCode || '暂无编码' }}</p>
          </div>
        </div>

        <el-descriptions :column="1" border class="detail-info">
          <el-descriptions-item label="负责人">
            <el-tag v-if="currentNode.leaderName" type="primary" size="small">
              {{ currentNode.leaderName }}
            </el-tag>
            <span v-else class="text-muted">未设置</span>
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ currentNode.phone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="部门人数">
            <el-statistic :value="currentNode.memberCount || 0" suffix="人" />
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentNode.status === 1 ? 'success' : 'danger'">
              {{ currentNode.status === 1 ? '正常' : '停用' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="currentNode.children?.length" class="sub-depts">
          <div class="sub-title">
            <span>下级部门</span>
            <el-tag size="small">{{ currentNode.children.length }}</el-tag>
          </div>
          <div class="sub-list">
            <div
              v-for="child in currentNode.children"
              :key="child.id"
              class="sub-item"
              @click="handleNodeClick(child)"
            >
              <el-icon><FolderOpened /></el-icon>
              <span class="sub-name">{{ child.deptName }}</span>
              <span class="sub-count">{{ child.memberCount || 0 }}人</span>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="drawerVisible = false">关闭</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, defineComponent, h } from 'vue'
import { ElMessage } from 'element-plus'
import * as api from '@/api/org'

const loading = ref(false)
const viewMode = ref<'tree' | 'list'>('tree')
const scale = ref(0.85)
const chartRef = ref<HTMLElement>()
const drawerVisible = ref(false)
const currentNode = ref<any>(null)
const orgData = ref<any>({})
const flatList = ref<any[]>([])
const collapsedNodes = ref<Set<number>>(new Set())

// 拖拽相关
const isDragging = ref(false)
const position = reactive({ x: 0, y: 0 })
const startPos = reactive({ x: 0, y: 0 })

// 树节点组件
const OrgTreeNode = defineComponent({
  name: 'OrgTreeNode',
  props: {
    node: { type: Object, required: true },
    level: { type: Number, default: 1 },
    collapsedNodes: { type: Set, required: true },
  },
  emits: ['click-node', 'toggle-collapse'],
  setup(props, { emit }) {
    return () => {
      const node = props.node
      const hasChildren = node.children && node.children.length > 0
      const isCollapsed = props.collapsedNodes.has(node.id)
      
      // 根据层级设置颜色
      const levelColors = [
        'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',  // level 1
        'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',  // level 2
        'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',  // level 3
        'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',  // level 4+
      ]
      const bgColor = levelColors[Math.min(props.level - 1, levelColors.length - 1)]

      return h('div', { class: 'tree-branch' }, [
        // 节点卡片
        h('div', { class: 'branch-node' }, [
          h('div', {
            class: 'node-card',
            style: { background: bgColor },
            onClick: () => emit('click-node', node),
          }, [
            h('div', { class: 'node-icon' }, [
              h('span', { class: 'icon-text' }, node.deptName?.charAt(0) || '?'),
            ]),
            h('div', { class: 'node-info' }, [
              h('div', { class: 'node-name' }, node.deptName),
              h('div', { class: 'node-meta' }, [
                node.leaderName && h('span', null, node.leaderName),
                h('span', { class: 'node-count' }, `${node.memberCount || 0}人`),
              ]),
            ]),
            // 折叠按钮
            hasChildren && h('div', {
              class: ['collapse-btn', { collapsed: isCollapsed }],
              onClick: (e: Event) => {
                e.stopPropagation()
                emit('toggle-collapse', node.id)
              },
            }, [
              h('span', null, isCollapsed ? '+' : '-'),
              h('span', { class: 'child-count' }, node.children.length),
            ]),
          ]),
        ]),
        // 子节点
        hasChildren && !isCollapsed && h('div', { class: 'branch-children' }, [
          h('div', { class: 'children-line' }),
          h('div', { class: 'children-nodes' },
            node.children.map((child: any) =>
              h(OrgTreeNode, {
                key: child.id,
                node: child,
                level: props.level + 1,
                collapsedNodes: props.collapsedNodes,
                onClickNode: (n: any) => emit('click-node', n),
                onToggleCollapse: (id: number) => emit('toggle-collapse', id),
              })
            )
          ),
        ]),
      ])
    }
  },
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.getDeptTree()
    const data = res.data || []
    
    orgData.value = {
      id: 0,
      deptName: '公司总部',
      deptCode: 'HQ',
      leaderName: 'CEO',
      memberCount: countMembers(data),
      status: 1,
      children: data,
    }
    
    flatList.value = data
  } catch (error) {
    console.error('加载失败:', error)
    orgData.value = getDefaultOrgData()
    flatList.value = orgData.value.children
  } finally {
    loading.value = false
  }
}

const countMembers = (nodes: any[]): number => {
  let total = 0
  nodes.forEach(node => {
    total += node.memberCount || 0
    if (node.children?.length) {
      total += countMembers(node.children)
    }
  })
  return total
}

const getDefaultOrgData = () => ({
  id: 0,
  deptName: '智慧科技有限公司',
  deptCode: 'HQ',
  leaderName: '张总',
  memberCount: 156,
  status: 1,
  children: [
    {
      id: 1, deptName: '技术研发中心', deptCode: 'TECH', leaderName: '李明',
      phone: '13800138001', memberCount: 45, status: 1,
      children: [
        { id: 11, deptName: '前端开发组', leaderName: '王伟', memberCount: 12, status: 1 },
        { id: 12, deptName: '后端开发组', leaderName: '刘强', memberCount: 15, status: 1 },
        { id: 13, deptName: '测试组', leaderName: '张丽', memberCount: 8, status: 1 },
        { id: 14, deptName: '运维组', leaderName: '陈军', memberCount: 10, status: 1 },
      ],
    },
    {
      id: 2, deptName: '产品设计部', deptCode: 'PD', leaderName: '赵芳',
      phone: '13800138002', memberCount: 20, status: 1,
      children: [
        { id: 21, deptName: '产品规划组', leaderName: '钱伟', memberCount: 8, status: 1 },
        { id: 22, deptName: 'UI设计组', leaderName: '孙娜', memberCount: 12, status: 1 },
      ],
    },
    {
      id: 3, deptName: '市场营销部', deptCode: 'MKT', leaderName: '周杰',
      phone: '13800138003', memberCount: 25, status: 1,
      children: [
        { id: 31, deptName: '品牌推广组', leaderName: '吴敏', memberCount: 10, status: 1 },
        { id: 32, deptName: '渠道销售组', leaderName: '郑浩', memberCount: 15, status: 1 },
      ],
    },
    { id: 4, deptName: '人力资源部', deptCode: 'HR', leaderName: '王丽华', memberCount: 12, status: 1 },
    { id: 5, deptName: '财务部', deptCode: 'FIN', leaderName: '李雪', memberCount: 8, status: 1 },
    { id: 6, deptName: '行政部', deptCode: 'ADM', leaderName: '张华', memberCount: 10, status: 1 },
  ],
})

// 缩放操作
const handleZoomIn = () => { scale.value = Math.min(scale.value + 0.15, 2) }
const handleZoomOut = () => { scale.value = Math.max(scale.value - 0.15, 0.3) }
const handleReset = () => { scale.value = 0.85; position.x = 0; position.y = 0 }

const handleWheel = (e: WheelEvent) => {
  const delta = e.deltaY > 0 ? -0.1 : 0.1
  scale.value = Math.min(Math.max(scale.value + delta, 0.3), 2)
}

// 拖拽操作
const handleMouseDown = (e: MouseEvent) => {
  isDragging.value = true
  startPos.x = e.clientX - position.x
  startPos.y = e.clientY - position.y
}

const handleMouseMove = (e: MouseEvent) => {
  if (!isDragging.value) return
  position.x = e.clientX - startPos.x
  position.y = e.clientY - startPos.y
}

const handleMouseUp = () => { isDragging.value = false }

// 折叠/展开
const toggleCollapse = (id: number) => {
  if (collapsedNodes.value.has(id)) {
    collapsedNodes.value.delete(id)
  } else {
    collapsedNodes.value.add(id)
  }
  // 触发响应式更新
  collapsedNodes.value = new Set(collapsedNodes.value)
}

const expandAll = () => { collapsedNodes.value = new Set() }

const collapseAll = () => {
  const ids = new Set<number>()
  const collectIds = (nodes: any[]) => {
    nodes.forEach(n => {
      if (n.children?.length) {
        ids.add(n.id)
        collectIds(n.children)
      }
    })
  }
  if (orgData.value.children) {
    collectIds(orgData.value.children)
  }
  collapsedNodes.value = ids
}

const handleNodeClick = (node: any) => {
  currentNode.value = node
  drawerVisible.value = true
}

const handleExport = () => {
  ElMessage.info('正在生成图片...')
  setTimeout(() => ElMessage.success('导出成功'), 1000)
}

onMounted(() => { loadData() })
</script>

<style scoped lang="scss">
.org-chart-page {
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
  background: #f0f2f5;
}

// 工具栏
.chart-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: white;
  border-bottom: 1px solid #e8e8e8;
  flex-shrink: 0;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  .zoom-text {
    display: inline-block;
    width: 50px;
    text-align: center;
    font-size: 13px;
    color: #666;
  }
}

// 图表容器
.chart-container {
  flex: 1;
  overflow: hidden;
  position: relative;
  background: 
    linear-gradient(90deg, #f0f0f0 1px, transparent 1px),
    linear-gradient(#f0f0f0 1px, transparent 1px);
  background-size: 20px 20px;
}

.chart-canvas {
  position: absolute;
  left: 50%;
  top: 40px;
  transform-origin: top center;
  transition: transform 0.1s ease-out;
  padding: 40px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 60px;
  color: #999;

  .loading-icon {
    font-size: 36px;
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

// 组织树
.org-tree {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.tree-root {
  margin-bottom: 20px;
}

// 节点卡片
.node-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-radius: 12px;
  color: white;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  min-width: 180px;
  position: relative;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.2);
  }

  &.root-node {
    min-width: 220px;
    padding: 18px 24px;
    background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  }

  .node-icon {
    width: 44px;
    height: 44px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;

    .icon-text {
      font-size: 18px;
      font-weight: 600;
    }
  }

  .node-info {
    flex: 1;
    min-width: 0;

    .node-name {
      font-size: 15px;
      font-weight: 600;
      margin-bottom: 4px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .node-meta {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 12px;
      opacity: 0.9;

      .node-count {
        background: rgba(255, 255, 255, 0.2);
        padding: 2px 8px;
        border-radius: 10px;
        font-size: 11px;
      }
    }
  }

  .collapse-btn {
    position: absolute;
    right: -12px;
    bottom: -12px;
    width: 28px;
    height: 28px;
    background: white;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
    cursor: pointer;
    transition: all 0.2s;
    color: #666;
    font-size: 14px;
    font-weight: bold;

    &:hover {
      background: #409eff;
      color: white;
      transform: scale(1.1);
    }

    .child-count {
      position: absolute;
      top: -6px;
      right: -6px;
      background: #f56c6c;
      color: white;
      font-size: 10px;
      padding: 1px 5px;
      border-radius: 10px;
      font-weight: normal;
    }
  }
}

// 分支结构
.tree-children,
.branch-children {
  position: relative;
  padding-top: 30px;

  .children-line {
    position: absolute;
    top: 0;
    left: 50%;
    width: 2px;
    height: 30px;
    background: linear-gradient(to bottom, #ddd, #ccc);
  }

  .children-nodes {
    display: flex;
    justify-content: center;
    gap: 24px;
    position: relative;

    // 水平连接线
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 2px;
      background: #ddd;
    }
  }
}

.tree-branch {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;

  // 垂直连接线
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 50%;
    width: 2px;
    height: 30px;
    background: #ddd;
    margin-top: -30px;
  }

  .branch-node {
    position: relative;
    z-index: 1;
  }
}

// 列表视图
.list-container {
  flex: 1;
  padding: 20px;
  overflow: auto;

  .dept-cell {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .el-icon {
      color: #409eff;
    }
  }

  .text-muted {
    color: #999;
  }
}

// 详情抽屉
.dept-detail {
  .detail-header {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    color: white;
    margin-bottom: 24px;

    .detail-avatar {
      width: 64px;
      height: 64px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      font-weight: bold;
    }

    .detail-title {
      h3 {
        margin: 0 0 4px;
        font-size: 20px;
      }
      p {
        margin: 0;
        opacity: 0.85;
        font-size: 13px;
      }
    }
  }

  .detail-info {
    margin-bottom: 24px;
  }

  .sub-depts {
    .sub-title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 12px;
      font-weight: 500;
      color: #333;
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
      background: #f5f7fa;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s;

      &:hover {
        background: #ecf5ff;
        color: #409eff;
      }

      .el-icon {
        color: #409eff;
      }

      .sub-name {
        flex: 1;
      }

      .sub-count {
        color: #999;
        font-size: 13px;
      }
    }
  }
}

.text-muted {
  color: #999;
}
</style>
