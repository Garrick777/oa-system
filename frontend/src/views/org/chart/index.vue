<template>
  <div class="org-page">
    <!-- 顶栏 -->
    <header class="topbar">
      <div class="left">
        <div class="view-tabs">
          <button :class="{ active: mode === 'chart' }" @click="mode = 'chart'">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
              <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
            </svg>
            架构图
          </button>
          <button :class="{ active: mode === 'list' }" @click="mode = 'list'">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="8" y1="6" x2="21" y2="6"/><line x1="8" y1="12" x2="21" y2="12"/>
              <line x1="8" y1="18" x2="21" y2="18"/><line x1="3" y1="6" x2="3.01" y2="6"/>
              <line x1="3" y1="12" x2="3.01" y2="12"/><line x1="3" y1="18" x2="3.01" y2="18"/>
            </svg>
            列表
          </button>
        </div>
      </div>
      <div class="right" v-if="mode === 'chart'">
        <div class="zoom-ctrl">
          <button @click="zoom = Math.max(50, zoom - 10)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
              <line x1="8" y1="11" x2="14" y2="11"/>
            </svg>
          </button>
          <span>{{ zoom }}%</span>
          <button @click="zoom = Math.min(150, zoom + 10)">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
              <line x1="11" y1="8" x2="11" y2="14"/><line x1="8" y1="11" x2="14" y2="11"/>
            </svg>
          </button>
        </div>
        <div class="sep"></div>
        <button class="text-btn" @click="expandAll">全部展开</button>
        <button class="text-btn" @click="collapseAll">全部收起</button>
      </div>
    </header>

    <!-- 架构图 -->
    <main v-if="mode === 'chart'" class="canvas-wrap" 
          @wheel.prevent="onWheel" @mousedown="onMouseDown" 
          @mousemove="onMouseMove" @mouseup="onMouseUp" @mouseleave="onMouseUp">
      <div class="canvas" :style="canvasStyle">
        <div v-if="loading" class="loader"><div class="spin"></div>加载中...</div>
        <div v-else class="tree">
          <Node :node="data" :exp="exp" @sel="onSelect" @tog="onToggle" root />
        </div>
      </div>
    </main>

    <!-- 列表 -->
    <main v-else class="list-wrap">
      <div class="list-table">
        <div class="tr head">
          <div class="td name">部门名称</div>
          <div class="td">负责人</div>
          <div class="td">人数</div>
          <div class="td">状态</div>
        </div>
        <div class="tr" v-for="r in flatList" :key="r.id" @click="onSelect(r)">
          <div class="td name" :style="{ paddingLeft: 16 + r.lv * 24 + 'px' }">
            <span class="icon">{{ r.children?.length ? '📂' : '📄' }}</span>
            {{ r.deptName }}
          </div>
          <div class="td leader">{{ r.leaderName || '-' }}</div>
          <div class="td">{{ r.memberCount || 0 }}</div>
          <div class="td">
            <span :class="['tag', r.status === 1 ? 'on' : 'off']">
              {{ r.status === 1 ? '正常' : '停用' }}
            </span>
          </div>
        </div>
      </div>
    </main>

    <!-- 侧边详情 -->
    <aside v-if="sel" class="sidebar">
      <div class="side-head">
        <div class="side-title">
          <span class="side-icon">🏢</span>
          <div>
            <h3>{{ sel.deptName }}</h3>
            <span class="code">{{ sel.deptCode || '-' }}</span>
          </div>
        </div>
        <button class="close-btn" @click="sel = null">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>
      <div class="side-body">
        <div class="info-grid">
          <div class="info-item">
            <span class="label">负责人</span>
            <span class="val primary">{{ sel.leaderName || '未设置' }}</span>
          </div>
          <div class="info-item">
            <span class="label">员工数</span>
            <span class="val">{{ sel.memberCount || 0 }} 人</span>
          </div>
          <div class="info-item full">
            <span class="label">状态</span>
            <span :class="['val', sel.status === 1 ? 'success' : 'danger']">
              {{ sel.status === 1 ? '正常运营' : '已停用' }}
            </span>
          </div>
        </div>
        <div v-if="sel.children?.length" class="child-section">
          <div class="section-title">下级部门 · {{ sel.children.length }}</div>
          <div class="child-list">
            <div v-for="c in sel.children" :key="c.id" class="child-card" @click="onSelect(c)">
              <div class="child-info">
                <span class="child-icon">📁</span>
                <span class="child-name">{{ c.deptName }}</span>
              </div>
              <div class="child-meta">
                <span>{{ c.memberCount || 0 }}人</span>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="9 18 15 12 9 6"/>
                </svg>
              </div>
            </div>
          </div>
        </div>
      </div>
    </aside>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, defineComponent, h } from 'vue'
import * as api from '@/api/org'

const loading = ref(true)
const mode = ref('chart')
const zoom = ref(80)
const pan = reactive({ x: 0, y: 0 })
const drag = reactive({ active: false, sx: 0, sy: 0 })
const sel = ref<any>(null)
const data = ref<any>({})
const exp = ref(new Set<number>())

const canvasStyle = computed(() => ({
  transform: `translate(${pan.x}px, ${pan.y}px) scale(${zoom.value / 100})`,
  cursor: drag.active ? 'grabbing' : 'grab'
}))

// 节点组件
const Node = defineComponent({
  name: 'Node',
  props: ['node', 'exp', 'root'],
  emits: ['sel', 'tog'],
  setup(props: any, { emit }) {
    return () => {
      const n = props.node
      if (!n?.deptName) return null
      const has = n.children?.length > 0
      const open = props.exp.has(n.id)

      // 不同层级不同颜色
      const colors: Record<string, string> = {
        root: '#1e293b',
        l1: '#3b82f6',
        l2: '#8b5cf6', 
        l3: '#06b6d4',
        l4: '#10b981'
      }
      const getLevel = (node: any, level = 0): number => {
        if (props.root) return 0
        return level
      }

      return h('div', { class: 'tree-row' }, [
        h('div', { 
          class: ['node-card', { root: props.root }],
          onClick: () => emit('sel', n)
        }, [
          h('div', { 
            class: 'node-avatar',
            style: { background: props.root ? colors.root : colors.l1 }
          }, props.root ? '🏛' : n.deptName.charAt(0)),
          h('div', { class: 'node-body' }, [
            h('div', { class: 'node-name' }, n.deptName),
            h('div', { class: 'node-info' }, [
              h('span', { class: 'node-leader' }, n.leaderName || '待定'),
              h('span', { class: 'node-badge' }, `${n.memberCount || 0}人`)
            ])
          ]),
          has && h('button', {
            class: ['node-toggle', { open }],
            onClick: (e: Event) => { e.stopPropagation(); emit('tog', n.id) }
          }, [
            h('svg', { width: 12, height: 12, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
              h('polyline', { points: '9 18 15 12 9 6' })
            ])
          ])
        ]),
        has && open && h('div', { class: 'node-kids' }, 
          n.children.map((c: any, i: number) => 
            h('div', { class: 'kid-wrap', key: c.id }, [
              h('div', { class: 'kid-line' }, [
                h('div', { class: 'line-h' }),
                h('div', { class: 'line-v', style: { 
                  top: i === 0 ? '50%' : '0',
                  bottom: i === n.children.length - 1 ? '50%' : '0'
                }})
              ]),
              h(Node, { node: c, exp: props.exp, root: false, onSel: (x: any) => emit('sel', x), onTog: (x: number) => emit('tog', x) })
            ])
          )
        )
      ])
    }
  }
})

const flatList = computed(() => {
  const arr: any[] = []
  const walk = (n: any, lv: number) => {
    arr.push({ ...n, lv })
    n.children?.forEach((c: any) => walk(c, lv + 1))
  }
  if (data.value?.deptName) walk(data.value, 0)
  return arr
})

const load = async () => {
  try {
    const res = await api.getDeptTree()
    if (res.data?.length) {
      data.value = res.data[0]
      exp.value.add(res.data[0].id)
      res.data[0].children?.forEach((c: any) => exp.value.add(c.id))
    }
  } catch (e) { console.error(e) }
  loading.value = false
}

const onWheel = (e: WheelEvent) => { zoom.value = Math.min(150, Math.max(50, zoom.value + (e.deltaY > 0 ? -5 : 5))) }
const onMouseDown = (e: MouseEvent) => { drag.active = true; drag.sx = e.clientX - pan.x; drag.sy = e.clientY - pan.y }
const onMouseMove = (e: MouseEvent) => { if (drag.active) { pan.x = e.clientX - drag.sx; pan.y = e.clientY - drag.sy } }
const onMouseUp = () => { drag.active = false }

const onToggle = (id: number) => { exp.value.has(id) ? exp.value.delete(id) : exp.value.add(id); exp.value = new Set(exp.value) }
const expandAll = () => { const add = (n: any) => { exp.value.add(n.id); n.children?.forEach(add) }; add(data.value); exp.value = new Set(exp.value) }
const collapseAll = () => { exp.value = new Set() }
const onSelect = (n: any) => { sel.value = n }

onMounted(load)
</script>

<style scoped>
* { box-sizing: border-box; }
.org-page {
  height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #f0f4f8 0%, #e2e8f0 100%);
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'PingFang SC', sans-serif;
  position: relative;
}

/* 顶栏 */
.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 56px;
  padding: 0 24px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
  flex-shrink: 0;
}
.left, .right { display: flex; align-items: center; gap: 12px; }
.view-tabs {
  display: flex;
  background: #f1f5f9;
  border-radius: 10px;
  padding: 4px;
}
.view-tabs button {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}
.view-tabs button.active {
  background: white;
  color: #1e293b;
  box-shadow: 0 1px 3px rgba(0,0,0,0.08);
}
.view-tabs button svg { opacity: 0.7; }
.view-tabs button.active svg { opacity: 1; }

.zoom-ctrl {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #f1f5f9;
  border-radius: 8px;
  padding: 4px;
}
.zoom-ctrl button {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  transition: all 0.15s;
}
.zoom-ctrl button:hover { background: white; color: #1e293b; }
.zoom-ctrl span { min-width: 44px; text-align: center; font-size: 12px; font-weight: 500; color: #64748b; }
.sep { width: 1px; height: 24px; background: #e2e8f0; }
.text-btn {
  padding: 8px 12px;
  border: none;
  background: transparent;
  font-size: 13px;
  font-weight: 500;
  color: #3b82f6;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.15s;
}
.text-btn:hover { background: #eff6ff; }

/* 画布 */
.canvas-wrap {
  flex: 1;
  overflow: hidden;
  position: relative;
}
.canvas {
  position: absolute;
  left: 60px;
  top: 50%;
  transform-origin: left center;
  padding: 40px;
}
.loader {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #64748b;
  font-size: 14px;
}
.spin {
  width: 24px;
  height: 24px;
  border: 3px solid #e2e8f0;
  border-top-color: #3b82f6;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin { to { transform: rotate(360deg); } }

/* 树 */
.tree { display: flex; }
.tree-row { display: flex; align-items: center; }

/* 节点卡片 */
.node-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  background: white;
  border-radius: 14px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 4px 12px rgba(0,0,0,0.04);
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  min-width: 180px;
  border: 1px solid rgba(0,0,0,0.04);
}
.node-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.08), 0 8px 24px rgba(0,0,0,0.06);
}
.node-card.root {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  padding: 16px 20px;
  min-width: 200px;
}
.node-card.root .node-avatar { background: rgba(255,255,255,0.12) !important; }
.node-card.root .node-name { color: white; }
.node-card.root .node-info { color: rgba(255,255,255,0.7); }
.node-card.root .node-leader { color: #7dd3fc !important; }
.node-card.root .node-badge { background: rgba(255,255,255,0.15); color: white; }
.node-card.root .node-toggle { color: rgba(255,255,255,0.6); }
.node-card.root .node-toggle:hover { background: rgba(255,255,255,0.1); color: white; }

.node-avatar {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: white;
  flex-shrink: 0;
}
.node-body { flex: 1; min-width: 0; }
.node-name {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
}
.node-info {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
  font-size: 12px;
  color: #64748b;
}
.node-leader { color: #3b82f6; font-weight: 500; }
.node-badge {
  background: #f1f5f9;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
}
.node-toggle {
  width: 28px;
  height: 28px;
  border: none;
  background: #f1f5f9;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  transition: all 0.2s;
  margin-left: 4px;
  flex-shrink: 0;
}
.node-toggle:hover { background: #e2e8f0; color: #1e293b; }
.node-toggle.open svg { transform: rotate(90deg); }
.node-toggle svg { transition: transform 0.2s; }

/* 子节点 */
.node-kids {
  display: flex;
  flex-direction: column;
  margin-left: 24px;
  position: relative;
}
.kid-wrap {
  display: flex;
  align-items: center;
  position: relative;
  padding: 8px 0;
}
.kid-line {
  width: 32px;
  height: 100%;
  position: relative;
  flex-shrink: 0;
}
.line-h {
  position: absolute;
  left: 0;
  top: 50%;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, #cbd5e1, #94a3b8);
  border-radius: 1px;
}
.line-v {
  position: absolute;
  left: 0;
  width: 2px;
  background: linear-gradient(180deg, #cbd5e1, #94a3b8);
  border-radius: 1px;
}

/* 列表 */
.list-wrap { flex: 1; padding: 24px; overflow: auto; }
.list-table {
  background: white;
  border-radius: 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.04), 0 4px 12px rgba(0,0,0,0.02);
  overflow: hidden;
}
.tr { display: flex; border-bottom: 1px solid #f1f5f9; }
.tr.head { background: #f8fafc; }
.tr:not(.head):hover { background: #f8fafc; cursor: pointer; }
.td { padding: 14px 16px; font-size: 13px; color: #475569; }
.td.name { flex: 2; display: flex; align-items: center; gap: 8px; font-weight: 500; color: #1e293b; }
.td.leader { color: #3b82f6; }
.td:not(.name) { flex: 1; }
.tr.head .td { font-size: 12px; font-weight: 600; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.5px; }
.icon { font-size: 16px; }
.tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 11px;
  font-weight: 600;
}
.tag.on { background: #dcfce7; color: #16a34a; }
.tag.off { background: #fee2e2; color: #dc2626; }

/* 侧边栏 */
.sidebar {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  width: 360px;
  background: white;
  box-shadow: -4px 0 24px rgba(0,0,0,0.08);
  z-index: 100;
  display: flex;
  flex-direction: column;
  animation: slideIn 0.25s ease;
}
@keyframes slideIn { from { transform: translateX(100%); } }
.side-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f1f5f9;
}
.side-title { display: flex; align-items: center; gap: 14px; }
.side-icon { font-size: 36px; }
.side-title h3 { margin: 0; font-size: 18px; font-weight: 600; color: #1e293b; }
.code { font-size: 12px; color: #94a3b8; }
.close-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: #f1f5f9;
  border-radius: 10px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  transition: all 0.15s;
}
.close-btn:hover { background: #e2e8f0; color: #1e293b; }
.side-body { flex: 1; overflow: auto; padding: 24px; }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.info-item { background: #f8fafc; border-radius: 12px; padding: 14px 16px; }
.info-item.full { grid-column: span 2; }
.info-item .label { display: block; font-size: 11px; font-weight: 600; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 6px; }
.info-item .val { font-size: 15px; font-weight: 600; color: #1e293b; }
.info-item .val.primary { color: #3b82f6; }
.info-item .val.success { color: #16a34a; }
.info-item .val.danger { color: #dc2626; }
.child-section { margin-top: 24px; }
.section-title { font-size: 12px; font-weight: 600; color: #94a3b8; text-transform: uppercase; letter-spacing: 0.5px; margin-bottom: 12px; }
.child-list { display: flex; flex-direction: column; gap: 8px; }
.child-card {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: #f8fafc;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.15s;
}
.child-card:hover { background: #eff6ff; }
.child-info { display: flex; align-items: center; gap: 10px; }
.child-icon { font-size: 18px; }
.child-name { font-size: 14px; font-weight: 500; color: #1e293b; }
.child-meta { display: flex; align-items: center; gap: 6px; font-size: 12px; color: #94a3b8; }
.child-meta svg { opacity: 0.5; }
</style>
