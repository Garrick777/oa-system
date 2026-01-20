package com.oasystem.modules.workflow.controller.dto;

import com.oasystem.modules.workflow.entity.ExpenseApplication;
import com.oasystem.modules.workflow.entity.ExpenseDetail;
import lombok.Data;

import java.util.List;

/**
 * 报销申请DTO
 */
@Data
public class ExpenseApplicationDTO {
    
    /**
     * 报销申请信息
     */
    private ExpenseApplication application;
    
    /**
     * 报销明细列表
     */
    private List<ExpenseDetail> details;
}
