import request from '@/utils/request'

// ==================== 用户管理 ====================

export function getUsers(params: any) {
  return request.get('/system/users', { params })
}

export function getUser(id: number) {
  return request.get(`/system/users/${id}`)
}

export function createUser(data: any) {
  return request.post('/system/users', data)
}

export function updateUser(id: number, data: any) {
  return request.put(`/system/users/${id}`, data)
}

export function deleteUser(id: number) {
  return request.delete(`/system/users/${id}`)
}

export function updateUserStatus(id: number, status: number) {
  return request.put(`/system/users/${id}/status`, null, { params: { status } })
}

export function resetUserPassword(id: number, newPassword: string = '123456') {
  return request.post(`/system/users/${id}/reset-password`, null, { params: { newPassword } })
}

// 用户导入
export function importUsers(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/system/users/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

// 用户导出
export function exportUsers(params: any) {
  return request.get('/system/users/export', {
    params,
    responseType: 'blob',
  })
}

// 下载导入模板
export function downloadUserTemplate() {
  return request.get('/system/users/template', {
    responseType: 'blob',
  })
}

// ==================== 系统配置 ====================

export function getConfigs(params?: any) {
  return request.get('/system/configs', { params })
}

export function getConfig(key: string) {
  return request.get(`/system/configs/${key}`)
}

export function updateConfig(key: string, value: string) {
  return request.put(`/system/configs/${key}`, { value })
}

export function updateConfigs(configs: { key: string; value: string }[]) {
  return request.put('/system/configs/batch', configs)
}

// ==================== 角色管理 ====================

export function getRoles(params: any) {
  return request.get('/system/roles', { params })
}

export function getAllRoles() {
  return request.get('/system/roles/all')
}

export function getRole(id: number) {
  return request.get(`/system/roles/${id}`)
}

export function createRole(data: any) {
  return request.post('/system/roles', data)
}

export function updateRole(id: number, data: any) {
  return request.put(`/system/roles/${id}`, data)
}

export function deleteRole(id: number) {
  return request.delete(`/system/roles/${id}`)
}

export function updateRoleStatus(id: number, status: number) {
  return request.put(`/system/roles/${id}/status`, null, { params: { status } })
}

export function getRoleMenus(id: number) {
  return request.get(`/system/roles/${id}/menus`)
}

export function assignRoleMenus(id: number, menuIds: number[]) {
  return request.put(`/system/roles/${id}/menus`, menuIds)
}

// ==================== 菜单管理 ====================

export function getMenuTree() {
  return request.get('/system/menus/tree')
}

export function getMenus() {
  return request.get('/system/menus')
}

export function createMenu(data: any) {
  return request.post('/system/menus', data)
}

export function updateMenu(id: number, data: any) {
  return request.put(`/system/menus/${id}`, data)
}

export function deleteMenu(id: number) {
  return request.delete(`/system/menus/${id}`)
}

// ==================== 字典管理 ====================

export function getDictTypes(params: any) {
  return request.get('/system/dicts/types', { params })
}

export function createDictType(data: any) {
  return request.post('/system/dicts/types', data)
}

export function updateDictType(id: number, data: any) {
  return request.put(`/system/dicts/types/${id}`, data)
}

export function deleteDictType(id: number) {
  return request.delete(`/system/dicts/types/${id}`)
}

export function getDictData(dictType: string) {
  return request.get(`/system/dicts/data/${dictType}`)
}

export function getDictDataList(params: any) {
  return request.get('/system/dicts/data', { params })
}

export function createDictData(data: any) {
  return request.post('/system/dicts/data', data)
}

export function updateDictData(id: number, data: any) {
  return request.put(`/system/dicts/data/${id}`, data)
}

export function deleteDictData(id: number) {
  return request.delete(`/system/dicts/data/${id}`)
}
