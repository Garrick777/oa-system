<template>
  <div class="org-page">
    <!-- 标题栏 -->
    <header class="page-header">
      <h1>组织架构</h1>
      <div class="actions">
        <button class="btn" @click="expandAll">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="6 9 12 15 18 9"/>
          </svg>
          全部展开
        </button>
        <button class="btn" @click="collapseAll">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="18 15 12 9 6 15"/>
          </svg>
          全部收起
        </button>
      </div>
    </header>

    <!-- 列表 -->
    <main class="list-container">
      <div class="list-card">
        <div class="list-header">
          <div class="col col-name">部门名称</div>
          <div class="col col-leader">负责人</div>
          <div class="col col-count">人数</div>
          <div class="col col-status">状态</div>
        </div>
        <div class="list-body" v-loading="loading">
          <template v-for="item in flatList" :key="item.id">
            <div class="list-row" @click="selectItem(item)">
              <div class="col col-name">
                <span class="indent" :style="{ width: item.level * 20 + 'px' }"></span>
                <button 
                  v-if="item.children?.length" 
                  class="expand-btn"
                  @click.stop="toggleExpand(item.id)"
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline :points="expanded.has(item.id) ? '6 9 12 15 18 9' : '9 18 15 12 9 6'"/>
                  </svg>
                </button>
                <span v-else class="expand-placeholder"></span>
                <span class="dept-icon">{{ item.children?.length ? '📂' : '📄' }}</span>
                <span class="dept-name">{{ item.deptName }}</span>
              </div>
              <div class="col col-leader">
                <span v-if="item.leaderName" class="leader">{{ item.leaderName }}</span>
                <span v-else class="empty">-</span>
              </div>
              <div class="col col-count">{{ item.memberCount || 0 }}</div>
              <div class="col col-status">
                <span :class="['status-tag', item.status === 1 ? 'active' : 'inactive']">
                  {{ item.status === 1 ? '正常' : '停用' }}
                </span>
              </div>
            </div>
          </template>
        </div>
      </div>
    </main>

    <!-- 详情侧边栏 -->
    <transition name="slide">
      <aside v-if="selected" class="detail-sidebar">
        <div class="sidebar-header">
          <div class="header-info">
            <span class="header-icon">🏢</span>
            <div class="header-text">
              <h3>{{ selected.deptName }}</h3>
              <span class="dept-code">{{ selected.deptCode || '-' }}</span>
            </div>
          </div>
          <button class="close-btn" @click="selected = null">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </button>
        </div>
        
        <div class="sidebar-body">
          <div class="info-section">
            <div class="info-row">
              <span class="label">负责人</span>
              <span class="value primary">{{ selected.leaderName || '未设置' }}</span>
            </div>
            <div class="info-row">
              <span class="label">员工人数</span>
              <span class="value">{{ selected.memberCount || 0 }} 人</span>
            </div>
            <div class="info-row">
              <span class="label">状态</span>
              <span :class="['value', selected.status === 1 ? 'success' : 'danger']">
                {{ selected.status === 1 ? '正常运营' : '已停用' }}
              </span>
            </div>
          </div>

          <div v-if="selected.children?.length" class="children-section">
            <h4>下级部门 · {{ selected.children.length }}</h4>
            <div class="children-list">
              <div 
                v-for="child in selected.children" 
                :key="child.id" 
                class="child-item"
                @click="selectItem(child)"
              >
                <div class="child-info">
                  <span class="child-icon">📁</span>
                  <span class="child-name">{{ child.deptName }}</span>
                </div>
                <div class="child-meta">
                  <span>{{ child.memberCount || 0 }}人</span>
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="9 18 15 12 9 6"/>
                  </svg>
                </div>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import * as api from '@/api/org'

const loading = ref(true)
const selected = ref<any>(null)
const treeData = ref<any>({})
const expanded = ref(new Set<number>())

// 展平树数据（根据展开状态）
const flatList = computed(() => {
  const result: any[] = []
  const walk = (node: any, level: number) => {
    result.push({ ...node, level })
    if (node.children?.length && expanded.value.has(node.id)) {
      node.children.forEach((c: any) => walk(c, level + 1))
    }
  }
  if (treeData.value?.deptName) walk(treeData.value, 0)
  return result
})

// 加载数据
const loadData = async () => {
  try {
    const res = await api.getDeptTree()
    if (res.data?.length) {
      treeData.value = res.data[0]
      // 默认展开前两级
      expanded.value.add(res.data[0].id)
      res.data[0].children?.forEach((c: any) => expanded.value.add(c.id))
    }
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

// 展开/收起
const toggleExpand = (id: number) => {
  if (expanded.value.has(id)) {
    expanded.value.delete(id)
  } else {
    expanded.value.add(id)
  }
  expanded.value = new Set(expanded.value)
}

const expandAll = () => {
  const addAll = (node: any) => {
    expanded.value.add(node.id)
    node.children?.forEach(addAll)
  }
  addAll(treeData.value)
  expanded.value = new Set(expanded.value)
}

const collapseAll = () => {
  expanded.value = new Set()
}

const selectItem = (item: any) => {
  selected.value = item
}

onMounted(loadData)
</script>

<style scoped>
.org-page {
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Microsoft YaHei', sans-serif;
  position: relative;
}

/* 标题栏 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: white;
  border-bottom: 1px solid #ebeef5;
}

.page-header h1 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.actions {
  display: flex;
  gap: 8px;
}

.btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border: 1px solid #dcdfe6;
  background: white;
  border-radius: 6px;
  font-size: 13px;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s;
}

.btn:hover {
  border-color: #409eff;
  color: #409eff;
}

.btn svg {
  opacity: 0.7;
}

/* 列表 */
.list-container {
  flex: 1;
  padding: 20px 24px;
  overflow: auto;
}

.list-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.list-header {
  display: flex;
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
}

.list-header .col {
  padding: 14px 16px;
  font-size: 13px;
  font-weight: 600;
  color: #909399;
}

.list-body {
  min-height: 200px;
}

.list-row {
  display: flex;
  border-bottom: 1px solid #f2f6fc;
  cursor: pointer;
  transition: background 0.15s;
}

.list-row:hover {
  background: #f5f7fa;
}

.list-row:last-child {
  border-bottom: none;
}

.col {
  padding: 14px 16px;
  font-size: 14px;
  color: #606266;
  display: flex;
  align-items: center;
}

.col-name {
  flex: 2.5;
  gap: 4px;
}

.col-leader {
  flex: 1;
}

.col-count {
  flex: 0.8;
}

.col-status {
  flex: 0.8;
}

.indent {
  display: inline-block;
  flex-shrink: 0;
}

.expand-btn {
  width: 22px;
  height: 22px;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  flex-shrink: 0;
  transition: all 0.15s;
}

.expand-btn:hover {
  background: #ecf5ff;
  color: #409eff;
}

.expand-placeholder {
  width: 22px;
  flex-shrink: 0;
}

.dept-icon {
  font-size: 16px;
  margin: 0 6px;
}

.dept-name {
  font-weight: 500;
  color: #303133;
}

.leader {
  color: #409eff;
}

.empty {
  color: #c0c4cc;
}

.status-tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.active {
  background: #f0f9eb;
  color: #67c23a;
}

.status-tag.inactive {
  background: #fef0f0;
  color: #f56c6c;
}

/* 侧边栏 */
.detail-sidebar {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 360px;
  background: white;
  box-shadow: -2px 0 12px rgba(0, 0, 0, 0.08);
  z-index: 100;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  font-size: 32px;
}

.header-text h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.dept-code {
  font-size: 12px;
  color: #909399;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #f5f7fa;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  transition: all 0.15s;
}

.close-btn:hover {
  background: #ecf5ff;
  color: #409eff;
}

.sidebar-body {
  flex: 1;
  overflow: auto;
  padding: 20px;
}

.info-section {
  background: #fafafa;
  border-radius: 8px;
  padding: 4px 0;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f2f6fc;
}

.info-row:last-child {
  border-bottom: none;
}

.info-row .label {
  font-size: 13px;
  color: #909399;
}

.info-row .value {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.info-row .value.primary {
  color: #409eff;
}

.info-row .value.success {
  color: #67c23a;
}

.info-row .value.danger {
  color: #f56c6c;
}

.children-section {
  margin-top: 20px;
}

.children-section h4 {
  margin: 0 0 12px;
  font-size: 13px;
  font-weight: 600;
  color: #909399;
}

.children-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.child-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 14px;
  background: #fafafa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.15s;
}

.child-item:hover {
  background: #ecf5ff;
}

.child-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.child-icon {
  font-size: 16px;
}

.child-name {
  font-size: 14px;
  color: #303133;
}

.child-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.child-meta svg {
  opacity: 0.5;
}

/* 动画 */
.slide-enter-active,
.slide-leave-active {
  transition: transform 0.25s ease;
}

.slide-enter-from,
.slide-leave-to {
  transform: translateX(100%);
}
</style>
