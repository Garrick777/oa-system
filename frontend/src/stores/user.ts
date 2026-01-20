import { defineStore } from 'pinia'
import { ref } from 'vue'
import request from '@/utils/request'
import router from '@/router'

interface UserInfo {
  userId: number
  username: string
  realName: string
  avatar: string | null
  roleCode: string
  roleName: string
  deptId: number | null
  deptName: string | null
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const needChangePassword = ref<boolean>(false)

  // 登录
  async function login(data: { username: string; password: string }) {
    const res = await request.post('/auth/login', data)
    if (res.code === 200) {
      token.value = res.data.token
      localStorage.setItem('token', res.data.token)
      userInfo.value = {
        userId: res.data.userId,
        username: res.data.username,
        realName: res.data.realName,
        avatar: res.data.avatar,
        roleCode: res.data.roleCode,
        roleName: res.data.roleName,
        deptId: res.data.deptId,
        deptName: res.data.deptName,
      }
      // 存储是否需要修改密码
      needChangePassword.value = res.data.needChangePassword || false
      return res.data
    }
    throw new Error(res.message)
  }

  // 首次修改密码
  async function firstChangePassword(newPassword: string) {
    const res = await request.put('/auth/first-password', { newPassword })
    if (res.code === 200) {
      needChangePassword.value = false
      return true
    }
    throw new Error(res.message)
  }

  // 获取用户信息
  async function getInfo() {
    if (!token.value) return null
    try {
      const res = await request.get('/auth/me')
      if (res.code === 200) {
        userInfo.value = {
          userId: res.data.id,
          username: res.data.username,
          realName: res.data.realName,
          avatar: res.data.avatar,
          roleCode: res.data.roleCode || 'employee',
          roleName: res.data.roleName || '普通员工',
          deptId: res.data.deptId,
          deptName: res.data.deptName,
        }
        return userInfo.value
      }
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
    return null
  }

  // 登出
  async function logout() {
    try {
      await request.post('/auth/logout')
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
      router.push('/login')
    }
  }

  // 检查是否登录
  function isLoggedIn() {
    return !!token.value
  }

  return {
    token,
    userInfo,
    needChangePassword,
    login,
    getInfo,
    logout,
    isLoggedIn,
    firstChangePassword,
  }
})
