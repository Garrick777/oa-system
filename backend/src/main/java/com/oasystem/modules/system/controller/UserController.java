package com.oasystem.modules.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oasystem.common.PageResult;
import com.oasystem.common.Result;
import com.oasystem.modules.system.controller.dto.UserCreateRequest;
import com.oasystem.modules.system.controller.dto.UserUpdateRequest;
import com.oasystem.modules.system.entity.User;
import com.oasystem.modules.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/system/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping
    @PreAuthorize("hasAnyRole('super_admin', 'hr_admin')")
    public PageResult<User> list(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "真实姓名") @RequestParam(required = false) String realName,
            @Parameter(description = "手机号") @RequestParam(required = false) String phone,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "部门ID") @RequestParam(required = false) Long deptId) {
        IPage<User> page = userService.pageUsers(current, size, username, realName, phone, status, deptId);
        return PageResult.of(page);
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('super_admin', 'hr_admin')")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }

    @Operation(summary = "创建用户")
    @PostMapping
    @PreAuthorize("hasAnyRole('super_admin', 'hr_admin')")
    public Result<Long> create(@Valid @RequestBody UserCreateRequest request) {
        Long userId = userService.createUser(request);
        return Result.success(userId);
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('super_admin', 'hr_admin')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(id, request);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @Operation(summary = "修改用户状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('super_admin', 'hr_admin')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('super_admin', 'hr_admin')")
    public Result<Void> resetPassword(@PathVariable Long id,
                                       @RequestParam(defaultValue = "123456") String newPassword) {
        userService.resetPassword(id, newPassword);
        return Result.success();
    }
}
