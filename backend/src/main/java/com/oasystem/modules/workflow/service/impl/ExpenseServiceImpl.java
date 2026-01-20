package com.oasystem.modules.workflow.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oasystem.common.exception.BusinessException;
import com.oasystem.modules.workflow.entity.ExpenseApplication;
import com.oasystem.modules.workflow.entity.ExpenseDetail;
import com.oasystem.modules.workflow.mapper.ExpenseApplicationMapper;
import com.oasystem.modules.workflow.mapper.ExpenseDetailMapper;
import com.oasystem.modules.workflow.service.ExpenseService;
import com.oasystem.modules.workflow.service.ProcessService;
import com.oasystem.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 报销申请服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl extends ServiceImpl<ExpenseApplicationMapper, ExpenseApplication> implements ExpenseService {

    private final ExpenseDetailMapper expenseDetailMapper;
    private final ProcessService processService;
    
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    @Override
    public IPage<ExpenseApplication> getExpenseApplications(int page, int size, String status, String expenseType, String keyword) {
        Page<ExpenseApplication> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ExpenseApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExpenseApplication::getDeleted, 0);
        
        // 只查询当前用户的申请
        Long currentUserId = SecurityUtils.getCurrentUserId();
        wrapper.eq(ExpenseApplication::getApplicantId, currentUserId);
        
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(ExpenseApplication::getStatus, status);
        }
        if (StrUtil.isNotBlank(expenseType)) {
            wrapper.eq(ExpenseApplication::getExpenseType, expenseType);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(ExpenseApplication::getReason, keyword);
        }
        wrapper.orderByDesc(ExpenseApplication::getCreateTime);
        return this.page(pageParam, wrapper);
    }

    @Override
    public ExpenseApplication getExpenseApplicationById(Long id) {
        return this.getById(id);
    }

    @Override
    @Transactional
    public ExpenseApplication createExpenseApplication(ExpenseApplication application, List<ExpenseDetail> details) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 生成申请单号
        application.setApplyNo(generateApplyNo());
        application.setApplicantId(currentUserId);
        application.setApplicantName(SecurityUtils.getCurrentUsername());
        application.setStatus("draft");
        application.setCreateBy(currentUserId);
        
        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ExpenseDetail detail : details) {
            totalAmount = totalAmount.add(detail.getAmount());
        }
        application.setTotalAmount(totalAmount);
        
        this.save(application);
        
        // 保存明细
        for (ExpenseDetail detail : details) {
            detail.setExpenseId(application.getId());
            expenseDetailMapper.insert(detail);
        }
        
        log.info("创建报销申请: applyNo={}, totalAmount={}", application.getApplyNo(), totalAmount);
        return application;
    }

    @Override
    @Transactional
    public void updateExpenseApplication(ExpenseApplication application, List<ExpenseDetail> details) {
        ExpenseApplication existing = this.getById(application.getId());
        if (existing == null) {
            throw new BusinessException("报销申请不存在");
        }
        if (!"draft".equals(existing.getStatus())) {
            throw new BusinessException("只有草稿状态的申请可以修改");
        }
        
        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (ExpenseDetail detail : details) {
            totalAmount = totalAmount.add(detail.getAmount());
        }
        application.setTotalAmount(totalAmount);
        application.setUpdateBy(SecurityUtils.getCurrentUserId());
        this.updateById(application);
        
        // 删除旧明细，插入新明细
        LambdaQueryWrapper<ExpenseDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExpenseDetail::getExpenseId, application.getId());
        expenseDetailMapper.delete(wrapper);
        
        for (ExpenseDetail detail : details) {
            detail.setExpenseId(application.getId());
            expenseDetailMapper.insert(detail);
        }
        
        log.info("更新报销申请: id={}", application.getId());
    }

    @Override
    @Transactional
    public void submitExpenseApplication(Long id) {
        ExpenseApplication application = this.getById(id);
        if (application == null) {
            throw new BusinessException("报销申请不存在");
        }
        if (!"draft".equals(application.getStatus())) {
            throw new BusinessException("只有草稿状态的申请可以提交");
        }
        
        // 准备流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("expenseType", application.getExpenseType());
        variables.put("totalAmount", application.getTotalAmount());
        variables.put("applicantId", application.getApplicantId());
        
        // 发起流程
        String title = application.getApplicantName() + "的" + getExpenseTypeName(application.getExpenseType()) + "报销申请";
        String processInstanceId = processService.startProcess(
                "expense",
                application.getId().toString(),
                "expense",
                title,
                variables
        );
        
        // 更新申请状态
        application.setProcessInstanceId(processInstanceId);
        application.setStatus("pending");
        application.setUpdateBy(SecurityUtils.getCurrentUserId());
        this.updateById(application);
        
        log.info("提交报销申请: id={}, processInstanceId={}", id, processInstanceId);
    }

    @Override
    @Transactional
    public void cancelExpenseApplication(Long id, String reason) {
        ExpenseApplication application = this.getById(id);
        if (application == null) {
            throw new BusinessException("报销申请不存在");
        }
        if ("approved".equals(application.getStatus()) || "cancelled".equals(application.getStatus()) || "paid".equals(application.getStatus())) {
            throw new BusinessException("已通过、已取消或已付款的申请不能取消");
        }
        
        // 如果有流程实例，取消流程
        if (StrUtil.isNotBlank(application.getProcessInstanceId())) {
            processService.cancelProcess(application.getProcessInstanceId(), reason);
        }
        
        // 更新申请状态
        application.setStatus("cancelled");
        application.setUpdateBy(SecurityUtils.getCurrentUserId());
        this.updateById(application);
        
        log.info("取消报销申请: id={}", id);
    }

    @Override
    public List<ExpenseDetail> getExpenseDetails(Long expenseId) {
        LambdaQueryWrapper<ExpenseDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExpenseDetail::getExpenseId, expenseId);
        return expenseDetailMapper.selectList(wrapper);
    }

    @Override
    public String generateApplyNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = SEQUENCE.incrementAndGet();
        if (seq > 9999) {
            SEQUENCE.set(0);
            seq = SEQUENCE.incrementAndGet();
        }
        return "BX" + dateStr + String.format("%04d", seq);
    }
    
    private String getExpenseTypeName(String expenseType) {
        return switch (expenseType) {
            case "travel" -> "差旅费";
            case "meal" -> "餐饮费";
            case "transport" -> "交通费";
            case "communication" -> "通讯费";
            case "office" -> "办公费";
            default -> "其他";
        };
    }
}
