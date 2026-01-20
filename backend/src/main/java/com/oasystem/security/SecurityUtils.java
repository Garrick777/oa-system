package com.oasystem.security;

import com.oasystem.common.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Security 工具类
 */
public class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * 获取当前登录用户
     */
    public static LoginUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(401, "用户未登录");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser) {
            return (LoginUser) principal;
        }
        throw new BusinessException(401, "用户未登录");
    }

    /**
     * 获取当前登录用户（可能为null）
     */
    public static LoginUser getCurrentUserOrNull() {
        try {
            return getCurrentUser();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    /**
     * 获取当前用户ID（可能为null）
     */
    public static Long getCurrentUserIdOrNull() {
        LoginUser user = getCurrentUserOrNull();
        return user != null ? user.getUserId() : null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        return getCurrentUser().getUsername();
    }

    /**
     * 获取当前用户角色编码
     */
    public static String getCurrentRoleCode() {
        return getCurrentUser().getRoleCode();
    }

    /**
     * 获取当前用户部门ID
     */
    public static Long getCurrentDeptId() {
        return getCurrentUser().getDeptId();
    }

    /**
     * 判断是否是超级管理员
     */
    public static boolean isSuperAdmin() {
        return "super_admin".equals(getCurrentRoleCode());
    }

    /**
     * 判断是否有指定角色
     */
    public static boolean hasRole(String roleCode) {
        return roleCode.equals(getCurrentRoleCode());
    }
}
