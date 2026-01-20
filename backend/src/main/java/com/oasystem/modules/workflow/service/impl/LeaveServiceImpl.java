package com.oasystem.modules.workflow.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oasystem.common.exception.BusinessException;
import com.oasystem.modules.workflow.entity.LeaveApplication;
import com.oasystem.modules.workflow.mapper.LeaveApplicationMapper;
import com.oasystem.modules.workflow.service.LeaveService;
import com.oasystem.modules.workflow.service.ProcessService;
import com.oasystem.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 请假申请服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveServiceImpl extends ServiceImpl<LeaveApplicationMapper, LeaveApplication> implements LeaveService {

    private final ProcessService processService;
    
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    @Override
    public IPage<LeaveApplication> getLeaveApplications(int page, int size, String status, String leaveType, String keyword) {
        Page<LeaveApplication> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<LeaveApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(LeaveApplication::getDeleted, 0);
        
        // 只查询当前用户的申请
        Long currentUserId = SecurityUtils.getCurrentUserId();
        wrapper.eq(LeaveApplication::getApplicantId, currentUserId);
        
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(LeaveApplication::getStatus, status);
        }
        if (StrUtil.isNotBlank(leaveType)) {
            wrapper.eq(LeaveApplication::getLeaveType, leaveType);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(LeaveApplication::getReason, keyword);
        }
        wrapper.orderByDesc(LeaveApplication::getCreateTime);
        return this.page(pageParam, wrapper);
    }

    @Override
    public LeaveApplication getLeaveApplicationById(Long id) {
        return this.getById(id);
    }

    @Override
    @Transactional
    public LeaveApplication createLeaveApplication(LeaveApplication application) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        
        // 生成申请单号
        application.setApplyNo(generateApplyNo());
        application.setApplicantId(currentUserId);
        application.setApplicantName(SecurityUtils.getCurrentUsername());
        application.setStatus("draft");
        application.setCreateBy(currentUserId);
        
        this.save(application);
        
        log.info("创建请假申请: applyNo={}", application.getApplyNo());
        return application;
    }

    @Override
    @Transactional
    public void updateLeaveApplication(LeaveApplication application) {
        LeaveApplication existing = this.getById(application.getId());
        if (existing == null) {
            throw new BusinessException("请假申请不存在");
        }
        if (!"draft".equals(existing.getStatus())) {
            throw new BusinessException("只有草稿状态的申请可以修改");
        }
        
        application.setUpdateBy(SecurityUtils.getCurrentUserId());
        this.updateById(application);
        
        log.info("更新请假申请: id={}", application.getId());
    }

    @Override
    @Transactional
    public void submitLeaveApplication(Long id) {
        LeaveApplication application = this.getById(id);
        if (application == null) {
            throw new BusinessException("请假申请不存在");
        }
        if (!"draft".equals(application.getStatus())) {
            throw new BusinessException("只有草稿状态的申请可以提交");
        }
        
        // 准备流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("leaveType", application.getLeaveType());
        variables.put("duration", application.getDuration());
        variables.put("applicantId", application.getApplicantId());
        
        // 发起流程
        String title = application.getApplicantName() + "的" + getLeaveTypeName(application.getLeaveType()) + "申请";
        String processInstanceId = processService.startProcess(
                "leave",
                application.getId().toString(),
                "leave",
                title,
                variables
        );
        
        // 更新申请状态
        application.setProcessInstanceId(processInstanceId);
        application.setStatus("pending");
        application.setUpdateBy(SecurityUtils.getCurrentUserId());
        this.updateById(application);
        
        log.info("提交请假申请: id={}, processInstanceId={}", id, processInstanceId);
    }

    @Override
    @Transactional
    public void cancelLeaveApplication(Long id, String reason) {
        LeaveApplication application = this.getById(id);
        if (application == null) {
            throw new BusinessException("请假申请不存在");
        }
        if ("approved".equals(application.getStatus()) || "cancelled".equals(application.getStatus())) {
            throw new BusinessException("已通过或已取消的申请不能取消");
        }
        
        // 如果有流程实例，取消流程
        if (StrUtil.isNotBlank(application.getProcessInstanceId())) {
            processService.cancelProcess(application.getProcessInstanceId(), reason);
        }
        
        // 更新申请状态
        application.setStatus("cancelled");
        application.setUpdateBy(SecurityUtils.getCurrentUserId());
        this.updateById(application);
        
        log.info("取消请假申请: id={}", id);
    }

    @Override
    public Map<String, Object> getLeaveBalance(Long userId, Integer year) {
        // TODO: 从假期余额表查询
        Map<String, Object> balance = new HashMap<>();
        balance.put("annual", Map.of("total", 10.0, "used", 3.0, "remaining", 7.0));
        balance.put("sick", Map.of("total", 15.0, "used", 2.0, "remaining", 13.0));
        balance.put("personal", Map.of("total", 5.0, "used", 1.0, "remaining", 4.0));
        return balance;
    }

    @Override
    public Double calculateLeaveDuration(String startTime, String endTime) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime start = LocalDateTime.parse(startTime, formatter);
            LocalDateTime end = LocalDateTime.parse(endTime, formatter);
            
            // 计算小时数
            long hours = ChronoUnit.HOURS.between(start, end);
            return (double) hours;
        } catch (Exception e) {
            log.error("计算请假时长失败", e);
            return 0.0;
        }
    }

    @Override
    public String generateApplyNo() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = SEQUENCE.incrementAndGet();
        if (seq > 9999) {
            SEQUENCE.set(0);
            seq = SEQUENCE.incrementAndGet();
        }
        return "QJ" + dateStr + String.format("%04d", seq);
    }
    
    private String getLeaveTypeName(String leaveType) {
        return switch (leaveType) {
            case "annual" -> "年假";
            case "sick" -> "病假";
            case "personal" -> "事假";
            case "marriage" -> "婚假";
            case "maternity" -> "产假";
            case "paternity" -> "陪产假";
            case "bereavement" -> "丧假";
            default -> "其他";
        };
    }
}
