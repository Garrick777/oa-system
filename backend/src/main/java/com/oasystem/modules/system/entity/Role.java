package com.oasystem.modules.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色实体
 */
@Data
@TableName("sys_role")
@Schema(description = "角色")
public class Role implements Serializable {

    @Schema(description = "角色ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态(0禁用1启用)")
    private Integer status;

    @Schema(description = "数据范围(1全部2本部门及以下3本部门4仅本人)")
    private Integer dataScope;

    @Schema(description = "备注")
    private String remark;

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
}
