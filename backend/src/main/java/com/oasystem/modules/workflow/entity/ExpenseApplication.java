package com.oasystem.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.oasystem.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 报销申请表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("biz_expense_application")
public class ExpenseApplication extends BaseEntity {

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
     * 报销类型（travel-差旅费, meal-餐饮费, transport-交通费, communication-通讯费, office-办公费, other-其他）
     */
    private String expenseType;

    /**
     * 报销总金额
     */
    private BigDecimal totalAmount;

    /**
     * 报销明细JSON
     */
    private String expenseDetails;

    /**
     * 报销事由
     */
    private String reason;

    /**
     * 附件JSON（发票等）
     */
    private String attachments;

    /**
     * 收款人姓名
     */
    private String payeeName;

    /**
     * 收款银行
     */
    private String payeeBank;

    /**
     * 收款账号
     */
    private String payeeAccount;

    /**
     * 状态（draft-草稿, pending-待审批, approved-已通过, rejected-已驳回, cancelled-已取消, paid-已付款）
     */
    private String status;
}
