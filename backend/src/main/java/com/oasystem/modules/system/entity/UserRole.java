package com.oasystem.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户角色关联实体
 */
@Data
@TableName("sys_user_role")
@Schema(description = "用户角色关联")
public class UserRole implements Serializable {

    @Schema(description = "ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "角色ID")
    private Long roleId;
}
