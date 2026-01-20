package com.oasystem.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.oasystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 审批记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_approval_record")
public class ApprovalRecord extends BaseEntity {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务节点Key
     */
    private String taskKey;

    /**
     * 审批人ID
     */
    private Long assigneeId;

    /**
     * 审批人姓名
     */
    private String assigneeName;

    /**
     * 审批操作（submit-提交, approve-通过, reject-驳回, return-退回, delegate-转办, withdraw-撤回, urge-催办）
     */
    private String action;

    /**
     * 审批意见
     */
    private String comment;

    /**
     * 附件JSON
     */
    private String attachments;

    /**
     * 审批时间
     */
    private LocalDateTime approvalTime;

    /**
     * 任务耗时（分钟）
     */
    private Long duration;
}
