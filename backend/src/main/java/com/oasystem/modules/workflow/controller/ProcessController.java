package com.oasystem.modules.workflow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oasystem.common.Result;
import com.oasystem.modules.workflow.entity.ApprovalRecord;
import com.oasystem.modules.workflow.entity.ProcessDefinitionExt;
import com.oasystem.modules.workflow.entity.ProcessInstanceExt;
import com.oasystem.modules.workflow.service.ProcessService;
import com.oasystem.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 流程管理控制器
 */
@Tag(name = "流程管理")
@RestController
@RequestMapping("/workflow/process")
@RequiredArgsConstructor
public class ProcessController {

    private final ProcessService processService;

    // ==================== 流程定义 ====================

    @Operation(summary = "获取流程定义列表")
    @GetMapping("/definitions")
    public Result<IPage<ProcessDefinitionExt>> getProcessDefinitions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        return Result.success(processService.getProcessDefinitions(page, size, category, keyword));
    }

    @Operation(summary = "获取流程定义详情")
    @GetMapping("/definitions/{id}")
    public Result<ProcessDefinitionExt> getProcessDefinitionById(@PathVariable Long id) {
        return Result.success(processService.getProcessDefinitionById(id));
    }

    @Operation(summary = "获取可发起的流程列表")
    @GetMapping("/definitions/available")
    public Result<List<ProcessDefinitionExt>> getAvailableProcesses() {
        return Result.success(processService.getAvailableProcesses());
    }

    @Operation(summary = "部署流程定义")
    @PostMapping("/definitions/deploy")
    public Result<Void> deployProcess(@RequestBody Map<String, String> request) {
        processService.deployProcess(request.get("processKey"), request.get("bpmnXml"));
        return Result.success();
    }

    @Operation(summary = "启用/停用流程")
    @PutMapping("/definitions/{id}/status")
    public Result<Void> toggleProcessStatus(@PathVariable Long id, @RequestParam Integer status) {
        processService.toggleProcessStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "删除流程定义")
    @DeleteMapping("/definitions/{id}")
    public Result<Void> deleteProcess(@PathVariable Long id) {
        processService.deleteProcess(id);
        return Result.success();
    }

    // ==================== 流程实例 ====================

    @Operation(summary = "获取我发起的流程")
    @GetMapping("/instances/my")
    public Result<IPage<ProcessInstanceExt>> getMyInitiatedProcesses(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(processService.getMyInitiatedProcesses(userId, page, size, status, keyword));
    }

    @Operation(summary = "获取流程实例详情")
    @GetMapping("/instances/{id}")
    public Result<ProcessInstanceExt> getProcessInstanceById(@PathVariable Long id) {
        return Result.success(processService.getProcessInstanceById(id));
    }

    @Operation(summary = "取消流程")
    @PostMapping("/instances/{processInstanceId}/cancel")
    public Result<Void> cancelProcess(@PathVariable String processInstanceId, @RequestBody Map<String, String> request) {
        processService.cancelProcess(processInstanceId, request.get("reason"));
        return Result.success();
    }

    // ==================== 待办/已办 ====================

    @Operation(summary = "获取待办任务")
    @GetMapping("/tasks/todo")
    public Result<IPage<Map<String, Object>>> getTodoTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(processService.getTodoTasks(userId, page, size, category, keyword));
    }

    @Operation(summary = "获取已办任务")
    @GetMapping("/tasks/done")
    public Result<IPage<Map<String, Object>>> getDoneTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(processService.getDoneTasks(userId, page, size, category, keyword));
    }

    @Operation(summary = "获取抄送给我的")
    @GetMapping("/tasks/copy")
    public Result<IPage<Map<String, Object>>> getCopyToMe(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(processService.getCopyToMe(userId, page, size));
    }

    // ==================== 审批操作 ====================

    @Operation(summary = "审批通过")
    @PostMapping("/tasks/{taskId}/approve")
    public Result<Void> approve(@PathVariable String taskId, @RequestBody Map<String, Object> request) {
        String comment = (String) request.get("comment");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) request.get("variables");
        processService.approve(taskId, comment, variables);
        return Result.success();
    }

    @Operation(summary = "审批驳回")
    @PostMapping("/tasks/{taskId}/reject")
    public Result<Void> reject(@PathVariable String taskId, @RequestBody Map<String, String> request) {
        processService.reject(taskId, request.get("comment"));
        return Result.success();
    }

    @Operation(summary = "退回上一步")
    @PostMapping("/tasks/{taskId}/return")
    public Result<Void> returnToPrevious(@PathVariable String taskId, @RequestBody Map<String, String> request) {
        processService.returnToPrevious(taskId, request.get("comment"));
        return Result.success();
    }

    @Operation(summary = "转办")
    @PostMapping("/tasks/{taskId}/delegate")
    public Result<Void> delegate(@PathVariable String taskId, @RequestBody Map<String, Object> request) {
        Long targetUserId = Long.valueOf(request.get("targetUserId").toString());
        String comment = (String) request.get("comment");
        processService.delegate(taskId, targetUserId, comment);
        return Result.success();
    }

    @Operation(summary = "撤回")
    @PostMapping("/instances/{processInstanceId}/withdraw")
    public Result<Void> withdraw(@PathVariable String processInstanceId, @RequestBody Map<String, String> request) {
        processService.withdraw(processInstanceId, request.get("reason"));
        return Result.success();
    }

    @Operation(summary = "催办")
    @PostMapping("/instances/{processInstanceId}/urge")
    public Result<Void> urge(@PathVariable String processInstanceId) {
        processService.urge(processInstanceId);
        return Result.success();
    }

    // ==================== 流程跟踪 ====================

    @Operation(summary = "获取流程进度")
    @GetMapping("/instances/{processInstanceId}/progress")
    public Result<Map<String, Object>> getProcessProgress(@PathVariable String processInstanceId) {
        return Result.success(processService.getProcessProgress(processInstanceId));
    }

    @Operation(summary = "获取审批记录")
    @GetMapping("/instances/{processInstanceId}/records")
    public Result<List<ApprovalRecord>> getApprovalRecords(@PathVariable String processInstanceId) {
        return Result.success(processService.getApprovalRecords(processInstanceId));
    }

    @Operation(summary = "获取流程图")
    @GetMapping(value = "/instances/{processInstanceId}/diagram", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getProcessDiagram(@PathVariable String processInstanceId) {
        return processService.getProcessDiagram(processInstanceId);
    }
}
