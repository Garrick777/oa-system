package com.oasystem.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oasystem.modules.system.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色菜单关联Mapper
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {

    /**
     * 删除角色的所有菜单关联
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除菜单的所有角色关联
     */
    int deleteByMenuId(@Param("menuId") Long menuId);

    /**
     * 根据角色ID查询菜单ID列表
     */
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);
}
