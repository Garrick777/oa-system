<template>
  <div class="page">
    <!-- 工具栏 -->
    <div class="header">
      <div class="tabs">
        <button :class="['tab', { active: view === 'chart' }]" @click="view = 'chart'">组织架构</button>
        <button :class="['tab', { active: view === 'table' }]" @click="view = 'table'">列表视图</button>
      </div>
      <div class="actions" v-if="view === 'chart'">
        <button class="btn" @click="zoomOut">−</button>
        <span class="zoom-text">{{ zoom }}%</span>
        <button class="btn" @click="zoomIn">+</button>
        <span class="sep"></span>
        <button class="btn link" @click="toggleAll(true)">展开</button>
        <button class="btn link" @click="toggleAll(false)">收起</button>
      </div>
    </div>

    <!-- 架构图 -->
    <div v-if="view === 'chart'" class="chart-area" ref="chartArea"
         @mousedown="startDrag" @mousemove="onDrag" @mouseup="endDrag" @mouseleave="endDrag"
         @wheel.prevent="onScroll">
      <div class="chart" :style="{ transform: `translate(${pos.x}px, ${pos.y}px) scale(${zoom / 100})` }">
        <div v-if="loading" class="loading">
          <span class="spinner"></span>
          加载中...
        </div>
        <template v-else>
          <TreeNode :data="tree" :expanded="expanded" @click="select" @toggle="toggle" :root="true" />
        </template>
      </div>
    </div>

    <!-- 列表 -->
    <div v-else class="table-area">
      <table class="table">
        <thead>
          <tr>
            <th>部门名称</th>
            <th>负责人</th>
            <th>人数</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in flatData" :key="row.id" @click="select(row)">
            <td>
              <span :style="{ paddingLeft: row.level * 20 + 'px' }">
                {{ row.children?.length ? '📂' : '📁' }} {{ row.deptName }}
              </span>
            </td>
            <td class="leader">{{ row.leaderName || '-' }}</td>
            <td>{{ row.memberCount || 0 }}</td>
            <td><span :class="['badge', row.status === 1 ? 'on' : 'off']">{{ row.status === 1 ? '正常' : '停用' }}</span></td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- 详情 -->
    <transition name="slide">
      <div v-if="current" class="panel">
        <div class="panel-head">
          <h3>{{ current.deptName }}</h3>
          <button class="close" @click="current = null">×</button>
        </div>
        <div class="panel-body">
          <div class="field"><label>编码</label><span>{{ current.deptCode || '-' }}</span></div>
          <div class="field"><label>负责人</label><span class="highlight">{{ current.leaderName || '未设置' }}</span></div>
          <div class="field"><label>人数</label><span>{{ current.memberCount || 0 }} 人</span></div>
          <div class="field"><label>状态</label><span :class="current.status === 1 ? 'green' : 'red'">{{ current.status === 1 ? '正常' : '停用' }}</span></div>
          
          <div v-if="current.children?.length" class="subs">
            <h4>下级部门</h4>
            <div v-for="c in current.children" :key="c.id" class="sub-item" @click="select(c)">
              <span>📁 {{ c.deptName }}</span>
              <span class="sub-count">{{ c.memberCount || 0 }}人 ›</span>
            </div>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, defineComponent, h } from 'vue'
import * as api from '@/api/org'

const loading = ref(true)
const view = ref('chart')
const zoom = ref(90)
const pos = reactive({ x: 0, y: 0 })
const dragging = ref(false)
const dragStart = reactive({ x: 0, y: 0 })
const current = ref<any>(null)
const tree = ref<any>({})
const expanded = ref<Set<number>>(new Set())

// 树节点组件
const TreeNode = defineComponent({
  name: 'TreeNode',
  props: ['data', 'expanded', 'root'],
  emits: ['click', 'toggle'],
  setup(props: any, { emit }) {
    return () => {
      const d = props.data
      if (!d?.deptName) return null
      const hasKids = d.children?.length > 0
      const open = props.expanded.has(d.id)

      return h('div', { class: 'node-wrap' }, [
        // 卡片
        h('div', { 
          class: ['card', props.root ? 'root' : ''],
          onClick: () => emit('click', d)
        }, [
          h('div', { class: 'avatar' }, props.root ? '🏢' : d.deptName?.charAt(0)),
          h('div', { class: 'info' }, [
            h('div', { class: 'name' }, d.deptName),
            h('div', { class: 'meta' }, [
              h('span', { class: 'leader-name' }, d.leaderName || '待定'),
              h('span', { class: 'count' }, `${d.memberCount || 0}人`)
            ])
          ]),
          hasKids && h('span', { 
            class: 'toggle',
            onClick: (e: Event) => { e.stopPropagation(); emit('toggle', d.id) }
          }, open ? '▼' : '▶')
        ]),
        // 子节点
        hasKids && open && h('div', { class: 'children' },
          d.children.map((c: any) => h(TreeNode, {
            key: c.id, data: c, expanded: props.expanded, root: false,
            onClick: (n: any) => emit('click', n),
            onToggle: (id: number) => emit('toggle', id)
          }))
        )
      ])
    }
  }
})

// 平铺数据
const flatData = computed(() => {
  const result: any[] = []
  const walk = (node: any, level: number) => {
    result.push({ ...node, level })
    node.children?.forEach((c: any) => walk(c, level + 1))
  }
  if (tree.value?.deptName) walk(tree.value, 0)
  return result
})

// 加载
const loadData = async () => {
  try {
    const res = await api.getDeptTree()
    if (res.data?.length) {
      tree.value = res.data[0]
      // 默认展开两级
      expanded.value.add(res.data[0].id)
      res.data[0].children?.forEach((c: any) => expanded.value.add(c.id))
    }
  } catch (e) {
    console.error(e)
    tree.value = defaultTree()
  } finally {
    loading.value = false
  }
}

const defaultTree = () => ({
  id: 1, deptName: '星辰科技有限公司', leaderName: 'CEO', memberCount: 128, status: 1,
  children: [
    { id: 2, deptName: '技术研发中心', leaderName: '李明', memberCount: 45, status: 1, children: [
      { id: 21, deptName: '前端组', leaderName: '王伟', memberCount: 15, status: 1 },
      { id: 22, deptName: '后端组', leaderName: '刘强', memberCount: 18, status: 1 }
    ]},
    { id: 3, deptName: '市场部', leaderName: '周杰', memberCount: 25, status: 1 },
    { id: 4, deptName: '人事部', leaderName: '王丽', memberCount: 12, status: 1 }
  ]
})

// 缩放
const zoomIn = () => { zoom.value = Math.min(zoom.value + 10, 150) }
const zoomOut = () => { zoom.value = Math.max(zoom.value - 10, 50) }
const onScroll = (e: WheelEvent) => {
  zoom.value = Math.min(Math.max(zoom.value + (e.deltaY > 0 ? -5 : 5), 50), 150)
}

// 拖拽
const startDrag = (e: MouseEvent) => {
  dragging.value = true
  dragStart.x = e.clientX - pos.x
  dragStart.y = e.clientY - pos.y
}
const onDrag = (e: MouseEvent) => {
  if (!dragging.value) return
  pos.x = e.clientX - dragStart.x
  pos.y = e.clientY - dragStart.y
}
const endDrag = () => { dragging.value = false }

// 展开
const toggle = (id: number) => {
  expanded.value.has(id) ? expanded.value.delete(id) : expanded.value.add(id)
  expanded.value = new Set(expanded.value)
}
const toggleAll = (open: boolean) => {
  if (open) {
    const add = (n: any) => { expanded.value.add(n.id); n.children?.forEach(add) }
    add(tree.value)
  } else {
    expanded.value.clear()
  }
  expanded.value = new Set(expanded.value)
}

const select = (node: any) => { current.value = node }

onMounted(loadData)
</script>

<style scoped>
.page {
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
  background: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif;
}

/* 头部 */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
}
.tabs { display: flex; gap: 4px; background: #f0f0f0; padding: 3px; border-radius: 8px; }
.tab {
  padding: 6px 16px;
  border: none;
  background: transparent;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
}
.tab.active { background: #fff; color: #333; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.actions { display: flex; align-items: center; gap: 8px; }
.btn {
  min-width: 28px;
  height: 28px;
  padding: 0 10px;
  border: 1px solid #ddd;
  background: #fff;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
}
.btn:hover { background: #f5f5f5; }
.btn.link { border: none; color: #007aff; }
.btn.link:hover { background: rgba(0,122,255,0.1); }
.zoom-text { font-size: 12px; color: #888; min-width: 36px; text-align: center; }
.sep { width: 1px; height: 20px; background: #e5e5e5; margin: 0 8px; }

/* 架构图区域 */
.chart-area {
  flex: 1;
  overflow: hidden;
  cursor: grab;
  background: linear-gradient(#f0f0f0 1px, transparent 1px),
              linear-gradient(90deg, #f0f0f0 1px, transparent 1px);
  background-size: 20px 20px;
}
.chart-area:active { cursor: grabbing; }
.chart {
  position: relative;
  left: 50%;
  top: 40px;
  transform-origin: top center;
  padding: 20px;
  display: inline-block;
}
.loading {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 40px;
  color: #888;
}
.spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #e5e5e5;
  border-top-color: #007aff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 树节点 */
.node-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  cursor: pointer;
  transition: all 0.2s;
  min-width: 180px;
  position: relative;
  border: 1px solid rgba(0,0,0,0.05);
}
.card:hover {
  box-shadow: 0 4px 16px rgba(0,0,0,0.12);
  transform: translateY(-2px);
}
.card.root {
  background: linear-gradient(135deg, #1a1a2e, #2d2d44);
  min-width: 200px;
}
.card.root .avatar { background: rgba(255,255,255,0.15); font-size: 22px; }
.card.root .name { color: #fff; }
.card.root .meta { color: rgba(255,255,255,0.7); }
.card.root .leader-name { color: #7dd3fc; }
.card.root .toggle { color: rgba(255,255,255,0.6); }

.avatar {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #007aff, #5856d6);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
}
.info { flex: 1; min-width: 0; }
.name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 2px;
  font-size: 12px;
  color: #888;
}
.leader-name { color: #007aff; font-weight: 500; }
.count {
  background: rgba(0,0,0,0.05);
  padding: 1px 6px;
  border-radius: 4px;
}
.toggle {
  font-size: 10px;
  color: #999;
  padding: 4px;
  cursor: pointer;
}
.toggle:hover { color: #333; }

/* 子节点 */
.children {
  display: flex;
  gap: 16px;
  margin-top: 24px;
  padding-top: 24px;
  position: relative;
}
.children::before {
  content: '';
  position: absolute;
  top: 0;
  left: 50%;
  width: 1px;
  height: 24px;
  background: #d5d5d5;
}
.children > .node-wrap {
  position: relative;
}
.children > .node-wrap::before {
  content: '';
  position: absolute;
  top: -24px;
  left: 50%;
  width: 1px;
  height: 24px;
  background: #d5d5d5;
}
.children > .node-wrap::after {
  content: '';
  position: absolute;
  top: -24px;
  left: 0;
  right: 0;
  height: 1px;
  background: #d5d5d5;
}
.children > .node-wrap:first-child::after { left: 50%; }
.children > .node-wrap:last-child::after { right: 50%; }
.children > .node-wrap:only-child::after { display: none; }

/* 表格 */
.table-area { flex: 1; padding: 20px; overflow: auto; }
.table {
  width: 100%;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  border-collapse: collapse;
  overflow: hidden;
}
.table th, .table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}
.table th { background: #fafafa; font-size: 12px; color: #888; font-weight: 500; }
.table tr:hover { background: #f9f9f9; cursor: pointer; }
.table .leader { color: #007aff; }
.badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}
.badge.on { background: #e8f5e9; color: #2e7d32; }
.badge.off { background: #ffebee; color: #c62828; }

/* 详情面板 */
.panel {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 340px;
  background: rgba(255,255,255,0.95);
  backdrop-filter: blur(20px);
  box-shadow: -2px 0 20px rgba(0,0,0,0.1);
  z-index: 100;
}
.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}
.panel-head h3 { margin: 0; font-size: 17px; }
.close {
  width: 28px;
  height: 28px;
  border: none;
  background: #f0f0f0;
  border-radius: 50%;
  font-size: 16px;
  cursor: pointer;
}
.close:hover { background: #e5e5e5; }
.panel-body { padding: 20px; }
.field {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f5f5f5;
}
.field label { color: #888; font-size: 14px; }
.field span { font-size: 14px; font-weight: 500; }
.field .highlight { color: #007aff; }
.field .green { color: #2e7d32; }
.field .red { color: #c62828; }
.subs { margin-top: 20px; }
.subs h4 { margin: 0 0 10px; font-size: 13px; color: #888; }
.sub-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 12px;
  background: #f9f9f9;
  border-radius: 8px;
  margin-bottom: 6px;
  cursor: pointer;
}
.sub-item:hover { background: #f0f0f0; }
.sub-count { color: #888; font-size: 12px; }

/* 动画 */
.slide-enter-active, .slide-leave-active { transition: transform 0.25s ease; }
.slide-enter-from, .slide-leave-to { transform: translateX(100%); }
</style>
