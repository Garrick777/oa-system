package com.oasystem.modules.workflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oasystem.modules.workflow.entity.ProcessDefinitionExt;
import com.oasystem.modules.workflow.entity.ProcessInstanceExt;
import com.oasystem.modules.workflow.entity.ApprovalRecord;

import java.util.List;
import java.util.Map;

/**
 * 流程服务接口
 */
public interface ProcessService {

    // ==================== 流程定义 ====================
    
    /**
     * 获取流程定义列表
     */
    IPage<ProcessDefinitionExt> getProcessDefinitions(int page, int size, String category, String keyword);

    /**
     * 获取流程定义详情
     */
    ProcessDefinitionExt getProcessDefinitionById(Long id);

    /**
     * 获取可发起的流程列表
     */
    List<ProcessDefinitionExt> getAvailableProcesses();

    /**
     * 部署流程定义
     */
    void deployProcess(String processKey, String bpmnXml);

    /**
     * 启用/停用流程
     */
    void toggleProcessStatus(Long id, Integer status);

    /**
     * 删除流程定义
     */
    void deleteProcess(Long id);

    // ==================== 流程实例 ====================

    /**
     * 发起流程
     */
    String startProcess(String processKey, String businessId, String businessType, String title, Map<String, Object> variables);

    /**
     * 获取我发起的流程
     */
    IPage<ProcessInstanceExt> getMyInitiatedProcesses(Long userId, int page, int size, String status, String keyword);

    /**
     * 获取流程实例详情
     */
    ProcessInstanceExt getProcessInstanceById(Long id);

    /**
     * 取消流程
     */
    void cancelProcess(String processInstanceId, String reason);

    // ==================== 待办/已办 ====================

    /**
     * 获取待办任务
     */
    IPage<Map<String, Object>> getTodoTasks(Long userId, int page, int size, String category, String keyword);

    /**
     * 获取已办任务
     */
    IPage<Map<String, Object>> getDoneTasks(Long userId, int page, int size, String category, String keyword);

    /**
     * 获取抄送给我的
     */
    IPage<Map<String, Object>> getCopyToMe(Long userId, int page, int size);

    // ==================== 审批操作 ====================

    /**
     * 审批通过
     */
    void approve(String taskId, String comment, Map<String, Object> variables);

    /**
     * 审批驳回
     */
    void reject(String taskId, String comment);

    /**
     * 退回上一步
     */
    void returnToPrevious(String taskId, String comment);

    /**
     * 转办
     */
    void delegate(String taskId, Long targetUserId, String comment);

    /**
     * 撤回
     */
    void withdraw(String processInstanceId, String reason);

    /**
     * 催办
     */
    void urge(String processInstanceId);

    // ==================== 流程跟踪 ====================

    /**
     * 获取流程进度
     */
    Map<String, Object> getProcessProgress(String processInstanceId);

    /**
     * 获取审批记录
     */
    List<ApprovalRecord> getApprovalRecords(String processInstanceId);

    /**
     * 获取流程图
     */
    byte[] getProcessDiagram(String processInstanceId);
}
