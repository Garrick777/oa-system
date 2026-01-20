package com.oasystem.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.oasystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 流程定义扩展表
 * 存储Flowable流程定义的扩展信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_process_definition_ext")
public class ProcessDefinitionExt extends BaseEntity {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Flowable流程定义ID
     */
    private String processDefinitionId;

    /**
     * 流程定义Key
     */
    private String processKey;

    /**
     * 流程名称
     */
    private String processName;

    /**
     * 流程分类（leave-请假, expense-报销, overtime-加班, business_trip-出差, go_out-外出, card_replace-补卡, general-通用）
     */
    private String category;

    /**
     * 流程图标
     */
    private String icon;

    /**
     * 关联表单ID
     */
    private Long formId;

    /**
     * 流程描述
     */
    private String description;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 状态（0-停用，1-启用）
     */
    private Integer status;

    /**
     * 排序
     */
    private Integer sortOrder;
}
