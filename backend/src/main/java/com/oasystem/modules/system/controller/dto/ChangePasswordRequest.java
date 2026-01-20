package com.oasystem.modules.system.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码请求
 */
@Data
@Schema(description = "修改密码请求")
public class ChangePasswordRequest {

    @Schema(description = "原密码", required = true)
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @Schema(description = "新密码", required = true)
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
