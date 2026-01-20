package com.oasystem.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oasystem.modules.system.entity.Role;
import com.oasystem.modules.system.entity.User;
import com.oasystem.modules.system.entity.UserRole;
import com.oasystem.modules.system.mapper.RoleMapper;
import com.oasystem.modules.system.mapper.UserMapper;
import com.oasystem.modules.system.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 数据初始化器 - 初始化测试用户和角色
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    // 测试用户配置：username -> {realName, roleCode, password}
    // 注意：所有测试账号密码统一为 123456
    private static final Map<String, String[]> TEST_USERS = new HashMap<>() {{
        put("admin", new String[]{"超级管理员", "super_admin", "123456"});
        put("leader", new String[]{"张总", "company_leader", "123456"});
        put("hr", new String[]{"李人事", "hr_admin", "123456"});
        put("officer", new String[]{"王行政", "admin_officer", "123456"});
        put("finance", new String[]{"赵财务", "finance_admin", "123456"});
        put("manager", new String[]{"刘经理", "dept_manager", "123456"});
        put("employee", new String[]{"陈员工", "employee", "123456"});
    }};

    @EventListener(ApplicationReadyEvent.class)
    public void initData() {
        initRoles();
        initTestUsers();
        log.info("数据初始化完成");
    }

    /**
     * 初始化角色
     */
    private void initRoles() {
        String[][] roles = {
            {"super_admin", "超级管理员", "1"},
            {"company_leader", "公司高管", "2"},
            {"hr_admin", "HR管理员", "3"},
            {"admin_officer", "行政管理员", "4"},
            {"finance_admin", "财务管理员", "5"},
            {"dept_manager", "部门经理", "6"},
            {"employee", "普通员工", "7"}
        };

        for (String[] roleData : roles) {
            String roleCode = roleData[0];
            String roleName = roleData[1];
            Integer sort = Integer.parseInt(roleData[2]);

            Role existing = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                    .eq(Role::getRoleCode, roleCode));
            
            if (existing == null) {
                Role role = new Role();
                role.setRoleCode(roleCode);
                role.setRoleName(roleName);
                role.setSort(sort);
                role.setStatus(1);
                roleMapper.insert(role);
                log.info("创建角色: {}", roleName);
            }
        }
    }

    /**
     * 初始化测试用户
     */
    private void initTestUsers() {
        String encodedDefaultPassword = passwordEncoder.encode("123456");

        for (Map.Entry<String, String[]> entry : TEST_USERS.entrySet()) {
            String username = entry.getKey();
            String[] userData = entry.getValue();
            String realName = userData[0];
            String roleCode = userData[1];
            String password = userData[2];

            User existing = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, username));

            if (existing == null) {
                // 创建新用户
                User user = new User();
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(password));
                // 超级管理员不需要初始密码提示和强制改密
                if ("super_admin".equals(roleCode)) {
                    user.setInitialPassword(null);
                    user.setPasswordChanged(1); // 标记为已修改密码
                } else {
                    user.setInitialPassword(password); // 存储初始密码明文
                    user.setPasswordChanged(0); // 标记为未修改密码
                }
                user.setRealName(realName);
                user.setStatus(1);
                userMapper.insert(user);

                // 关联角色
                Role role = roleMapper.selectOne(new LambdaQueryWrapper<Role>()
                        .eq(Role::getRoleCode, roleCode));
                if (role != null) {
                    UserRole userRole = new UserRole();
                    userRole.setUserId(user.getId());
                    userRole.setRoleId(role.getId());
                    userRoleMapper.insert(userRole);
                }
                log.info("创建测试用户: {} ({})", realName, username);
            } else {
                // 确保密码正确，并设置初始密码字段
                if (!passwordEncoder.matches(password, existing.getPassword())) {
                    existing.setPassword(passwordEncoder.encode(password));
                }
                // 确保初始密码字段有值
                if (existing.getInitialPassword() == null) {
                    existing.setInitialPassword(password);
                    existing.setPasswordChanged(0);
                }
                userMapper.updateById(existing);
            }
        }
    }
}
