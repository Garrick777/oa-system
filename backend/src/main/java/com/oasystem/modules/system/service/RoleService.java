package com.oasystem.modules.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oasystem.modules.system.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService extends IService<Role> {

    /**
     * 分页查询角色
     */
    IPage<Role> pageRoles(int current, int size, String roleName, String roleCode, Integer status);

    /**
     * 获取所有启用的角色
     */
    List<Role> getAllEnabled();

    /**
     * 创建角色
     */
    Long createRole(Role role);

    /**
     * 更新角色
     */
    void updateRole(Role role);

    /**
     * 删除角色
     */
    void deleteRole(Long id);

    /**
     * 修改角色状态
     */
    void updateStatus(Long id, Integer status);

    /**
     * 分配菜单权限
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 获取角色的菜单ID列表
     */
    List<Long> getRoleMenuIds(Long roleId);
}
