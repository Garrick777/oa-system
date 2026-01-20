package com.oasystem.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oasystem.modules.system.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色Mapper
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户ID查询角色列表
     */
    List<Role> selectRolesByUserId(@Param("userId") Long userId);

    /**
     * 查询所有启用的角色
     */
    List<Role> selectAllEnabled();
}
