package com.oasystem.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.oasystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 请假申请表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_leave_application")
public class LeaveApplication extends BaseEntity {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 申请单号
     */
    private String applyNo;

    /**
     * 流程实例ID
     */
    private String processInstanceId;

    /**
     * 申请人ID
     */
    private Long applicantId;

    /**
     * 申请人姓名
     */
    private String applicantName;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 请假类型（annual-年假, sick-病假, personal-事假, marriage-婚假, maternity-产假, paternity-陪产假, bereavement-丧假, other-其他）
     */
    private String leaveType;

    /**
     * 请假开始时间
     */
    private LocalDateTime startTime;

    /**
     * 请假结束时间
     */
    private LocalDateTime endTime;

    /**
     * 请假时长（小时）
     */
    private BigDecimal duration;

    /**
     * 请假事由
     */
    private String reason;

    /**
     * 附件JSON
     */
    private String attachments;

    /**
     * 工作交接人ID
     */
    private Long handoverUserId;

    /**
     * 工作交接人姓名
     */
    private String handoverUserName;

    /**
     * 工作交接说明
     */
    private String handoverRemark;

    /**
     * 状态（draft-草稿, pending-待审批, approved-已通过, rejected-已驳回, cancelled-已取消）
     */
    private String status;
}
