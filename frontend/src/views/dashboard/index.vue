<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <div class="welcome-card">
      <div class="welcome-content">
        <div class="welcome-text">
          <h1>{{ greeting }}，{{ userStore.userInfo?.realName || '用户' }} 👋</h1>
          <p>欢迎回到 OA 办公系统，今天是 {{ currentDate }}</p>
        </div>
        <div class="welcome-illustration">
          <svg viewBox="0 0 200 160" fill="none">
            <circle cx="100" cy="80" r="60" fill="url(#g1)" opacity="0.2"/>
            <circle cx="100" cy="80" r="40" fill="url(#g1)" opacity="0.3"/>
            <path d="M70 80L90 100L130 60" stroke="white" stroke-width="6" stroke-linecap="round" stroke-linejoin="round"/>
            <defs>
              <linearGradient id="g1" x1="40" y1="20" x2="160" y2="140">
                <stop stop-color="#fff"/>
                <stop offset="1" stop-color="#fff" stop-opacity="0.2"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
      </div>
      <div class="welcome-stats">
        <div class="stat-item">
          <span class="stat-value">{{ todoCount }}</span>
          <span class="stat-label">待办事项</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ pendingApprovalCount }}</span>
          <span class="stat-label">待审批</span>
        </div>
        <div class="stat-item">
          <span class="stat-value">{{ unreadMessageCount }}</span>
          <span class="stat-label">未读消息</span>
        </div>
      </div>
    </div>

    <!-- 数据统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card" v-for="stat in stats" :key="stat.title">
        <div class="stat-card-icon" :style="{ background: stat.gradient }">
          <component :is="stat.icon" />
        </div>
        <div class="stat-card-info">
          <span class="stat-card-value">{{ stat.value }}</span>
          <span class="stat-card-title">{{ stat.title }}</span>
        </div>
        <div class="stat-card-trend" :class="stat.trend > 0 ? 'up' : 'down'">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline :points="stat.trend > 0 ? '18 15 12 9 6 15' : '6 9 12 15 18 9'"/>
          </svg>
          <span>{{ Math.abs(stat.trend) }}%</span>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="content-grid">
      <!-- 快捷入口 -->
      <div class="quick-actions">
        <div class="section-header">
          <h3>快捷入口</h3>
        </div>
        <div class="actions-grid">
          <router-link v-for="action in quickActions" :key="action.title" :to="action.path" class="action-item">
            <div class="action-icon" :style="{ background: action.color }">
              <component :is="action.icon" />
            </div>
            <span class="action-title">{{ action.title }}</span>
          </router-link>
        </div>
      </div>

      <!-- 待办事项 -->
      <div class="todo-list">
        <div class="section-header">
          <h3>待办事项</h3>
          <a href="#" class="view-all">查看全部</a>
        </div>
        <div class="todo-items">
          <div v-for="todo in todos" :key="todo.id" class="todo-item">
            <div class="todo-checkbox" :class="{ checked: todo.done }" @click="todo.done = !todo.done">
              <svg v-if="todo.done" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3">
                <polyline points="20 6 9 17 4 12"/>
              </svg>
            </div>
            <div class="todo-content" :class="{ done: todo.done }">
              <span class="todo-title">{{ todo.title }}</span>
              <span class="todo-time">{{ todo.time }}</span>
            </div>
            <div class="todo-priority" :class="todo.priority">
              {{ todo.priority === 'high' ? '紧急' : todo.priority === 'medium' ? '一般' : '低' }}
            </div>
          </div>
        </div>
      </div>

      <!-- 最近活动 -->
      <div class="recent-activity">
        <div class="section-header">
          <h3>最近活动</h3>
        </div>
        <div class="activity-timeline">
          <div v-for="activity in activities" :key="activity.id" class="activity-item">
            <div class="activity-dot" :style="{ background: activity.color }"></div>
            <div class="activity-content">
              <span class="activity-text">{{ activity.text }}</span>
              <span class="activity-time">{{ activity.time }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, h, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { getDashboardStats, getMyTodos, getRecentActivities } from '@/api/system'

const userStore = useUserStore()

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const currentDate = computed(() => {
  const d = new Date()
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return `${d.getMonth() + 1}月${d.getDate()}日 ${weekdays[d.getDay()]}`
})

// 统计图标组件
const UserIcon = () => h('svg', { width: 24, height: 24, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
  h('path', { d: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2' }),
  h('circle', { cx: 12, cy: 7, r: 4 })
])

const DeptIcon = () => h('svg', { width: 24, height: 24, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
  h('path', { d: 'M3 3h7v7H3zM14 3h7v7h-7zM14 14h7v7h-7zM3 14h7v7H3z' })
])

const TaskIcon = () => h('svg', { width: 24, height: 24, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
  h('path', { d: 'M9 11l3 3L22 4' }),
  h('path', { d: 'M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11' })
])

const ApprovalIcon = () => h('svg', { width: 24, height: 24, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
  h('path', { d: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z' }),
  h('polyline', { points: '14 2 14 8 20 8' }),
  h('line', { x1: 16, y1: 13, x2: 8, y2: 13 }),
  h('line', { x1: 16, y1: 17, x2: 8, y2: 17 }),
  h('polyline', { points: '10 9 9 9 8 9' })
])

// 响应式数据
const todoCount = ref(0)
const pendingApprovalCount = ref(0)
const unreadMessageCount = ref(0)

const stats = ref([
  { title: '员工总数', value: '0', trend: 12, gradient: 'linear-gradient(135deg, #6366f1, #8b5cf6)', icon: UserIcon },
  { title: '部门数量', value: '0', trend: 5, gradient: 'linear-gradient(135deg, #3b82f6, #06b6d4)', icon: DeptIcon },
  { title: '本月申请', value: '0', trend: -3, gradient: 'linear-gradient(135deg, #10b981, #34d399)', icon: TaskIcon },
  { title: '待审批', value: '0', trend: 8, gradient: 'linear-gradient(135deg, #f59e0b, #fbbf24)', icon: ApprovalIcon },
])

const quickActions = ref([
  { title: '新建审批', path: '/my/apply', color: 'linear-gradient(135deg, #6366f1, #8b5cf6)', icon: ApprovalIcon },
  { title: '发起会议', path: '/meeting/create', color: 'linear-gradient(135deg, #3b82f6, #06b6d4)', icon: DeptIcon },
  { title: '提交日志', path: '/log/create', color: 'linear-gradient(135deg, #10b981, #34d399)', icon: TaskIcon },
  { title: '考勤打卡', path: '/attendance', color: 'linear-gradient(135deg, #f59e0b, #fbbf24)', icon: UserIcon },
])

const todos = ref<any[]>([])
const activities = ref<any[]>([])

// 加载统计数据
const loadStats = async () => {
  try {
    const res: any = await getDashboardStats()
    if (res.code === 200 && res.data) {
      stats.value[0].value = String(res.data.employeeCount || 0)
      stats.value[1].value = String(res.data.departmentCount || 0)
      stats.value[2].value = String(res.data.monthlyLeaveCount || 0)
      stats.value[3].value = String(res.data.pendingApprovalCount || 0)
      pendingApprovalCount.value = res.data.pendingApprovalCount || 0
    }
  } catch (error) {
    console.error('加载统计数据失败', error)
  }
}

// 加载待办事项
const loadTodos = async () => {
  try {
    const res: any = await getMyTodos()
    if (res.code === 200 && res.data) {
      todos.value = res.data.map((item: any) => ({
        id: item.id,
        title: item.title,
        time: item.deadline,
        priority: item.priority === 'high' ? 'high' : item.priority === 'normal' ? 'medium' : 'low',
        done: false
      }))
      todoCount.value = todos.value.length
    }
  } catch (error) {
    console.error('加载待办事项失败', error)
    // 显示默认数据
    todos.value = [
      { id: 1, title: '暂无待办事项', time: '-', priority: 'low', done: false },
    ]
  }
}

// 加载最近活动
const loadActivities = async () => {
  try {
    const res: any = await getRecentActivities()
    if (res.code === 200 && res.data) {
      const colors = ['#6366f1', '#10b981', '#3b82f6', '#f59e0b', '#ef4444']
      activities.value = res.data.map((item: any, index: number) => ({
        id: item.id,
        text: item.content,
        time: item.time,
        color: colors[index % colors.length]
      }))
    }
  } catch (error) {
    console.error('加载最近活动失败', error)
    // 显示默认数据
    activities.value = [
      { id: 1, text: '欢迎使用OA办公系统', time: '刚刚', color: '#6366f1' },
    ]
  }
}

onMounted(() => {
  loadStats()
  loadTodos()
  loadActivities()
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.dashboard {
  max-width: 1400px;
  margin: 0 auto;
}

// 欢迎卡片
.welcome-card {
  background: $primary-gradient;
  border-radius: $radius-xl;
  padding: 32px;
  margin-bottom: 28px;
  color: #fff;
  position: relative;
  overflow: hidden;
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}

.welcome-text {
  h1 {
    font-size: 28px;
    font-weight: 700;
    margin-bottom: 8px;
  }

  p {
    font-size: 15px;
    opacity: 0.85;
  }
}

.welcome-illustration {
  width: 160px;
  height: 130px;
  opacity: 0.9;

  svg {
    width: 100%;
    height: 100%;
  }
}

.welcome-stats {
  display: flex;
  gap: 48px;

  .stat-item {
    display: flex;
    flex-direction: column;

    .stat-value {
      font-size: 32px;
      font-weight: 700;
    }

    .stat-label {
      font-size: 14px;
      opacity: 0.8;
    }
  }
}

// 统计卡片
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 28px;
}

.stat-card {
  background: #fff;
  border-radius: $radius-lg;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: $shadow-sm;
  transition: all $transition-normal;

  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-md;
  }
}

.stat-card-icon {
  width: 56px;
  height: 56px;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-card-info {
  flex: 1;

  .stat-card-value {
    display: block;
    font-size: 28px;
    font-weight: 700;
    color: $text-primary;
    line-height: 1.2;
  }

  .stat-card-title {
    font-size: 14px;
    color: $text-secondary;
  }
}

.stat-card-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 20px;

  &.up {
    color: $success-color;
    background: rgba(16, 185, 129, 0.1);
  }

  &.down {
    color: $danger-color;
    background: rgba(239, 68, 68, 0.1);
  }
}

// 内容网格
.content-grid {
  display: grid;
  grid-template-columns: 1fr 1.2fr 1fr;
  gap: 20px;
}

// 区块样式
.quick-actions,
.todo-list,
.recent-activity {
  background: #fff;
  border-radius: $radius-lg;
  padding: 24px;
  box-shadow: $shadow-sm;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;

  h3 {
    font-size: 16px;
    font-weight: 600;
    color: $text-primary;
  }

  .view-all {
    font-size: 13px;
    color: $primary-color;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }
}

// 快捷入口
.actions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 20px 16px;
  border-radius: $radius-md;
  background: $bg-page;
  text-decoration: none;
  transition: all $transition-normal;

  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-sm;
  }

  .action-icon {
    width: 48px;
    height: 48px;
    border-radius: $radius-md;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }

  .action-title {
    font-size: 13px;
    font-weight: 500;
    color: $text-primary;
  }
}

// 待办事项
.todo-items {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.todo-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  background: $bg-page;
  border-radius: $radius-md;
  transition: all $transition-fast;

  &:hover {
    background: rgba(99, 102, 241, 0.05);
  }
}

.todo-checkbox {
  width: 22px;
  height: 22px;
  border: 2px solid $border-color;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all $transition-fast;
  flex-shrink: 0;

  &:hover {
    border-color: $primary-color;
  }

  &.checked {
    background: $primary-color;
    border-color: $primary-color;
    color: #fff;
  }
}

.todo-content {
  flex: 1;
  min-width: 0;

  &.done .todo-title {
    text-decoration: line-through;
    color: $text-muted;
  }

  .todo-title {
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: $text-primary;
    margin-bottom: 2px;
  }

  .todo-time {
    font-size: 12px;
    color: $text-muted;
  }
}

.todo-priority {
  font-size: 11px;
  font-weight: 600;
  padding: 4px 10px;
  border-radius: 12px;
  text-transform: uppercase;

  &.high {
    background: rgba(239, 68, 68, 0.1);
    color: $danger-color;
  }

  &.medium {
    background: rgba(245, 158, 11, 0.1);
    color: $warning-color;
  }

  &.low {
    background: rgba(148, 163, 184, 0.1);
    color: $text-muted;
  }
}

// 最近活动
.activity-timeline {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-item {
  display: flex;
  gap: 14px;
  position: relative;

  &:not(:last-child)::before {
    content: '';
    position: absolute;
    left: 6px;
    top: 20px;
    width: 2px;
    height: calc(100% + 8px);
    background: $border-color;
  }
}

.activity-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 3px;
}

.activity-content {
  .activity-text {
    display: block;
    font-size: 14px;
    color: $text-primary;
    margin-bottom: 4px;
  }

  .activity-time {
    font-size: 12px;
    color: $text-muted;
  }
}

// 响应式
@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .content-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }

  .welcome-content {
    flex-direction: column;
    text-align: center;
  }

  .welcome-stats {
    justify-content: center;
  }
}
</style>
