package com.oasystem.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oasystem.modules.system.controller.dto.LoginRequest;
import com.oasystem.modules.system.controller.dto.LoginResponse;
import com.oasystem.modules.system.controller.dto.UserCreateRequest;
import com.oasystem.modules.system.controller.dto.UserUpdateRequest;
import com.oasystem.modules.system.entity.User;
import com.oasystem.security.LoginUser;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 登录
     */
    LoginResponse login(LoginRequest request, String ip);

    /**
     * 登出
     */
    void logout(String token);

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 根据ID查询用户（含角色信息）
     */
    User getUserById(Long id);

    /**
     * 加载用户详情（用于认证）
     */
    LoginUser loadUserByUsername(String username);

    /**
     * 分页查询用户
     */
    IPage<User> pageUsers(int current, int size, String username, String realName,
                          String phone, Integer status, Long deptId);

    /**
     * 创建用户
     */
    Long createUser(UserCreateRequest request);

    /**
     * 更新用户
     */
    void updateUser(Long id, UserUpdateRequest request);

    /**
     * 删除用户
     */
    void deleteUser(Long id);

    /**
     * 修改用户状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 重置密码
     */
    void resetPassword(Long id, String newPassword);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 首次修改密码（不需要旧密码）
     */
    void firstChangePassword(Long userId, String newPassword);
}
