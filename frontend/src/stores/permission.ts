/**
 * 权限管理 Store
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { menuConfig, filterMenuByRole, type MenuItem } from '@/config/menuConfig'
import { useUserStore } from './user'

export const usePermissionStore = defineStore('permission', () => {
  // 原始菜单配置
  const allMenus = ref<MenuItem[]>(menuConfig)
  
  // 根据角色过滤后的菜单
  const filteredMenus = computed(() => {
    const userStore = useUserStore()
    const roleCode = userStore.userInfo?.roleCode || ''
    if (!roleCode) return []
    return filterMenuByRole(allMenus.value, roleCode)
  })

  // 检查是否有某个菜单的权限
  function hasMenuPermission(menuId: string): boolean {
    const userStore = useUserStore()
    const roleCode = userStore.userInfo?.roleCode || ''
    if (!roleCode) return false

    const findMenu = (menus: MenuItem[]): MenuItem | undefined => {
      for (const menu of menus) {
        if (menu.id === menuId) return menu
        if (menu.children) {
          const found = findMenu(menu.children)
          if (found) return found
        }
      }
      return undefined
    }

    const menu = findMenu(allMenus.value)
    return menu ? menu.roles.includes(roleCode) : false
  }

  // 检查是否有某个路径的权限
  function hasPathPermission(path: string): boolean {
    const userStore = useUserStore()
    const roleCode = userStore.userInfo?.roleCode || ''
    if (!roleCode) return false

    const findMenuByPath = (menus: MenuItem[]): MenuItem | undefined => {
      for (const menu of menus) {
        if (menu.path === path) return menu
        if (menu.children) {
          const found = findMenuByPath(menu.children)
          if (found) return found
        }
      }
      return undefined
    }

    const menu = findMenuByPath(allMenus.value)
    return menu ? menu.roles.includes(roleCode) : true // 未找到的路径默认允许
  }

  // 检查角色是否属于某类角色
  function hasRole(roles: string[]): boolean {
    const userStore = useUserStore()
    const roleCode = userStore.userInfo?.roleCode || ''
    return roles.includes(roleCode)
  }

  // 是否是管理员
  const isAdmin = computed(() => hasRole(['super_admin']))

  // 是否可以审批
  const canApprove = computed(() => 
    hasRole(['super_admin', 'company_leader', 'hr_admin', 'admin_officer', 'finance_admin', 'dept_manager'])
  )

  // 是否是HR
  const isHR = computed(() => hasRole(['super_admin', 'hr_admin']))

  // 是否是经理级别
  const isManager = computed(() => 
    hasRole(['super_admin', 'company_leader', 'hr_admin', 'admin_officer', 'finance_admin', 'dept_manager'])
  )

  return {
    allMenus,
    filteredMenus,
    hasMenuPermission,
    hasPathPermission,
    hasRole,
    isAdmin,
    canApprove,
    isHR,
    isManager,
  }
})
