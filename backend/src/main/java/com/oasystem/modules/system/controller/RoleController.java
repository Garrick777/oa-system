package com.oasystem.modules.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oasystem.common.PageResult;
import com.oasystem.common.Result;
import com.oasystem.modules.system.entity.Role;
import com.oasystem.modules.system.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理控制器
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/system/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "分页查询角色列表")
    @GetMapping
    @PreAuthorize("hasRole('super_admin')")
    public PageResult<Role> list(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "角色名称") @RequestParam(required = false) String roleName,
            @Parameter(description = "角色编码") @RequestParam(required = false) String roleCode,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) {
        IPage<Role> page = roleService.pageRoles(current, size, roleName, roleCode, status);
        return PageResult.of(page);
    }

    @Operation(summary = "获取所有启用的角色")
    @GetMapping("/all")
    public Result<List<Role>> getAllEnabled() {
        List<Role> roles = roleService.getAllEnabled();
        return Result.success(roles);
    }

    @Operation(summary = "获取角色详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<Role> getById(@PathVariable Long id) {
        Role role = roleService.getById(id);
        return Result.success(role);
    }

    @Operation(summary = "创建角色")
    @PostMapping
    @PreAuthorize("hasRole('super_admin')")
    public Result<Long> create(@Valid @RequestBody Role role) {
        Long roleId = roleService.createRole(role);
        return Result.success(roleId);
    }

    @Operation(summary = "更新角色")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Role role) {
        role.setId(id);
        roleService.updateRole(role);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    @Operation(summary = "修改角色状态")
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('super_admin')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        roleService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "获取角色的菜单权限")
    @GetMapping("/{id}/menus")
    @PreAuthorize("hasRole('super_admin')")
    public Result<List<Long>> getRoleMenus(@PathVariable Long id) {
        List<Long> menuIds = roleService.getRoleMenuIds(id);
        return Result.success(menuIds);
    }

    @Operation(summary = "分配菜单权限")
    @PutMapping("/{id}/menus")
    @PreAuthorize("hasRole('super_admin')")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return Result.success();
    }
}
