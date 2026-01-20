package com.oasystem.modules.system.controller;

import com.oasystem.common.Result;
import com.oasystem.modules.system.service.DashboardService;
import com.oasystem.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 工作台控制器
 */
@Tag(name = "工作台")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "获取统计概览")
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(dashboardService.getStats());
    }

    @Operation(summary = "获取我的待办")
    @GetMapping("/my-todos")
    public Result<List<Map<String, Object>>> getMyTodos() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(dashboardService.getMyTodos(userId));
    }

    @Operation(summary = "获取最近活动")
    @GetMapping("/recent-activities")
    public Result<List<Map<String, Object>>> getRecentActivities() {
        return Result.success(dashboardService.getRecentActivities());
    }

    @Operation(summary = "获取待审批数量")
    @GetMapping("/pending-approval-count")
    public Result<Integer> getPendingApprovalCount() {
        Long userId = SecurityUtils.getCurrentUserId();
        String roleCode = SecurityUtils.getCurrentRoleCode();
        return Result.success(dashboardService.getPendingApprovalCount(userId, roleCode));
    }
}
