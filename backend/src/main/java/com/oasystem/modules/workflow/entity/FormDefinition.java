package com.oasystem.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.oasystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 表单定义表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_form_definition")
public class FormDefinition extends BaseEntity {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 表单Key（唯一标识）
     */
    private String formKey;

    /**
     * 表单名称
     */
    private String formName;

    /**
     * 表单类型（system-系统表单, custom-自定义表单）
     */
    private String formType;

    /**
     * 表单配置JSON（字段定义）
     */
    @TableField(typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private String formConfig;

    /**
     * 表单描述
     */
    private String description;

    /**
     * 状态（0-停用，1-启用）
     */
    private Integer status;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 排序
     */
    private Integer sortOrder;
}
