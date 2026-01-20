package com.oasystem.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oasystem.common.exception.BusinessException;
import com.oasystem.modules.system.controller.dto.LoginRequest;
import com.oasystem.modules.system.controller.dto.LoginResponse;
import com.oasystem.modules.system.controller.dto.UserCreateRequest;
import com.oasystem.modules.system.controller.dto.UserUpdateRequest;
import com.oasystem.modules.system.entity.User;
import com.oasystem.modules.system.entity.UserRole;
import com.oasystem.modules.system.mapper.UserMapper;
import com.oasystem.modules.system.mapper.UserRoleMapper;
import com.oasystem.modules.system.service.UserService;
import com.oasystem.security.JwtUtils;
import com.oasystem.security.LoginUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_CACHE_PREFIX = "oa:token:";
    private static final long TOKEN_EXPIRE_HOURS = 24;

    @Override
    public LoginResponse login(LoginRequest request, String ip) {
        // 查询用户
        User user = baseMapper.selectByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 检查状态
        if (user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用");
        }

        // 生成Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRoleCode());

        // 缓存登录信息
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setPassword(user.getPassword());
        loginUser.setRealName(user.getRealName());
        loginUser.setRoleCode(user.getRoleCode());
        loginUser.setDeptId(user.getDeptId());
        loginUser.setStatus(user.getStatus());

        String cacheKey = TOKEN_CACHE_PREFIX + token;
        redisTemplate.opsForValue().set(cacheKey, loginUser, TOKEN_EXPIRE_HOURS, TimeUnit.HOURS);

        // 更新登录信息
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setLastLoginTime(LocalDateTime.now());
        updateUser.setLastLoginIp(ip);
        baseMapper.updateById(updateUser);

        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setAvatar(user.getAvatar());
        response.setRoleCode(user.getRoleCode());
        response.setRoleName(user.getRoleName());
        response.setDeptId(user.getDeptId());
        response.setDeptName(user.getDeptName());
        // 判断是否需要修改密码（passwordChanged为0或null表示未修改过）
        response.setNeedChangePassword(user.getPasswordChanged() == null || user.getPasswordChanged() == 0);

        log.info("用户登录成功: {}", user.getUsername());
        return response;
    }

    @Override
    public void logout(String token) {
        if (token != null) {
            String cacheKey = TOKEN_CACHE_PREFIX + token;
            redisTemplate.delete(cacheKey);
            log.info("用户登出成功");
        }
    }

    @Override
    public User getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    public LoginUser loadUserByUsername(String username) {
        User user = baseMapper.selectByUsername(username);
        if (user == null) {
            return null;
        }
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setPassword(user.getPassword());
        loginUser.setRealName(user.getRealName());
        loginUser.setRoleCode(user.getRoleCode());
        loginUser.setDeptId(user.getDeptId());
        loginUser.setStatus(user.getStatus());
        return loginUser;
    }

    @Override
    public IPage<User> pageUsers(int current, int size, String username, String realName,
                                  String phone, Integer status, Long deptId) {
        Page<User> page = new Page<>(current, size);
        return baseMapper.selectUserPage(page, username, realName, phone, status, deptId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(UserCreateRequest request) {
        // 检查用户名是否存在
        User existUser = getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername()));
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setInitialPassword(request.getPassword()); // 存储初始密码明文
        user.setPasswordChanged(0); // 标记为未修改密码
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        user.setDeptId(request.getDeptId());
        user.setPositionId(request.getPositionId());
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        baseMapper.insert(user);

        // 关联角色
        if (request.getRoleId() != null) {
            UserRole userRole = new UserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(request.getRoleId());
            userRoleMapper.insert(userRole);
        }

        log.info("创建用户成功: {}", user.getUsername());
        return user.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserUpdateRequest request) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 更新用户信息
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender());
        user.setDeptId(request.getDeptId());
        user.setPositionId(request.getPositionId());
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        baseMapper.updateById(user);

        // 更新角色
        if (request.getRoleId() != null) {
            userRoleMapper.deleteByUserId(id);
            UserRole userRole = new UserRole();
            userRole.setUserId(id);
            userRole.setRoleId(request.getRoleId());
            userRoleMapper.insert(userRole);
        }

        log.info("更新用户成功: {}", user.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 不能删除超级管理员
        if ("admin".equals(user.getUsername())) {
            throw new BusinessException("不能删除超级管理员");
        }

        // 删除用户角色关联
        userRoleMapper.deleteByUserId(id);

        // 逻辑删除用户
        removeById(id);

        log.info("删除用户成功: {}", user.getUsername());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if ("admin".equals(user.getUsername()) && status == 0) {
            throw new BusinessException("不能禁用超级管理员");
        }

        User updateUser = new User();
        updateUser.setId(id);
        updateUser.setStatus(status);
        baseMapper.updateById(updateUser);

        log.info("更新用户状态: {} -> {}", user.getUsername(), status);
    }

    @Override
    public void resetPassword(Long id, String newPassword) {
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        User updateUser = new User();
        updateUser.setId(id);
        updateUser.setPassword(passwordEncoder.encode(newPassword));
        updateUser.setInitialPassword(newPassword); // 重置时保存新的初始密码
        updateUser.setPasswordChanged(0); // 标记为需要修改密码
        baseMapper.updateById(updateUser);

        log.info("重置用户密码: {}", user.getUsername());
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(newPassword));
        updateUser.setInitialPassword(null); // 清空初始密码
        updateUser.setPasswordChanged(1); // 标记为已修改密码
        baseMapper.updateById(updateUser);

        log.info("修改密码成功: {}", user.getUsername());
    }

    @Override
    public void firstChangePassword(Long userId, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 首次修改密码不需要验证旧密码
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setPassword(passwordEncoder.encode(newPassword));
        updateUser.setInitialPassword(null); // 清空初始密码
        updateUser.setPasswordChanged(1); // 标记为已修改密码
        baseMapper.updateById(updateUser);

        log.info("首次修改密码成功: {}", user.getUsername());
    }
}
