package com.oasystem.modules.org.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位实体
 */
@Data
@TableName("org_position")
@Schema(description = "岗位")
public class Position implements Serializable {

    @Schema(description = "ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "岗位名称")
    private String positionName;

    @Schema(description = "岗位编码")
    private String positionCode;

    @Schema(description = "岗位级别")
    @TableField("position_level")
    private Integer level;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Schema(description = "是否删除：0-否 1-是")
    @TableLogic
    private Integer deleted;
}
