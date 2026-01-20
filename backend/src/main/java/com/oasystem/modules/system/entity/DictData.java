package com.oasystem.modules.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典数据实体
 */
@Data
@TableName("sys_dict_data")
@Schema(description = "字典数据")
public class DictData implements Serializable {

    @Schema(description = "ID")
    @TableId(type = IdType.AUTO)
    private Long id;

    @Schema(description = "字典类型")
    private String dictType;

    @Schema(description = "字典标签")
    private String dictLabel;

    @Schema(description = "字典值")
    private String dictValue;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：0-禁用 1-启用")
    private Integer status;

    @Schema(description = "样式属性")
    private String cssClass;

    @Schema(description = "标签类型")
    private String listClass;

    @Schema(description = "是否默认：0-否 1-是")
    private Integer isDefault;

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
