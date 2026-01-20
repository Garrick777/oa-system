package com.oasystem.modules.system.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建用户请求
 */
@Data
@Schema(description = "创建用户请求")
public class UserCreateRequest {

    @Schema(description = "用户名", required = true)
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码", required = true)
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "真实姓名", required = true)
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

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

    @Schema(description = "角色ID", required = true)
    @NotNull(message = "角色不能为空")
    private Long roleId;

    @Schema(description = "状态(0禁用1启用)")
    private Integer status = 1;
}
