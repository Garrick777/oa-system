package com.oasystem.modules.system.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新用户请求
 */
@Data
@Schema(description = "更新用户请求")
public class UserUpdateRequest {

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

    @Schema(description = "角色ID")
    private Long roleId;

    @Schema(description = "状态(0禁用1启用)")
    private Integer status;
}
