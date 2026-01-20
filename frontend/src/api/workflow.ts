import request from '@/utils/request';

// ==================== 流程定义 ====================

export function getProcessDefinitions(params: any) {
  return request.get('/workflow/process/definitions', { params });
}

export function getProcessDefinitionById(id: number) {
  return request.get(`/workflow/process/definitions/${id}`);
}

export function getAvailableProcesses() {
  return request.get('/workflow/process/definitions/available');
}

export function deployProcess(data: { processKey: string; bpmnXml: string }) {
  return request.post('/workflow/process/definitions/deploy', data);
}

export function toggleProcessStatus(id: number, status: number) {
  return request.put(`/workflow/process/definitions/${id}/status`, null, { params: { status } });
}

export function deleteProcessDefinition(id: number) {
  return request.delete(`/workflow/process/definitions/${id}`);
}

// ==================== 流程实例 ====================

export function getMyInitiatedProcesses(params: any) {
  return request.get('/workflow/process/instances/my', { params });
}

export function getProcessInstanceById(id: number) {
  return request.get(`/workflow/process/instances/${id}`);
}

export function cancelProcess(processInstanceId: string, reason: string) {
  return request.post(`/workflow/process/instances/${processInstanceId}/cancel`, { reason });
}

// ==================== 待办/已办 ====================

export function getTodoTasks(params: any) {
  return request.get('/workflow/process/tasks/todo', { params });
}

export function getDoneTasks(params: any) {
  return request.get('/workflow/process/tasks/done', { params });
}

export function getCopyToMe(params: any) {
  return request.get('/workflow/process/tasks/copy', { params });
}

// ==================== 审批操作 ====================

export function approveTask(taskId: string, data: { comment: string; variables?: any }) {
  return request.post(`/workflow/process/tasks/${taskId}/approve`, data);
}

export function rejectTask(taskId: string, comment: string) {
  return request.post(`/workflow/process/tasks/${taskId}/reject`, { comment });
}

export function returnTask(taskId: string, comment: string) {
  return request.post(`/workflow/process/tasks/${taskId}/return`, { comment });
}

export function delegateTask(taskId: string, targetUserId: number, comment: string) {
  return request.post(`/workflow/process/tasks/${taskId}/delegate`, { targetUserId, comment });
}

export function withdrawProcess(processInstanceId: string, reason: string) {
  return request.post(`/workflow/process/instances/${processInstanceId}/withdraw`, { reason });
}

export function urgeProcess(processInstanceId: string) {
  return request.post(`/workflow/process/instances/${processInstanceId}/urge`);
}

// ==================== 流程跟踪 ====================

export function getProcessProgress(processInstanceId: string) {
  return request.get(`/workflow/process/instances/${processInstanceId}/progress`);
}

export function getApprovalRecords(processInstanceId: string) {
  return request.get(`/workflow/process/instances/${processInstanceId}/records`);
}

export function getProcessDiagram(processInstanceId: string) {
  return request.get(`/workflow/process/instances/${processInstanceId}/diagram`, { responseType: 'blob' });
}

// ==================== 请假申请 ====================

export function getLeaveApplications(params: any) {
  return request.get('/workflow/leave', { params });
}

export function getLeaveApplicationById(id: number) {
  return request.get(`/workflow/leave/${id}`);
}

export function createLeaveApplication(data: any) {
  return request.post('/workflow/leave', data);
}

export function updateLeaveApplication(id: number, data: any) {
  return request.put(`/workflow/leave/${id}`, data);
}

export function submitLeaveApplication(id: number) {
  return request.post(`/workflow/leave/${id}/submit`);
}

export function cancelLeaveApplication(id: number, reason: string) {
  return request.post(`/workflow/leave/${id}/cancel`, { reason });
}

export function getLeaveBalance(year?: number) {
  return request.get('/workflow/leave/balance', { params: { year } });
}

export function calculateLeaveDuration(startTime: string, endTime: string) {
  return request.get('/workflow/leave/duration', { params: { startTime, endTime } });
}

// ==================== 报销申请 ====================

export function getExpenseApplications(params: any) {
  return request.get('/workflow/expense', { params });
}

export function getExpenseApplicationById(id: number) {
  return request.get(`/workflow/expense/${id}`);
}

export function createExpenseApplication(data: any) {
  return request.post('/workflow/expense', data);
}

export function updateExpenseApplication(id: number, data: any) {
  return request.put(`/workflow/expense/${id}`, data);
}

export function submitExpenseApplication(id: number) {
  return request.post(`/workflow/expense/${id}/submit`);
}

export function cancelExpenseApplication(id: number, reason: string) {
  return request.post(`/workflow/expense/${id}/cancel`, { reason });
}
