package com.oasystem.modules.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("sys_user")
@Schema(description = "用户")
public class User implements Serializable {

    @Schema(description = "用户ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "密码")
    @JsonIgnore
    private String password;

    @Schema(description = "初始密码(明文,供HR查看)")
    private String initialPassword;

    @Schema(description = "是否已修改密码(0否1是)")
    private Integer passwordChanged;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "性别(0未知1男2女)")
    private Integer gender;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "岗位ID")
    private Long positionId;

    @Schema(description = "状态(0禁用1启用)")
    private Integer status;

    @Schema(description = "最后登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Schema(description = "是否删除")
    @TableLogic
    private Integer deleted;

    // ========== 非数据库字段 ==========

    @Schema(description = "角色ID")
    @TableField(exist = false)
    private Long roleId;

    @Schema(description = "角色名称")
    @TableField(exist = false)
    private String roleName;

    @Schema(description = "角色编码")
    @TableField(exist = false)
    private String roleCode;

    @Schema(description = "部门名称")
    @TableField(exist = false)
    private String deptName;

    @Schema(description = "岗位名称")
    @TableField(exist = false)
    private String positionName;
}
