package com.oasystem.modules.workflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oasystem.modules.workflow.entity.ExpenseApplication;
import com.oasystem.modules.workflow.entity.ExpenseDetail;

import java.util.List;

/**
 * 报销申请服务接口
 */
public interface ExpenseService extends IService<ExpenseApplication> {

    /**
     * 获取报销申请列表
     */
    IPage<ExpenseApplication> getExpenseApplications(int page, int size, String status, String expenseType, String keyword);

    /**
     * 获取报销申请详情
     */
    ExpenseApplication getExpenseApplicationById(Long id);

    /**
     * 创建报销申请（草稿）
     */
    ExpenseApplication createExpenseApplication(ExpenseApplication application, List<ExpenseDetail> details);

    /**
     * 更新报销申请
     */
    void updateExpenseApplication(ExpenseApplication application, List<ExpenseDetail> details);

    /**
     * 提交报销申请（发起流程）
     */
    void submitExpenseApplication(Long id);

    /**
     * 取消报销申请
     */
    void cancelExpenseApplication(Long id, String reason);

    /**
     * 获取报销明细
     */
    List<ExpenseDetail> getExpenseDetails(Long expenseId);

    /**
     * 生成申请单号
     */
    String generateApplyNo();
}
