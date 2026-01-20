package com.oasystem.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oasystem.modules.system.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单Mapper
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 根据角色ID查询菜单
     */
    List<Menu> selectMenusByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查询菜单（通过角色关联）
     */
    List<Menu> selectMenusByUserId(@Param("userId") Long userId);
}
