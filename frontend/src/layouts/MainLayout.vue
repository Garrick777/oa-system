<template>
  <div class="app-layout" :class="{ 'sidebar-open': sidebarOpen }">
    <!-- 移动端遮罩 -->
    <transition name="fade">
      <div v-if="sidebarOpen && isMobile" class="sidebar-overlay" @click="closeSidebar"></div>
    </transition>

    <!-- 侧边栏 -->
    <aside class="sidebar" :class="{ collapsed: isCollapse && !isMobile }">
      <!-- Logo -->
      <div class="sidebar-logo">
        <div class="logo-icon">
          <svg viewBox="0 0 32 32" fill="none">
            <rect width="32" height="32" rx="8" fill="url(#nav-logo)"/>
            <path d="M10 16L14 20L22 12" stroke="white" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            <defs>
              <linearGradient id="nav-logo" x1="0" y1="0" x2="32" y2="32">
                <stop stop-color="#6366f1"/>
                <stop offset="1" stop-color="#a855f7"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <transition name="fade">
          <span v-show="!isCollapse || isMobile" class="logo-text">OA 办公</span>
        </transition>
        <!-- 移动端关闭按钮 -->
        <button v-if="isMobile" class="sidebar-close" @click="closeSidebar">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="18" y1="6" x2="6" y2="18"/>
            <line x1="6" y1="6" x2="18" y2="18"/>
          </svg>
        </button>
      </div>

      <!-- 动态导航菜单 -->
      <nav class="sidebar-nav">
        <template v-for="menu in filteredMenus" :key="menu.id">
          <!-- 有子菜单 - 可折叠 -->
          <div v-if="menu.children && menu.children.length > 0" class="nav-group">
            <div 
              class="nav-item nav-parent" 
              :class="{ 'is-open': expandedMenus.includes(menu.id), 'has-active': hasActiveChild(menu) }"
              @click="toggleMenu(menu.id)"
            >
              <div class="nav-icon">
                <MenuIcon :name="menu.icon || 'default'" />
              </div>
              <span class="nav-text">{{ menu.title }}</span>
              <svg class="nav-arrow" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </div>
            <transition name="slide">
              <div v-show="expandedMenus.includes(menu.id)" class="nav-children">
                <router-link
                  v-for="child in menu.children"
                  :key="child.id"
                  :to="child.path!"
                  class="nav-item nav-child"
                  :class="{ active: isActive(child.path!) }"
                  @click="handleNavClick"
                >
                  <span class="nav-dot"></span>
                  <span class="nav-text">{{ child.title }}</span>
                  <span v-if="child.badge" class="nav-badge">{{ child.badge }}</span>
                </router-link>
              </div>
            </transition>
          </div>
          <!-- 无子菜单的单项 -->
          <router-link
            v-else
            :to="menu.path!"
            class="nav-item"
            :class="{ active: isActive(menu.path!) }"
            @click="handleNavClick"
          >
            <div class="nav-icon">
              <MenuIcon :name="menu.icon || 'default'" />
            </div>
            <span class="nav-text">{{ menu.title }}</span>
            <span v-if="menu.badge" class="nav-badge">{{ menu.badge }}</span>
          </router-link>
        </template>
      </nav>

      <!-- 折叠按钮(仅桌面端) -->
      <div v-if="!isMobile" class="sidebar-toggle" @click="isCollapse = !isCollapse">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" :style="{ transform: isCollapse ? 'rotate(180deg)' : '' }">
          <polyline points="15 18 9 12 15 6"/>
        </svg>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶栏 -->
      <header class="topbar">
        <div class="topbar-left">
          <!-- 移动端菜单按钮 -->
          <button v-if="isMobile" class="menu-toggle" @click="toggleSidebar">
            <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="3" y1="12" x2="21" y2="12"/>
              <line x1="3" y1="6" x2="21" y2="6"/>
              <line x1="3" y1="18" x2="21" y2="18"/>
            </svg>
          </button>
          
          <div class="breadcrumb">
            <span class="breadcrumb-item">
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
                <polyline points="9 22 9 12 15 12 15 22"/>
              </svg>
            </span>
            <span class="breadcrumb-separator">/</span>
            <span class="breadcrumb-item current">{{ $route.meta.title || '工作台' }}</span>
          </div>
        </div>
        
        <div class="topbar-right">
          <!-- 搜索(仅桌面端) -->
          <div v-if="!isMobile" class="topbar-search">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <line x1="21" y1="21" x2="16.65" y2="16.65"/>
            </svg>
            <input type="text" placeholder="搜索..." />
          </div>

          <!-- 通知 -->
          <div class="topbar-icon" title="通知">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
            </svg>
            <span class="badge">3</span>
          </div>

          <!-- 用户菜单 -->
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-dropdown">
              <div class="user-avatar">
                {{ userStore.userInfo?.realName?.charAt(0) || 'U' }}
              </div>
              <div class="user-info" v-if="!isTablet">
                <span class="user-name">{{ userStore.userInfo?.realName || '用户' }}</span>
                <span class="user-role">{{ userStore.userInfo?.roleName || '员工' }}</span>
              </div>
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="6 9 12 15 18 9"/>
              </svg>
            </div>
            <template #dropdown>
              <el-dropdown-menu class="user-menu">
                <el-dropdown-item command="profile">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                  </svg>
                  修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
                    <polyline points="16 17 21 12 16 7"/>
                    <line x1="21" y1="12" x2="9" y2="12"/>
                  </svg>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch, defineComponent, h } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import type { MenuItem } from '@/config/menuConfig'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()

// 获取过滤后的菜单
const filteredMenus = computed(() => permissionStore.filteredMenus)

// 展开的菜单ID列表
const expandedMenus = ref<string[]>([])

// 响应式状态
const windowWidth = ref(window.innerWidth)
const isCollapse = ref(false)
const sidebarOpen = ref(false)

// 断点判断
const isMobile = computed(() => windowWidth.value < 768)
const isTablet = computed(() => windowWidth.value < 1024)

// 切换菜单展开/收起
const toggleMenu = (menuId: string) => {
  const index = expandedMenus.value.indexOf(menuId)
  if (index > -1) {
    expandedMenus.value.splice(index, 1)
  } else {
    expandedMenus.value.push(menuId)
  }
}

// 检查菜单是否有激活的子项
const hasActiveChild = (menu: MenuItem): boolean => {
  if (!menu.children) return false
  return menu.children.some(child => child.path && route.path.startsWith(child.path))
}

// 监听路由变化，自动展开当前菜单
watch(() => route.path, (newPath) => {
  filteredMenus.value.forEach(menu => {
    if (menu.children) {
      const hasActive = menu.children.some(child => child.path && newPath.startsWith(child.path))
      if (hasActive && !expandedMenus.value.includes(menu.id)) {
        expandedMenus.value.push(menu.id)
      }
    }
  })
}, { immediate: true })

// 监听窗口变化
const handleResize = () => {
  windowWidth.value = window.innerWidth
  if (isMobile.value) {
    sidebarOpen.value = false
  }
}

onMounted(() => {
  window.addEventListener('resize', handleResize)
  document.addEventListener('touchstart', handleTouchStart, { passive: true })
  document.addEventListener('touchmove', handleTouchMove, { passive: false })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  document.removeEventListener('touchstart', handleTouchStart)
  document.removeEventListener('touchmove', handleTouchMove)
})

// 触摸手势支持
let touchStartX = 0
let touchStartY = 0

const handleTouchStart = (e: TouchEvent) => {
  touchStartX = e.touches[0].clientX
  touchStartY = e.touches[0].clientY
}

const handleTouchMove = (e: TouchEvent) => {
  if (!isMobile.value) return
  
  const touchX = e.touches[0].clientX
  const touchY = e.touches[0].clientY
  const diffX = touchX - touchStartX
  const diffY = touchY - touchStartY
  
  if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > 50) {
    if (diffX > 0 && touchStartX < 30 && !sidebarOpen.value) {
      sidebarOpen.value = true
    } else if (diffX < 0 && sidebarOpen.value) {
      sidebarOpen.value = false
    }
  }
}

const isActive = (path: string) => route.path === path

const toggleSidebar = () => {
  sidebarOpen.value = !sidebarOpen.value
}

const closeSidebar = () => {
  sidebarOpen.value = false
}

const handleNavClick = () => {
  if (isMobile.value) {
    closeSidebar()
  }
}

const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/my/profile')
      break
    case 'password':
      router.push('/my/profile')
      break
    case 'logout':
      ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }).then(() => {
        userStore.logout()
      })
      break
  }
}

// 菜单图标组件
const MenuIcon = defineComponent({
  props: {
    name: { type: String, required: true }
  },
  setup(props) {
    const icons: Record<string, () => any> = {
      dashboard: () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
        h('rect', { x: 3, y: 3, width: 7, height: 9, rx: 1 }),
        h('rect', { x: 14, y: 3, width: 7, height: 5, rx: 1 }),
        h('rect', { x: 14, y: 12, width: 7, height: 9, rx: 1 }),
        h('rect', { x: 3, y: 16, width: 7, height: 5, rx: 1 }),
      ]),
      work: () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
        h('path', { d: 'M9 11l3 3L22 4' }),
        h('path', { d: 'M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11' }),
      ]),
      approve: () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
        h('path', { d: 'M22 11.08V12a10 10 0 1 1-5.93-9.14' }),
        h('polyline', { points: '22 4 12 14.01 9 11.01' }),
      ]),
      calendar: () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
        h('rect', { x: 3, y: 4, width: 18, height: 18, rx: 2, ry: 2 }),
        h('line', { x1: 16, y1: 2, x2: 16, y2: 6 }),
        h('line', { x1: 8, y1: 2, x2: 8, y2: 6 }),
        h('line', { x1: 3, y1: 10, x2: 21, y2: 10 }),
      ]),
      log: () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
        h('path', { d: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z' }),
        h('polyline', { points: '14 2 14 8 20 8' }),
        h('line', { x1: 16, y1: 13, x2: 8, y2: 13 }),
        h('line', { x1: 16, y1: 17, x2: 8, y2: 17 }),
      ]),
      attendance: () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
        h('circle', { cx: 12, cy: 12, r: 10 }),
        h('polyline', { points: '12 6 12 12 16 14' }),
      ]),
      notice: () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
        h('path', { d: 'M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9' }),
        h('path', { d: 'M13.73 21a2 2 0 0 1-3.46 0' }),
      ]),
      document: () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
        h('path', { d: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z' }),
        h('polyline', { points: '14 2 14 8 20 8' }),
      ]),
      team: () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
        h('path', { d: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2' }),
        h('circle', { cx: 9, cy: 7, r: 4 }),
        h('path', { d: 'M23 21v-2a4 4 0 0 0-3-3.87' }),
        h('path', { d: 'M16 3.13a4 4 0 0 1 0 7.75' }),
      ]),
      hr: () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
        h('path', { d: 'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2' }),
        h('circle', { cx: 9, cy: 7, r: 4 }),
        h('line', { x1: 19, y1: 8, x2: 19, y2: 14 }),
        h('line', { x1: 22, y1: 11, x2: 16, y2: 11 }),
      ]),
      org: () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
        h('rect', { x: 3, y: 3, width: 7, height: 7 }),
        h('rect', { x: 14, y: 3, width: 7, height: 7 }),
        h('rect', { x: 14, y: 14, width: 7, height: 7 }),
        h('rect', { x: 3, y: 14, width: 7, height: 7 }),
      ]),
      admin: () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
        h('circle', { cx: 12, cy: 12, r: 3 }),
        h('path', { d: 'M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09a1.65 1.65 0 0 0 1.51-1 1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z' }),
      ]),
      finance: () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
        h('line', { x1: 12, y1: 1, x2: 12, y2: 23 }),
        h('path', { d: 'M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6' }),
      ]),
      report: () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
        h('line', { x1: 18, y1: 20, x2: 18, y2: 10 }),
        h('line', { x1: 12, y1: 20, x2: 12, y2: 4 }),
        h('line', { x1: 6, y1: 20, x2: 6, y2: 14 }),
      ]),
      system: () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
        h('circle', { cx: 12, cy: 12, r: 3 }),
        h('path', { d: 'M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09a1.65 1.65 0 0 0 1.51-1 1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z' }),
      ]),
      workflow: () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
        h('polyline', { points: '22 12 18 12 15 21 9 3 6 12 2 12' }),
      ]),
      default: () => h('svg', { width: 20, height: 20, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': 2 }, [
        h('circle', { cx: 12, cy: 12, r: 10 }),
      ]),
    }
    
    return () => (icons[props.name] || icons.default)()
  }
})
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.app-layout {
  display: flex;
  min-height: 100vh;
  background: $bg-page;
  font-family: $font-family;
}

// 侧边栏遮罩
.sidebar-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  z-index: 99;
}

// 侧边栏
.sidebar {
  width: $sidebar-width;
  background: linear-gradient(180deg, #1e1b4b 0%, #0f172a 100%);
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  z-index: 100;
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1), transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.15);

  &.collapsed {
    width: $sidebar-collapsed-width;

    .nav-text, .nav-badge, .nav-arrow { opacity: 0; width: 0; overflow: hidden; }
    .logo-text { opacity: 0; width: 0; }
    .nav-item { justify-content: center; padding: 12px; }
    .nav-children { display: none; }
  }

  @media (max-width: 767px) {
    width: 280px;
    transform: translateX(-100%);
    
    .sidebar-open & {
      transform: translateX(0);
    }
  }
}

.sidebar-logo {
  height: $topbar-height;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 14px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;

  .logo-icon {
    width: 36px;
    height: 36px;
    flex-shrink: 0;

    svg { width: 100%; height: 100%; }
  }

  .logo-text {
    font-size: 18px;
    font-weight: 700;
    color: #fff;
    white-space: nowrap;
    transition: all 0.3s;
  }

  .sidebar-close {
    margin-left: auto;
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.1);
    border: none;
    border-radius: $radius-sm;
    color: #fff;
    cursor: pointer;
  }
}

.sidebar-nav {
  flex: 1;
  padding: 12px 10px;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;

  &::-webkit-scrollbar { width: 4px; }
  &::-webkit-scrollbar-thumb { background: rgba(255, 255, 255, 0.1); border-radius: 2px; }
}

// 菜单组
.nav-group {
  margin-bottom: 4px;
}

// 菜单项
.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  margin-bottom: 2px;
  border-radius: $radius-md;
  color: rgba(255, 255, 255, 0.65);
  text-decoration: none;
  transition: all 0.2s;
  position: relative;
  cursor: pointer;

  &:hover, &:active {
    background: rgba(255, 255, 255, 0.06);
    color: #fff;
  }

  &.active {
    background: linear-gradient(135deg, rgba(99, 102, 241, 0.25), rgba(168, 85, 247, 0.25));
    color: #fff;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 50%;
      transform: translateY(-50%);
      width: 3px;
      height: 20px;
      background: $primary-gradient;
      border-radius: 0 3px 3px 0;
    }

    .nav-icon { color: $primary-light; }
  }
}

// 父级菜单项
.nav-parent {
  user-select: none;

  &.has-active {
    color: #fff;
    .nav-icon { color: rgba(167, 139, 250, 0.8); }
  }

  &.is-open {
    background: rgba(255, 255, 255, 0.04);
    
    .nav-arrow {
      transform: rotate(180deg);
    }
  }
}

.nav-arrow {
  margin-left: auto;
  opacity: 0.5;
  transition: transform 0.3s ease;
  flex-shrink: 0;
}

// 子菜单容器
.nav-children {
  overflow: hidden;
  padding-left: 8px;
}

// 子菜单项
.nav-child {
  padding: 8px 14px 8px 20px;
  font-size: 13px;

  .nav-dot {
    width: 6px;
    height: 6px;
    background: rgba(255, 255, 255, 0.3);
    border-radius: 50%;
    flex-shrink: 0;
    transition: all 0.2s;
  }

  &:hover .nav-dot {
    background: rgba(255, 255, 255, 0.6);
  }

  &.active {
    background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(168, 85, 247, 0.2));
    
    .nav-dot {
      background: $primary-light;
      box-shadow: 0 0 8px rgba(167, 139, 250, 0.6);
    }

    &::before { display: none; }
  }
}

.nav-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-text {
  font-size: 14px;
  font-weight: 500;
  white-space: nowrap;
  transition: all 0.3s;
}

.nav-badge {
  margin-left: auto;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  border-radius: 9px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.sidebar-toggle {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  display: flex;
  justify-content: center;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.5);
  transition: all 0.2s;
  flex-shrink: 0;

  &:hover { color: #fff; background: rgba(255, 255, 255, 0.05); }
  svg { transition: transform 0.3s; }
}

// 下拉动画
.slide-enter-active,
.slide-leave-active {
  transition: all 0.25s ease;
  max-height: 500px;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  max-height: 0;
}

// 主容器
.main-container {
  flex: 1;
  margin-left: $sidebar-width;
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  min-width: 0;
  overflow: hidden;
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  .sidebar.collapsed ~ & { margin-left: $sidebar-collapsed-width; }

  @media (max-width: 767px) { margin-left: 0; }
}

// 顶栏
.topbar {
  height: $topbar-height;
  background: #fff;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  position: sticky;
  top: 0;
  z-index: 50;
  flex-shrink: 0;
  min-width: 0;
  width: 100%;
  box-sizing: border-box;

  @media (max-width: 767px) {
    height: $topbar-height-mobile;
    padding: 0 16px;
  }
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
  overflow: hidden;
}

.menu-toggle {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: none;
  border: none;
  border-radius: $radius-sm;
  color: $text-secondary;
  cursor: pointer;

  &:active { background: $bg-page; }
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 10px;
  color: $text-secondary;
  font-size: 14px;
  min-width: 0;
  overflow: hidden;

  .breadcrumb-item {
    display: flex;
    align-items: center;
    flex-shrink: 0;

    &.current {
      color: $text-primary;
      font-weight: 500;
      flex-shrink: 1;
      min-width: 0;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .breadcrumb-separator { color: $text-muted; flex-shrink: 0; }

  @media (max-width: 480px) {
    .breadcrumb-item:not(.current), .breadcrumb-separator { display: none; }
  }
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;

  @media (max-width: 767px) { gap: 8px; }
}

.topbar-search {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 16px;
  background: $bg-page;
  border-radius: $radius-lg;
  color: $text-muted;

  input {
    border: none;
    background: none;
    outline: none;
    font-size: 14px;
    width: 180px;
    color: $text-primary;

    &::placeholder { color: $text-muted; }
  }

  @media (max-width: 1200px) { input { width: 140px; } }
}

.topbar-icon {
  width: 42px;
  height: 42px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: $radius-md;
  color: $text-secondary;
  cursor: pointer;
  position: relative;
  transition: all 0.2s;

  &:hover, &:active { background: $bg-page; color: $primary-color; }

  .badge {
    position: absolute;
    top: 6px;
    right: 6px;
    min-width: 18px;
    height: 18px;
    padding: 0 4px;
    background: $danger-color;
    color: #fff;
    font-size: 11px;
    font-weight: 600;
    border-radius: 9px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  @media (max-width: 767px) { width: 38px; height: 38px; }
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 10px 6px 6px;
  border-radius: $radius-lg;
  cursor: pointer;
  transition: all 0.2s;

  &:hover, &:active { background: $bg-page; }

  @media (max-width: 767px) { padding: 4px; gap: 6px; }
}

.user-avatar {
  width: 38px;
  height: 38px;
  background: $primary-gradient;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 600;
  font-size: 15px;
  flex-shrink: 0;

  @media (max-width: 767px) { width: 34px; height: 34px; font-size: 14px; }
}

.user-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
  max-width: 120px;

  .user-name {
    font-size: 14px;
    font-weight: 600;
    color: $text-primary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .user-role {
    font-size: 12px;
    color: $text-muted;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  @media (max-width: 1200px) { max-width: 80px; }
}

:deep(.user-menu) {
  padding: 8px;
  min-width: 180px;

  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 14px;
    border-radius: $radius-sm;
    font-size: 14px;
    color: $text-secondary;

    &:hover { background: $bg-page; color: $primary-color; }
    svg { flex-shrink: 0; }
  }
}

// 主内容
.main-content {
  flex: 1;
  padding: 24px;
  overflow: auto;
  -webkit-overflow-scrolling: touch;
  min-width: 0;

  @media (max-width: 767px) { padding: 16px; }
  @media (max-width: 480px) { padding: 12px; }
}

// 动画
.page-enter-active, .page-leave-active { transition: all 0.25s ease; }
.page-enter-from { opacity: 0; transform: translateY(12px); }
.page-leave-to { opacity: 0; transform: translateY(-12px); }
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
