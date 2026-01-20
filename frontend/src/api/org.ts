import request from '@/utils/request'

// ==================== 部门管理 ====================

export function getDeptTree() {
  return request.get('/org/depts/tree')
}

export function getAllDepts() {
  return request.get('/org/depts')
}

export function getDept(id: number) {
  return request.get(`/org/depts/${id}`)
}

export function createDept(data: any) {
  return request.post('/org/depts', data)
}

export function updateDept(id: number, data: any) {
  return request.put(`/org/depts/${id}`, data)
}

export function deleteDept(id: number) {
  return request.delete(`/org/depts/${id}`)
}

// ==================== 岗位管理 ====================

export function getPositions(params: any) {
  return request.get('/org/positions', { params })
}

export function getAllPositions() {
  return request.get('/org/positions/all')
}

export function getPosition(id: number) {
  return request.get(`/org/positions/${id}`)
}

export function createPosition(data: any) {
  return request.post('/org/positions', data)
}

export function updatePosition(id: number, data: any) {
  return request.put(`/org/positions/${id}`, data)
}

export function deletePosition(id: number) {
  return request.delete(`/org/positions/${id}`)
}
