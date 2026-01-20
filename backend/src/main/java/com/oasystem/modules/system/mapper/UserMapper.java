package com.oasystem.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oasystem.modules.system.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户（含角色信息）
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 分页查询用户列表（含关联信息）
     */
    IPage<User> selectUserPage(Page<User> page,
                               @Param("username") String username,
                               @Param("realName") String realName,
                               @Param("phone") String phone,
                               @Param("status") Integer status,
                               @Param("deptId") Long deptId);

    /**
     * 根据ID查询用户详情（含关联信息）
     */
    User selectUserById(@Param("id") Long id);
}
