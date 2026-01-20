package com.oasystem.modules.org.controller;

import com.oasystem.common.Result;
import com.oasystem.modules.org.entity.Department;
import com.oasystem.modules.org.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器
 */
@Tag(name = "部门管理")
@RestController
@RequestMapping("/org/depts")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "获取部门树")
    @GetMapping("/tree")
    public Result<List<Department>> getDeptTree() {
        List<Department> tree = departmentService.getDeptTree();
        return Result.success(tree);
    }

    @Operation(summary = "获取所有部门列表")
    @GetMapping
    public Result<List<Department>> getAllDepts() {
        List<Department> depts = departmentService.getAllDepts();
        return Result.success(depts);
    }

    @Operation(summary = "获取部门详情")
    @GetMapping("/{id}")
    public Result<Department> getById(@PathVariable Long id) {
        Department dept = departmentService.getById(id);
        return Result.success(dept);
    }

    @Operation(summary = "创建部门")
    @PostMapping
    @PreAuthorize("hasAnyRole('super_admin', 'hr_admin')")
    public Result<Long> create(@Valid @RequestBody Department dept) {
        Long deptId = departmentService.createDept(dept);
        return Result.success(deptId);
    }

    @Operation(summary = "更新部门")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('super_admin', 'hr_admin')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Department dept) {
        dept.setId(id);
        departmentService.updateDept(dept);
        return Result.success();
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('super_admin', 'hr_admin')")
    public Result<Void> delete(@PathVariable Long id) {
        departmentService.deleteDept(id);
        return Result.success();
    }
}
