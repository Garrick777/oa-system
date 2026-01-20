package com.oasystem.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oasystem.modules.system.entity.UserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户角色关联Mapper
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 删除用户的所有角色
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 删除角色的所有用户关联
     */
    int deleteByRoleId(@Param("roleId") Long roleId);
}
