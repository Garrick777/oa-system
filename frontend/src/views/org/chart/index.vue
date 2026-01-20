<template>
  <div class="org-chart-page">
    <el-card class="toolbar-card">
      <div class="chart-toolbar">
        <div class="toolbar-left">
          <el-button-group>
            <el-button :type="viewMode === 'tree' ? 'primary' : 'default'" @click="viewMode = 'tree'">
              <el-icon><Share /></el-icon>树形图
            </el-button>
            <el-button :type="viewMode === 'list' ? 'primary' : 'default'" @click="viewMode = 'list'">
              <el-icon><List /></el-icon>列表视图
            </el-button>
          </el-button-group>
          <el-divider direction="vertical" />
          <el-button @click="handleZoomIn" :disabled="scale >= 1.5">
            <el-icon><ZoomIn /></el-icon>放大
          </el-button>
          <el-button @click="handleZoomOut" :disabled="scale <= 0.5">
            <el-icon><ZoomOut /></el-icon>缩小
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-button type="primary" @click="handleExport">
            <el-icon><Download /></el-icon>导出图片
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 树形图视图 -->
    <div v-if="viewMode === 'tree'" class="chart-container" ref="chartRef">
      <div class="org-tree-wrapper" :style="{ transform: `scale(${scale})` }">
        <div v-if="loading" class="loading-mask">
          <el-icon class="loading-icon"><Loading /></el-icon>
          <span>加载中...</span>
        </div>
        <div v-else class="org-tree">
          <OrgNode :node="orgData" @click-node="handleNodeClick" />
        </div>
      </div>
    </div>

    <!-- 列表视图 -->
    <el-card v-else class="list-view">
      <el-table
        :data="flatList"
        v-loading="loading"
        row-key="id"
        :tree-props="{ children: 'children' }"
        default-expand-all
        border
      >
        <el-table-column prop="deptName" label="部门名称" min-width="200" />
        <el-table-column prop="leaderName" label="负责人" width="100">
          <template #default="{ row }">
            <span v-if="row.leaderName">{{ row.leaderName }}</span>
            <span v-else class="text-muted">未设置</span>
          </template>
        </el-table-column>
        <el-table-column prop="memberCount" label="人数" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small">{{ row.memberCount || 0 }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 部门详情抽屉 -->
    <el-drawer
      v-model="drawerVisible"
      :title="currentNode?.deptName"
      size="400px"
      direction="rtl"
    >
      <div v-if="currentNode" class="dept-detail">
        <div class="detail-header">
          <div class="dept-icon">
            <el-icon :size="48"><OfficeBuilding /></el-icon>
          </div>
          <div class="dept-info">
            <h3>{{ currentNode.deptName }}</h3>
            <p>{{ currentNode.deptCode }}</p>
          </div>
        </div>

        <el-descriptions :column="1" border>
          <el-descriptions-item label="负责人">
            {{ currentNode.leaderName || '未设置' }}
          </el-descriptions-item>
          <el-descriptions-item label="联系电话">
            {{ currentNode.phone || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="邮箱">
            {{ currentNode.email || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="部门人数">
            <el-tag>{{ currentNode.memberCount || 0 }} 人</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="currentNode.status === 1 ? 'success' : 'danger'">
              {{ currentNode.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">
            {{ currentNode.createTime || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <div v-if="currentNode.children?.length" class="sub-depts">
          <h4>下级部门 ({{ currentNode.children.length }})</h4>
          <div class="sub-dept-list">
            <div
              v-for="child in currentNode.children"
              :key="child.id"
              class="sub-dept-item"
              @click="handleNodeClick(child)"
            >
              <el-icon><Folder /></el-icon>
              <span>{{ child.deptName }}</span>
              <el-tag size="small" type="info">{{ child.memberCount || 0 }}人</el-tag>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <el-button @click="drawerVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleViewMembers">查看成员</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, defineComponent, h } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import * as api from '@/api/org'

const router = useRouter()
const loading = ref(false)
const viewMode = ref<'tree' | 'list'>('tree')
const scale = ref(1)
const chartRef = ref<HTMLElement>()
const drawerVisible = ref(false)
const currentNode = ref<any>(null)
const orgData = ref<any>({})
const flatList = ref<any[]>([])

// 组织节点组件
const OrgNode = defineComponent({
  name: 'OrgNode',
  props: {
    node: {
      type: Object,
      required: true,
    },
  },
  emits: ['click-node'],
  setup(props, { emit }) {
    const handleClick = () => {
      emit('click-node', props.node)
    }

    return () => {
      const node = props.node
      if (!node || !node.deptName) return null

      const hasChildren = node.children && node.children.length > 0

      return h('div', { class: 'org-node-wrapper' }, [
        // 当前节点
        h('div', {
          class: ['org-node', { 'is-root': !node.parentId }],
          onClick: handleClick,
        }, [
          h('div', { class: 'node-avatar' }, [
            h('span', { class: 'avatar-icon' }, node.deptName.charAt(0)),
          ]),
          h('div', { class: 'node-content' }, [
            h('div', { class: 'node-name' }, node.deptName),
            h('div', { class: 'node-leader' }, node.leaderName || '负责人待定'),
            h('div', { class: 'node-count' }, `${node.memberCount || 0} 人`),
          ]),
        ]),
        // 子节点
        hasChildren && h('div', { class: 'org-children' },
          node.children.map((child: any) =>
            h(OrgNode, {
              key: child.id,
              node: child,
              onClickNode: (n: any) => emit('click-node', n),
            })
          )
        ),
      ])
    }
  },
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.getDeptTree()
    const data = res.data || []
    
    // 构建根节点
    orgData.value = {
      id: 0,
      deptName: '公司组织架构',
      deptCode: 'root',
      leaderName: 'CEO',
      memberCount: countMembers(data),
      status: 1,
      children: data,
    }
    
    flatList.value = data
  } catch (error) {
    console.error('加载数据失败:', error)
    // 使用默认数据
    orgData.value = getDefaultOrgData()
    flatList.value = orgData.value.children
  } finally {
    loading.value = false
  }
}

// 计算总人数
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

// 默认组织架构数据
const getDefaultOrgData = () => ({
  id: 0,
  deptName: '智慧科技有限公司',
  deptCode: 'root',
  leaderName: '张总',
  memberCount: 156,
  status: 1,
  children: [
    {
      id: 1,
      deptName: '技术研发中心',
      deptCode: 'tech',
      leaderName: '李明',
      phone: '13800138001',
      memberCount: 45,
      status: 1,
      children: [
        { id: 11, deptName: '前端开发组', deptCode: 'frontend', leaderName: '王伟', memberCount: 12, status: 1 },
        { id: 12, deptName: '后端开发组', deptCode: 'backend', leaderName: '刘强', memberCount: 15, status: 1 },
        { id: 13, deptName: '测试组', deptCode: 'qa', leaderName: '张丽', memberCount: 8, status: 1 },
        { id: 14, deptName: '运维组', deptCode: 'devops', leaderName: '陈军', memberCount: 10, status: 1 },
      ],
    },
    {
      id: 2,
      deptName: '产品设计部',
      deptCode: 'product',
      leaderName: '赵芳',
      phone: '13800138002',
      memberCount: 20,
      status: 1,
      children: [
        { id: 21, deptName: '产品规划组', deptCode: 'pm', leaderName: '钱伟', memberCount: 8, status: 1 },
        { id: 22, deptName: 'UI设计组', deptCode: 'ui', leaderName: '孙娜', memberCount: 12, status: 1 },
      ],
    },
    {
      id: 3,
      deptName: '市场营销部',
      deptCode: 'marketing',
      leaderName: '周杰',
      phone: '13800138003',
      memberCount: 25,
      status: 1,
      children: [
        { id: 31, deptName: '品牌推广组', deptCode: 'brand', leaderName: '吴敏', memberCount: 10, status: 1 },
        { id: 32, deptName: '渠道销售组', deptCode: 'sales', leaderName: '郑浩', memberCount: 15, status: 1 },
      ],
    },
    {
      id: 4,
      deptName: '人力资源部',
      deptCode: 'hr',
      leaderName: '王丽华',
      phone: '13800138004',
      memberCount: 12,
      status: 1,
    },
    {
      id: 5,
      deptName: '财务部',
      deptCode: 'finance',
      leaderName: '李雪',
      phone: '13800138005',
      memberCount: 8,
      status: 1,
    },
    {
      id: 6,
      deptName: '行政部',
      deptCode: 'admin',
      leaderName: '张华',
      phone: '13800138006',
      memberCount: 10,
      status: 1,
    },
  ],
})

const handleZoomIn = () => {
  scale.value = Math.min(scale.value + 0.1, 1.5)
}

const handleZoomOut = () => {
  scale.value = Math.max(scale.value - 0.1, 0.5)
}

const handleReset = () => {
  scale.value = 1
}

const handleNodeClick = (node: any) => {
  currentNode.value = node
  drawerVisible.value = true
}

const handleViewMembers = () => {
  if (currentNode.value) {
    router.push({
      path: '/org/employee',
      query: { deptId: currentNode.value.id },
    })
  }
}

const handleExport = () => {
  ElMessage.info('正在生成图片...')
  // 实际项目中可使用 html2canvas 等库实现导出
  setTimeout(() => {
    ElMessage.success('导出成功')
  }, 1000)
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.org-chart-page {
  padding: 20px;
  min-height: calc(100vh - 120px);
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
}

.toolbar-card {
  margin-bottom: 20px;
}

.chart-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;

  .toolbar-left {
    display: flex;
    align-items: center;
    gap: 8px;
  }
}

.chart-container {
  background: white;
  border-radius: 8px;
  padding: 40px;
  min-height: 600px;
  overflow: auto;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.org-tree-wrapper {
  display: flex;
  justify-content: center;
  transform-origin: top center;
  transition: transform 0.3s ease;
}

.loading-mask {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #909399;

  .loading-icon {
    font-size: 32px;
    animation: spin 1s linear infinite;
  }
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

// 组织树样式
.org-tree {
  display: flex;
  flex-direction: column;
  align-items: center;
}

:deep(.org-node-wrapper) {
  display: flex;
  flex-direction: column;
  align-items: center;
}

:deep(.org-node) {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
  min-width: 200px;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
  }

  &.is-root {
    background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    box-shadow: 0 4px 15px rgba(240, 147, 251, 0.4);

    &:hover {
      box-shadow: 0 6px 20px rgba(240, 147, 251, 0.5);
    }
  }

  .node-avatar {
    width: 48px;
    height: 48px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;

    .avatar-icon {
      font-size: 20px;
      font-weight: bold;
    }
  }

  .node-content {
    flex: 1;

    .node-name {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 4px;
    }

    .node-leader {
      font-size: 13px;
      opacity: 0.9;
    }

    .node-count {
      font-size: 12px;
      opacity: 0.8;
      margin-top: 2px;
    }
  }
}

:deep(.org-children) {
  display: flex;
  gap: 20px;
  padding-top: 30px;
  position: relative;

  // 连接线
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 50%;
    width: 2px;
    height: 30px;
    background: linear-gradient(to bottom, #667eea, #ddd);
  }

  > .org-node-wrapper {
    position: relative;

    // 水平连接线
    &::before {
      content: '';
      position: absolute;
      top: -30px;
      left: 50%;
      width: 100%;
      height: 2px;
      background: #ddd;
    }

    // 垂直连接线
    &::after {
      content: '';
      position: absolute;
      top: -30px;
      left: 50%;
      width: 2px;
      height: 30px;
      background: #ddd;
    }

    &:first-child::before {
      left: 50%;
      width: 50%;
    }

    &:last-child::before {
      width: 50%;
    }

    &:only-child::before {
      display: none;
    }
  }

  .org-node {
    background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
    box-shadow: 0 4px 15px rgba(17, 153, 142, 0.3);

    &:hover {
      box-shadow: 0 6px 20px rgba(17, 153, 142, 0.4);
    }
  }

  // 第三级节点样式
  .org-children .org-node {
    background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    box-shadow: 0 4px 15px rgba(79, 172, 254, 0.3);
    padding: 12px 16px;
    min-width: 160px;

    .node-avatar {
      width: 40px;
      height: 40px;

      .avatar-icon {
        font-size: 16px;
      }
    }

    .node-name {
      font-size: 14px;
    }

    .node-leader {
      font-size: 12px;
    }
  }
}

// 列表视图
.list-view {
  :deep(.el-table) {
    .el-table__row {
      cursor: pointer;
    }
  }
}

.text-muted {
  color: #909399;
}

// 部门详情
.dept-detail {
  .detail-header {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    color: white;
    margin-bottom: 20px;

    .dept-icon {
      width: 80px;
      height: 80px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .dept-info {
      h3 {
        margin: 0 0 4px;
        font-size: 20px;
      }

      p {
        margin: 0;
        opacity: 0.9;
      }
    }
  }

  .sub-depts {
    margin-top: 20px;

    h4 {
      margin: 0 0 12px;
      font-size: 15px;
      color: #303133;
    }

    .sub-dept-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .sub-dept-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 12px 16px;
      background: #f5f7fa;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background: #ecf5ff;
        color: #409eff;
      }

      span {
        flex: 1;
      }
    }
  }
}
</style>
