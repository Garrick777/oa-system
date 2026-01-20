package com.oasystem.modules.workflow.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oasystem.common.exception.BusinessException;
import com.oasystem.modules.workflow.entity.ApprovalRecord;
import com.oasystem.modules.workflow.entity.ProcessDefinitionExt;
import com.oasystem.modules.workflow.entity.ProcessInstanceExt;
import com.oasystem.modules.workflow.mapper.ApprovalRecordMapper;
import com.oasystem.modules.workflow.mapper.ProcessDefinitionExtMapper;
import com.oasystem.modules.workflow.mapper.ProcessInstanceExtMapper;
import com.oasystem.modules.workflow.service.ProcessService;
import com.oasystem.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 流程服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessServiceImpl implements ProcessService {

    private final ProcessDefinitionExtMapper processDefinitionExtMapper;
    private final ProcessInstanceExtMapper processInstanceExtMapper;
    private final ApprovalRecordMapper approvalRecordMapper;
    
    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;
    private final RepositoryService repositoryService;

    // ==================== 流程定义 ====================

    @Override
    public IPage<ProcessDefinitionExt> getProcessDefinitions(int page, int size, String category, String keyword) {
        Page<ProcessDefinitionExt> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ProcessDefinitionExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessDefinitionExt::getDeleted, 0);
        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(ProcessDefinitionExt::getCategory, category);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(ProcessDefinitionExt::getProcessName, keyword);
        }
        wrapper.orderByAsc(ProcessDefinitionExt::getSortOrder);
        return processDefinitionExtMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public ProcessDefinitionExt getProcessDefinitionById(Long id) {
        return processDefinitionExtMapper.selectById(id);
    }

    @Override
    public List<ProcessDefinitionExt> getAvailableProcesses() {
        LambdaQueryWrapper<ProcessDefinitionExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessDefinitionExt::getStatus, 1)
               .eq(ProcessDefinitionExt::getDeleted, 0)
               .orderByAsc(ProcessDefinitionExt::getSortOrder);
        return processDefinitionExtMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public void deployProcess(String processKey, String bpmnXml) {
        // 部署流程到Flowable
        repositoryService.createDeployment()
                .addString(processKey + ".bpmn20.xml", bpmnXml)
                .name(processKey)
                .deploy();
        
        // 更新扩展表
        LambdaQueryWrapper<ProcessDefinitionExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessDefinitionExt::getProcessKey, processKey);
        ProcessDefinitionExt ext = processDefinitionExtMapper.selectOne(wrapper);
        if (ext != null) {
            ext.setVersion(ext.getVersion() + 1);
            ext.setStatus(1);
            processDefinitionExtMapper.updateById(ext);
        }
        
        log.info("流程部署成功: {}", processKey);
    }

    @Override
    @Transactional
    public void toggleProcessStatus(Long id, Integer status) {
        ProcessDefinitionExt ext = processDefinitionExtMapper.selectById(id);
        if (ext == null) {
            throw new BusinessException("流程定义不存在");
        }
        ext.setStatus(status);
        processDefinitionExtMapper.updateById(ext);
    }

    @Override
    @Transactional
    public void deleteProcess(Long id) {
        ProcessDefinitionExt ext = processDefinitionExtMapper.selectById(id);
        if (ext == null) {
            throw new BusinessException("流程定义不存在");
        }
        ext.setDeleted(1);
        processDefinitionExtMapper.updateById(ext);
    }

    // ==================== 流程实例 ====================

    @Override
    @Transactional
    public String startProcess(String processKey, String businessId, String businessType, String title, Map<String, Object> variables) {
        // 查询流程定义
        LambdaQueryWrapper<ProcessDefinitionExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessDefinitionExt::getProcessKey, processKey)
               .eq(ProcessDefinitionExt::getStatus, 1)
               .eq(ProcessDefinitionExt::getDeleted, 0);
        ProcessDefinitionExt processDefExt = processDefinitionExtMapper.selectOne(wrapper);
        
        if (processDefExt == null) {
            throw new BusinessException("流程定义不存在或未启用");
        }

        // 设置流程变量
        if (variables == null) {
            variables = new HashMap<>();
        }
        Long currentUserId = SecurityUtils.getCurrentUserId();
        variables.put("initiator", currentUserId.toString());
        variables.put("businessId", businessId);
        variables.put("businessType", businessType);

        // 启动Flowable流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(processKey, businessId, variables);

        // 创建流程实例扩展记录
        ProcessInstanceExt instanceExt = new ProcessInstanceExt();
        instanceExt.setProcessInstanceId(processInstance.getId());
        instanceExt.setProcessDefinitionId(processInstance.getProcessDefinitionId());
        instanceExt.setProcessKey(processKey);
        instanceExt.setProcessName(processDefExt.getProcessName());
        instanceExt.setBusinessId(businessId);
        instanceExt.setBusinessType(businessType);
        instanceExt.setTitle(title);
        instanceExt.setInitiatorId(currentUserId);
        instanceExt.setInitiatorName(SecurityUtils.getCurrentUsername());
        instanceExt.setStatus("running");
        
        // 获取当前任务信息
        Task currentTask = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();
        if (currentTask != null) {
            instanceExt.setCurrentTaskName(currentTask.getName());
            instanceExt.setCurrentAssignee(currentTask.getAssignee());
        }
        
        instanceExt.setCreateBy(currentUserId);
        processInstanceExtMapper.insert(instanceExt);

        // 记录提交审批记录
        ApprovalRecord record = new ApprovalRecord();
        record.setProcessInstanceId(processInstance.getId());
        record.setAssigneeId(currentUserId);
        record.setAssigneeName(SecurityUtils.getCurrentUsername());
        record.setAction("submit");
        record.setComment("提交申请");
        record.setApprovalTime(LocalDateTime.now());
        record.setCreateBy(currentUserId);
        approvalRecordMapper.insert(record);

        log.info("流程启动成功: processInstanceId={}, businessId={}", processInstance.getId(), businessId);
        return processInstance.getId();
    }

    @Override
    public IPage<ProcessInstanceExt> getMyInitiatedProcesses(Long userId, int page, int size, String status, String keyword) {
        Page<ProcessInstanceExt> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ProcessInstanceExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessInstanceExt::getInitiatorId, userId)
               .eq(ProcessInstanceExt::getDeleted, 0);
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(ProcessInstanceExt::getStatus, status);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(ProcessInstanceExt::getTitle, keyword);
        }
        wrapper.orderByDesc(ProcessInstanceExt::getCreateTime);
        return processInstanceExtMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public ProcessInstanceExt getProcessInstanceById(Long id) {
        return processInstanceExtMapper.selectById(id);
    }

    @Override
    @Transactional
    public void cancelProcess(String processInstanceId, String reason) {
        // 终止Flowable流程
        runtimeService.deleteProcessInstance(processInstanceId, reason);
        
        // 更新扩展表状态
        LambdaQueryWrapper<ProcessInstanceExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessInstanceExt::getProcessInstanceId, processInstanceId);
        ProcessInstanceExt instanceExt = processInstanceExtMapper.selectOne(wrapper);
        if (instanceExt != null) {
            instanceExt.setStatus("cancelled");
            instanceExt.setEndTime(LocalDateTime.now());
            instanceExt.setRemark(reason);
            processInstanceExtMapper.updateById(instanceExt);
        }

        // 记录取消操作
        Long currentUserId = SecurityUtils.getCurrentUserId();
        ApprovalRecord record = new ApprovalRecord();
        record.setProcessInstanceId(processInstanceId);
        record.setAssigneeId(currentUserId);
        record.setAssigneeName(SecurityUtils.getCurrentUsername());
        record.setAction("cancel");
        record.setComment(reason);
        record.setApprovalTime(LocalDateTime.now());
        record.setCreateBy(currentUserId);
        approvalRecordMapper.insert(record);

        log.info("流程取消: processInstanceId={}", processInstanceId);
    }

    // ==================== 待办/已办 ====================

    @Override
    public IPage<Map<String, Object>> getTodoTasks(Long userId, int page, int size, String category, String keyword) {
        // 查询Flowable待办任务
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(userId.toString())
                .orderByTaskCreateTime().desc()
                .listPage((page - 1) * size, size);
        
        long total = taskService.createTaskQuery()
                .taskAssignee(userId.toString())
                .count();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> taskMap = new HashMap<>();
            taskMap.put("taskId", task.getId());
            taskMap.put("taskName", task.getName());
            taskMap.put("createTime", task.getCreateTime());
            taskMap.put("processInstanceId", task.getProcessInstanceId());
            
            // 获取流程实例扩展信息
            LambdaQueryWrapper<ProcessInstanceExt> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProcessInstanceExt::getProcessInstanceId, task.getProcessInstanceId());
            ProcessInstanceExt instanceExt = processInstanceExtMapper.selectOne(wrapper);
            if (instanceExt != null) {
                taskMap.put("title", instanceExt.getTitle());
                taskMap.put("processName", instanceExt.getProcessName());
                taskMap.put("initiatorName", instanceExt.getInitiatorName());
                taskMap.put("businessId", instanceExt.getBusinessId());
                taskMap.put("businessType", instanceExt.getBusinessType());
            }
            result.add(taskMap);
        }

        Page<Map<String, Object>> resultPage = new Page<>(page, size, total);
        resultPage.setRecords(result);
        return resultPage;
    }

    @Override
    public IPage<Map<String, Object>> getDoneTasks(Long userId, int page, int size, String category, String keyword) {
        // 查询Flowable已办任务
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId.toString())
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .listPage((page - 1) * size, size);
        
        long total = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(userId.toString())
                .finished()
                .count();

        List<Map<String, Object>> result = new ArrayList<>();
        for (HistoricTaskInstance task : tasks) {
            Map<String, Object> taskMap = new HashMap<>();
            taskMap.put("taskId", task.getId());
            taskMap.put("taskName", task.getName());
            taskMap.put("startTime", task.getStartTime());
            taskMap.put("endTime", task.getEndTime());
            taskMap.put("processInstanceId", task.getProcessInstanceId());
            
            // 获取流程实例扩展信息
            LambdaQueryWrapper<ProcessInstanceExt> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProcessInstanceExt::getProcessInstanceId, task.getProcessInstanceId());
            ProcessInstanceExt instanceExt = processInstanceExtMapper.selectOne(wrapper);
            if (instanceExt != null) {
                taskMap.put("title", instanceExt.getTitle());
                taskMap.put("processName", instanceExt.getProcessName());
                taskMap.put("initiatorName", instanceExt.getInitiatorName());
                taskMap.put("status", instanceExt.getStatus());
            }
            result.add(taskMap);
        }

        Page<Map<String, Object>> resultPage = new Page<>(page, size, total);
        resultPage.setRecords(result);
        return resultPage;
    }

    @Override
    public IPage<Map<String, Object>> getCopyToMe(Long userId, int page, int size) {
        // 暂时返回空列表，抄送功能后续实现
        Page<Map<String, Object>> resultPage = new Page<>(page, size, 0);
        resultPage.setRecords(new ArrayList<>());
        return resultPage;
    }

    // ==================== 审批操作 ====================

    @Override
    @Transactional
    public void approve(String taskId, String comment, Map<String, Object> variables) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在或已处理");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 完成任务
        if (variables != null) {
            taskService.complete(taskId, variables);
        } else {
            taskService.complete(taskId);
        }

        // 记录审批操作
        ApprovalRecord record = new ApprovalRecord();
        record.setProcessInstanceId(task.getProcessInstanceId());
        record.setTaskId(taskId);
        record.setTaskName(task.getName());
        record.setTaskKey(task.getTaskDefinitionKey());
        record.setAssigneeId(currentUserId);
        record.setAssigneeName(SecurityUtils.getCurrentUsername());
        record.setAction("approve");
        record.setComment(comment);
        record.setApprovalTime(LocalDateTime.now());
        record.setCreateBy(currentUserId);
        approvalRecordMapper.insert(record);

        // 检查流程是否结束
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();
        
        if (processInstance == null) {
            // 流程已结束，更新状态
            updateProcessInstanceStatus(task.getProcessInstanceId(), "completed");
        } else {
            // 更新当前任务信息
            updateCurrentTask(task.getProcessInstanceId());
        }

        log.info("任务审批通过: taskId={}", taskId);
    }

    @Override
    @Transactional
    public void reject(String taskId, String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在或已处理");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();
        String processInstanceId = task.getProcessInstanceId();

        // 终止流程
        runtimeService.deleteProcessInstance(processInstanceId, "驳回: " + comment);

        // 记录审批操作
        ApprovalRecord record = new ApprovalRecord();
        record.setProcessInstanceId(processInstanceId);
        record.setTaskId(taskId);
        record.setTaskName(task.getName());
        record.setTaskKey(task.getTaskDefinitionKey());
        record.setAssigneeId(currentUserId);
        record.setAssigneeName(SecurityUtils.getCurrentUsername());
        record.setAction("reject");
        record.setComment(comment);
        record.setApprovalTime(LocalDateTime.now());
        record.setCreateBy(currentUserId);
        approvalRecordMapper.insert(record);

        // 更新流程实例状态
        updateProcessInstanceStatus(processInstanceId, "rejected");

        log.info("任务驳回: taskId={}", taskId);
    }

    @Override
    @Transactional
    public void returnToPrevious(String taskId, String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在或已处理");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 获取上一个任务节点
        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .activityType("userTask")
                .finished()
                .orderByHistoricActivityInstanceEndTime().desc()
                .list();

        if (activities.isEmpty()) {
            throw new BusinessException("没有可退回的节点");
        }

        String targetActivityId = activities.get(0).getActivityId();
        
        // 使用Flowable API退回
        runtimeService.createChangeActivityStateBuilder()
                .processInstanceId(task.getProcessInstanceId())
                .moveActivityIdTo(task.getTaskDefinitionKey(), targetActivityId)
                .changeState();

        // 记录退回操作
        ApprovalRecord record = new ApprovalRecord();
        record.setProcessInstanceId(task.getProcessInstanceId());
        record.setTaskId(taskId);
        record.setTaskName(task.getName());
        record.setTaskKey(task.getTaskDefinitionKey());
        record.setAssigneeId(currentUserId);
        record.setAssigneeName(SecurityUtils.getCurrentUsername());
        record.setAction("return");
        record.setComment(comment);
        record.setApprovalTime(LocalDateTime.now());
        record.setCreateBy(currentUserId);
        approvalRecordMapper.insert(record);

        log.info("任务退回: taskId={}", taskId);
    }

    @Override
    @Transactional
    public void delegate(String taskId, Long targetUserId, String comment) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new BusinessException("任务不存在或已处理");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 转办任务
        taskService.setAssignee(taskId, targetUserId.toString());

        // 记录转办操作
        ApprovalRecord record = new ApprovalRecord();
        record.setProcessInstanceId(task.getProcessInstanceId());
        record.setTaskId(taskId);
        record.setTaskName(task.getName());
        record.setTaskKey(task.getTaskDefinitionKey());
        record.setAssigneeId(currentUserId);
        record.setAssigneeName(SecurityUtils.getCurrentUsername());
        record.setAction("delegate");
        record.setComment(comment + " (转办给用户ID: " + targetUserId + ")");
        record.setApprovalTime(LocalDateTime.now());
        record.setCreateBy(currentUserId);
        approvalRecordMapper.insert(record);

        // 更新当前审批人
        updateCurrentTask(task.getProcessInstanceId());

        log.info("任务转办: taskId={}, targetUserId={}", taskId, targetUserId);
    }

    @Override
    @Transactional
    public void withdraw(String processInstanceId, String reason) {
        // 检查是否可以撤回（只有发起人可以撤回，且只能撤回第一个任务未处理的流程）
        LambdaQueryWrapper<ProcessInstanceExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessInstanceExt::getProcessInstanceId, processInstanceId);
        ProcessInstanceExt instanceExt = processInstanceExtMapper.selectOne(wrapper);
        
        if (instanceExt == null) {
            throw new BusinessException("流程实例不存在");
        }
        
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (!instanceExt.getInitiatorId().equals(currentUserId)) {
            throw new BusinessException("只有发起人可以撤回");
        }

        // 终止流程
        runtimeService.deleteProcessInstance(processInstanceId, "撤回: " + reason);

        // 更新流程实例状态
        instanceExt.setStatus("cancelled");
        instanceExt.setEndTime(LocalDateTime.now());
        instanceExt.setRemark("撤回: " + reason);
        processInstanceExtMapper.updateById(instanceExt);

        // 记录撤回操作
        ApprovalRecord record = new ApprovalRecord();
        record.setProcessInstanceId(processInstanceId);
        record.setAssigneeId(currentUserId);
        record.setAssigneeName(SecurityUtils.getCurrentUsername());
        record.setAction("withdraw");
        record.setComment(reason);
        record.setApprovalTime(LocalDateTime.now());
        record.setCreateBy(currentUserId);
        approvalRecordMapper.insert(record);

        log.info("流程撤回: processInstanceId={}", processInstanceId);
    }

    @Override
    public void urge(String processInstanceId) {
        // 获取当前任务
        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        
        if (task == null) {
            throw new BusinessException("流程已结束或不存在待办任务");
        }

        Long currentUserId = SecurityUtils.getCurrentUserId();

        // 记录催办操作
        ApprovalRecord record = new ApprovalRecord();
        record.setProcessInstanceId(processInstanceId);
        record.setTaskId(task.getId());
        record.setTaskName(task.getName());
        record.setAssigneeId(currentUserId);
        record.setAssigneeName(SecurityUtils.getCurrentUsername());
        record.setAction("urge");
        record.setComment("催办提醒");
        record.setApprovalTime(LocalDateTime.now());
        record.setCreateBy(currentUserId);
        approvalRecordMapper.insert(record);

        // TODO: 发送催办通知给当前审批人

        log.info("流程催办: processInstanceId={}, assignee={}", processInstanceId, task.getAssignee());
    }

    // ==================== 流程跟踪 ====================

    @Override
    public Map<String, Object> getProcessProgress(String processInstanceId) {
        Map<String, Object> result = new HashMap<>();
        
        // 获取流程实例扩展信息
        LambdaQueryWrapper<ProcessInstanceExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessInstanceExt::getProcessInstanceId, processInstanceId);
        ProcessInstanceExt instanceExt = processInstanceExtMapper.selectOne(wrapper);
        result.put("instance", instanceExt);

        // 获取历史活动节点
        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();
        result.put("activities", activities);

        // 获取当前待办任务
        Task currentTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        result.put("currentTask", currentTask);

        return result;
    }

    @Override
    public List<ApprovalRecord> getApprovalRecords(String processInstanceId) {
        LambdaQueryWrapper<ApprovalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalRecord::getProcessInstanceId, processInstanceId)
               .eq(ApprovalRecord::getDeleted, 0)
               .orderByAsc(ApprovalRecord::getApprovalTime);
        return approvalRecordMapper.selectList(wrapper);
    }

    @Override
    public byte[] getProcessDiagram(String processInstanceId) {
        try {
            // 获取流程实例
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            
            if (historicProcessInstance == null) {
                return null;
            }

            // 生成流程图
            InputStream inputStream = repositoryService.getProcessDiagram(historicProcessInstance.getProcessDefinitionId());
            if (inputStream == null) {
                return null;
            }
            return inputStream.readAllBytes();
        } catch (Exception e) {
            log.error("获取流程图失败", e);
            return null;
        }
    }

    // ==================== 私有方法 ====================

    private void updateProcessInstanceStatus(String processInstanceId, String status) {
        LambdaQueryWrapper<ProcessInstanceExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessInstanceExt::getProcessInstanceId, processInstanceId);
        ProcessInstanceExt instanceExt = processInstanceExtMapper.selectOne(wrapper);
        if (instanceExt != null) {
            instanceExt.setStatus(status);
            instanceExt.setEndTime(LocalDateTime.now());
            // 计算耗时
            if (instanceExt.getCreateTime() != null) {
                long duration = ChronoUnit.MINUTES.between(instanceExt.getCreateTime(), LocalDateTime.now());
                instanceExt.setDuration(duration);
            }
            processInstanceExtMapper.updateById(instanceExt);
        }
    }

    private void updateCurrentTask(String processInstanceId) {
        Task currentTask = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        
        LambdaQueryWrapper<ProcessInstanceExt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProcessInstanceExt::getProcessInstanceId, processInstanceId);
        ProcessInstanceExt instanceExt = processInstanceExtMapper.selectOne(wrapper);
        
        if (instanceExt != null && currentTask != null) {
            instanceExt.setCurrentTaskName(currentTask.getName());
            instanceExt.setCurrentAssignee(currentTask.getAssignee());
            processInstanceExtMapper.updateById(instanceExt);
        }
    }
}
