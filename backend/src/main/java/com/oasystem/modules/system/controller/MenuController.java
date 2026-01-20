package com.oasystem.modules.system.controller;

import com.oasystem.common.Result;
import com.oasystem.modules.system.entity.Menu;
import com.oasystem.modules.system.service.MenuService;
import com.oasystem.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜单管理控制器
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/system/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "获取菜单树（用于分配权限）")
    @GetMapping("/tree")
    @PreAuthorize("hasRole('super_admin')")
    public Result<List<Menu>> getMenuTree() {
        List<Menu> tree = menuService.getMenuTree();
        return Result.success(tree);
    }

    @Operation(summary = "获取所有菜单列表")
    @GetMapping
    @PreAuthorize("hasRole('super_admin')")
    public Result<List<Menu>> getAllMenus() {
        List<Menu> menus = menuService.getAllMenus();
        return Result.success(menus);
    }

    @Operation(summary = "获取当前用户菜单树（用于渲染侧边栏）")
    @GetMapping("/user")
    public Result<List<Menu>> getUserMenuTree(@AuthenticationPrincipal LoginUser loginUser) {
        // 超级管理员返回全部菜单
        if ("super_admin".equals(loginUser.getRoleCode())) {
            return Result.success(menuService.getMenuTree());
        }
        List<Menu> tree = menuService.getMenuTreeByUserId(loginUser.getUserId());
        return Result.success(tree);
    }

    @Operation(summary = "获取菜单详情")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<Menu> getById(@PathVariable Long id) {
        Menu menu = menuService.getById(id);
        return Result.success(menu);
    }

    @Operation(summary = "创建菜单")
    @PostMapping
    @PreAuthorize("hasRole('super_admin')")
    public Result<Long> create(@Valid @RequestBody Menu menu) {
        Long menuId = menuService.createMenu(menu);
        return Result.success(menuId);
    }

    @Operation(summary = "更新菜单")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody Menu menu) {
        menu.setId(id);
        menuService.updateMenu(menu);
        return Result.success();
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('super_admin')")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.deleteMenu(id);
        return Result.success();
    }
}
