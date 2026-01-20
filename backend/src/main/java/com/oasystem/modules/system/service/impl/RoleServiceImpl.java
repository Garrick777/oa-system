package com.oasystem.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oasystem.common.exception.BusinessException;
import com.oasystem.modules.system.entity.Role;
import com.oasystem.modules.system.entity.RoleMenu;
import com.oasystem.modules.system.mapper.RoleMapper;
import com.oasystem.modules.system.mapper.RoleMenuMapper;
import com.oasystem.modules.system.mapper.UserRoleMapper;
import com.oasystem.modules.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 角色服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final RoleMenuMapper roleMenuMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public IPage<Role> pageRoles(int current, int size, String roleName, String roleCode, Integer status) {
        Page<Role> page = new Page<>(current, size);
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(roleName)) {
            wrapper.like(Role::getRoleName, roleName);
        }
        if (StringUtils.hasText(roleCode)) {
            wrapper.like(Role::getRoleCode, roleCode);
        }
        if (status != null) {
            wrapper.eq(Role::getStatus, status);
        }
        
        wrapper.orderByAsc(Role::getSort);
        return page(page, wrapper);
    }

    @Override
    public List<Role> getAllEnabled() {
        return list(new LambdaQueryWrapper<Role>()
                .eq(Role::getStatus, 1)
                .orderByAsc(Role::getSort));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(Role role) {
        // 检查角色编码是否存在
        Role existRole = getOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, role.getRoleCode()));
        if (existRole != null) {
            throw new BusinessException("角色编码已存在");
        }

        role.setStatus(role.getStatus() != null ? role.getStatus() : 1);
        save(role);
        
        log.info("创建角色成功: {}", role.getRoleName());
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Role role) {
        Role existRole = getById(role.getId());
        if (existRole == null) {
            throw new BusinessException("角色不存在");
        }

        // 检查角色编码是否被其他角色使用
        Role codeRole = getOne(new LambdaQueryWrapper<Role>()
                .eq(Role::getRoleCode, role.getRoleCode())
                .ne(Role::getId, role.getId()));
        if (codeRole != null) {
            throw new BusinessException("角色编码已存在");
        }

        // 不能修改超级管理员角色编码
        if ("super_admin".equals(existRole.getRoleCode()) && 
            !existRole.getRoleCode().equals(role.getRoleCode())) {
            throw new BusinessException("不能修改超级管理员角色编码");
        }

        updateById(role);
        log.info("更新角色成功: {}", role.getRoleName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 不能删除超级管理员角色
        if ("super_admin".equals(role.getRoleCode())) {
            throw new BusinessException("不能删除超级管理员角色");
        }

        // 检查是否有用户使用该角色
        Long userCount = userRoleMapper.selectCount(new LambdaQueryWrapper<com.oasystem.modules.system.entity.UserRole>()
                .eq(com.oasystem.modules.system.entity.UserRole::getRoleId, id));
        if (userCount > 0) {
            throw new BusinessException("该角色下存在用户，无法删除");
        }

        // 删除角色菜单关联
        roleMenuMapper.deleteByRoleId(id);
        
        // 删除角色
        removeById(id);
        
        log.info("删除角色成功: {}", role.getRoleName());
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        if ("super_admin".equals(role.getRoleCode()) && status == 0) {
            throw new BusinessException("不能禁用超级管理员角色");
        }

        Role updateRole = new Role();
        updateRole.setId(id);
        updateRole.setStatus(status);
        updateById(updateRole);
        
        log.info("更新角色状态: {} -> {}", role.getRoleName(), status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        Role role = getById(roleId);
        if (role == null) {
            throw new BusinessException("角色不存在");
        }

        // 删除原有菜单关联
        roleMenuMapper.deleteByRoleId(roleId);

        // 添加新的菜单关联
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                RoleMenu roleMenu = new RoleMenu();
                roleMenu.setRoleId(roleId);
                roleMenu.setMenuId(menuId);
                roleMenuMapper.insert(roleMenu);
            }
        }

        log.info("分配角色菜单权限: {} -> {} 个菜单", role.getRoleName(), menuIds != null ? menuIds.size() : 0);
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        return roleMenuMapper.selectMenuIdsByRoleId(roleId);
    }
}
