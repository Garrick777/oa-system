package com.oasystem.modules.workflow.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 报销明细表
 */
@Data
@TableName("biz_expense_detail")
public class ExpenseDetail {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 报销申请ID
     */
    private Long expenseId;

    /**
     * 费用类型（travel-差旅费, meal-餐饮费, transport-交通费, communication-通讯费, office-办公费, other-其他）
     */
    private String feeType;

    /**
     * 费用发生日期
     */
    private LocalDate feeDate;

    /**
     * 费用金额
     */
    private BigDecimal amount;

    /**
     * 费用说明
     */
    private String description;

    /**
     * 发票号
     */
    private String invoiceNo;

    /**
     * 发票附件
     */
    private String invoiceAttachment;
}
