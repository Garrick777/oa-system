package com.oasystem.modules.system.controller;

import com.oasystem.common.Result;
import com.oasystem.modules.system.controller.dto.ChangePasswordRequest;
import com.oasystem.modules.system.controller.dto.LoginRequest;
import com.oasystem.modules.system.controller.dto.LoginResponse;
import com.oasystem.modules.system.entity.User;
import com.oasystem.modules.system.service.UserService;
import com.oasystem.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        LoginResponse response = userService.login(request, ip);
        return Result.success(response);
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        String token = getTokenFromRequest(request);
        userService.logout(token);
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<User> getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userService.getById(userId);
        user.setPassword(null);
        return Result.success(user);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        return Result.success();
    }

    @Operation(summary = "首次修改密码")
    @PutMapping("/first-password")
    public Result<Void> firstChangePassword(@RequestBody java.util.Map<String, String> request) {
        Long userId = SecurityUtils.getCurrentUserId();
        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            throw new com.oasystem.common.exception.BusinessException("密码长度不能少于6位");
        }
        userService.firstChangePassword(userId, newPassword);
        return Result.success();
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
