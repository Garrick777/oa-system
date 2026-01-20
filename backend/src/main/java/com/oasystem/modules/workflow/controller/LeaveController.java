package com.oasystem.modules.workflow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oasystem.common.Result;
import com.oasystem.modules.workflow.entity.LeaveApplication;
import com.oasystem.modules.workflow.service.LeaveService;
import com.oasystem.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.Map;

/**
 * 请假申请控制器
 */
@Tag(name = "请假申请")
@RestController
@RequestMapping("/workflow/leave")
@RequiredArgsConstructor
public class LeaveController {

    private final LeaveService leaveService;

    @Operation(summary = "获取请假申请列表")
    @GetMapping
    public Result<IPage<LeaveApplication>> getLeaveApplications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String leaveType,
            @RequestParam(required = false) String keyword) {
        return Result.success(leaveService.getLeaveApplications(page, size, status, leaveType, keyword));
    }

    @Operation(summary = "获取请假申请详情")
    @GetMapping("/{id}")
    public Result<LeaveApplication> getLeaveApplicationById(@PathVariable Long id) {
        return Result.success(leaveService.getLeaveApplicationById(id));
    }

    @Operation(summary = "创建请假申请")
    @PostMapping
    public Result<LeaveApplication> createLeaveApplication(@RequestBody LeaveApplication application) {
        return Result.success(leaveService.createLeaveApplication(application));
    }

    @Operation(summary = "更新请假申请")
    @PutMapping("/{id}")
    public Result<Void> updateLeaveApplication(@PathVariable Long id, @RequestBody LeaveApplication application) {
        application.setId(id);
        leaveService.updateLeaveApplication(application);
        return Result.success();
    }

    @Operation(summary = "提交请假申请")
    @PostMapping("/{id}/submit")
    public Result<Void> submitLeaveApplication(@PathVariable Long id) {
        leaveService.submitLeaveApplication(id);
        return Result.success();
    }

    @Operation(summary = "取消请假申请")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelLeaveApplication(@PathVariable Long id, @RequestBody Map<String, String> request) {
        leaveService.cancelLeaveApplication(id, request.get("reason"));
        return Result.success();
    }

    @Operation(summary = "获取假期余额")
    @GetMapping("/balance")
    public Result<Map<String, Object>> getLeaveBalance(
            @RequestParam(required = false) Integer year) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (year == null) {
            year = Year.now().getValue();
        }
        return Result.success(leaveService.getLeaveBalance(userId, year));
    }

    @Operation(summary = "计算请假时长")
    @GetMapping("/duration")
    public Result<Double> calculateLeaveDuration(
            @RequestParam String startTime,
            @RequestParam String endTime) {
        return Result.success(leaveService.calculateLeaveDuration(startTime, endTime));
    }
}
