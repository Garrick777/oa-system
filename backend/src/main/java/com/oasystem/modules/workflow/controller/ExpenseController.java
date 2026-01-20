package com.oasystem.modules.workflow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oasystem.common.Result;
import com.oasystem.modules.workflow.controller.dto.ExpenseApplicationDTO;
import com.oasystem.modules.workflow.entity.ExpenseApplication;
import com.oasystem.modules.workflow.entity.ExpenseDetail;
import com.oasystem.modules.workflow.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报销申请控制器
 */
@Tag(name = "报销申请")
@RestController
@RequestMapping("/workflow/expense")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "获取报销申请列表")
    @GetMapping
    public Result<IPage<ExpenseApplication>> getExpenseApplications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String expenseType,
            @RequestParam(required = false) String keyword) {
        return Result.success(expenseService.getExpenseApplications(page, size, status, expenseType, keyword));
    }

    @Operation(summary = "获取报销申请详情")
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getExpenseApplicationById(@PathVariable Long id) {
        ExpenseApplication application = expenseService.getExpenseApplicationById(id);
        List<ExpenseDetail> details = expenseService.getExpenseDetails(id);
        Map<String, Object> result = new HashMap<>();
        result.put("application", application);
        result.put("details", details);
        return Result.success(result);
    }

    @Operation(summary = "创建报销申请")
    @PostMapping
    public Result<ExpenseApplication> createExpenseApplication(@RequestBody ExpenseApplicationDTO dto) {
        return Result.success(expenseService.createExpenseApplication(dto.getApplication(), dto.getDetails()));
    }

    @Operation(summary = "更新报销申请")
    @PutMapping("/{id}")
    public Result<Void> updateExpenseApplication(@PathVariable Long id, @RequestBody ExpenseApplicationDTO dto) {
        dto.getApplication().setId(id);
        expenseService.updateExpenseApplication(dto.getApplication(), dto.getDetails());
        return Result.success();
    }

    @Operation(summary = "提交报销申请")
    @PostMapping("/{id}/submit")
    public Result<Void> submitExpenseApplication(@PathVariable Long id) {
        expenseService.submitExpenseApplication(id);
        return Result.success();
    }

    @Operation(summary = "取消报销申请")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancelExpenseApplication(@PathVariable Long id, @RequestBody Map<String, String> request) {
        expenseService.cancelExpenseApplication(id, request.get("reason"));
        return Result.success();
    }
}
