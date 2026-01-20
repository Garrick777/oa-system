package com.oasystem.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.oasystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 流程实例扩展表
 * 存储Flowable流程实例的扩展信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_process_instance_ext")
public class ProcessInstanceExt extends BaseEntity {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Flowable流程实例ID
     */
    private String processInstanceId;

    /**
     * 流程定义ID
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
     * 业务ID（关联具体业务表）
     */
    private String businessId;

    /**
     * 业务类型
     */
    private String businessType;

    /**
     * 申请标题
     */
    private String title;

    /**
     * 发起人ID
     */
    private Long initiatorId;

    /**
     * 发起人姓名
     */
    private String initiatorName;

    /**
     * 发起人部门ID
     */
    private Long deptId;

    /**
     * 发起人部门名称
     */
    private String deptName;

    /**
     * 流程状态（draft-草稿, running-审批中, completed-已完成, rejected-已驳回, cancelled-已取消）
     */
    private String status;

    /**
     * 当前审批人
     */
    private String currentAssignee;

    /**
     * 当前任务节点名称
     */
    private String currentTaskName;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 流程耗时（分钟）
     */
    private Long duration;

    /**
     * 备注
     */
    private String remark;
}
