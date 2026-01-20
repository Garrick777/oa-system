package com.oasystem.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oasystem.modules.org.entity.Department;
import com.oasystem.modules.org.mapper.DepartmentMapper;
import com.oasystem.modules.system.entity.User;
import com.oasystem.modules.system.mapper.UserMapper;
import com.oasystem.modules.system.service.DashboardService;
import com.oasystem.modules.workflow.entity.LeaveApplication;
import com.oasystem.modules.workflow.entity.ExpenseApplication;
import com.oasystem.modules.workflow.mapper.LeaveApplicationMapper;
import com.oasystem.modules.workflow.mapper.ExpenseApplicationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 工作台服务实现
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserMapper userMapper;
    private final DepartmentMapper departmentMapper;
    private final LeaveApplicationMapper leaveApplicationMapper;
    private final ExpenseApplicationMapper expenseApplicationMapper;

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 员工总数
        Long userCount = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getStatus, 1)
                .eq(User::getDeleted, 0));
        stats.put("employeeCount", userCount);
        
        // 部门数量
        Long deptCount = departmentMapper.selectCount(new LambdaQueryWrapper<Department>()
                .eq(Department::getStatus, 1)
                .eq(Department::getDeleted, 0));
        stats.put("departmentCount", deptCount);
        
        // 本月请假申请数
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        Long leaveCount = leaveApplicationMapper.selectCount(new LambdaQueryWrapper<LeaveApplication>()
                .ge(LeaveApplication::getCreateTime, monthStart));
        stats.put("monthlyLeaveCount", leaveCount);
        
        // 待审批数（running状态的申请）
        Long pendingLeave = leaveApplicationMapper.selectCount(new LambdaQueryWrapper<LeaveApplication>()
                .eq(LeaveApplication::getStatus, "running"));
        Long pendingExpense = expenseApplicationMapper.selectCount(new LambdaQueryWrapper<ExpenseApplication>()
                .eq(ExpenseApplication::getStatus, "running"));
        stats.put("pendingApprovalCount", pendingLeave + pendingExpense);
        
        return stats;
    }

    @Override
    public List<Map<String, Object>> getMyTodos(Long userId) {
        List<Map<String, Object>> todos = new ArrayList<>();
        
        // 获取用户的待处理请假申请
        List<LeaveApplication> myLeaves = leaveApplicationMapper.selectList(
                new LambdaQueryWrapper<LeaveApplication>()
                        .eq(LeaveApplication::getApplicantId, userId)
                        .eq(LeaveApplication::getStatus, "draft")
                        .orderByDesc(LeaveApplication::getCreateTime)
                        .last("LIMIT 5"));
        
        for (LeaveApplication leave : myLeaves) {
            Map<String, Object> todo = new HashMap<>();
            todo.put("id", leave.getId());
            todo.put("title", "请假申请草稿待提交");
            todo.put("type", "leave");
            todo.put("priority", "normal");
            todo.put("deadline", formatTime(leave.getCreateTime()));
            todos.add(todo);
        }
        
        // 获取用户的待处理报销申请
        List<ExpenseApplication> myExpenses = expenseApplicationMapper.selectList(
                new LambdaQueryWrapper<ExpenseApplication>()
                        .eq(ExpenseApplication::getApplicantId, userId)
                        .eq(ExpenseApplication::getStatus, "draft")
                        .orderByDesc(ExpenseApplication::getCreateTime)
                        .last("LIMIT 5"));
        
        for (ExpenseApplication expense : myExpenses) {
            Map<String, Object> todo = new HashMap<>();
            todo.put("id", expense.getId());
            todo.put("title", "报销申请草稿待提交");
            todo.put("type", "expense");
            todo.put("priority", "normal");
            todo.put("deadline", formatTime(expense.getCreateTime()));
            todos.add(todo);
        }
        
        return todos;
    }

    @Override
    public List<Map<String, Object>> getRecentActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();
        
        // 获取最近的请假申请
        List<LeaveApplication> recentLeaves = leaveApplicationMapper.selectList(
                new LambdaQueryWrapper<LeaveApplication>()
                        .orderByDesc(LeaveApplication::getCreateTime)
                        .last("LIMIT 5"));
        
        for (LeaveApplication leave : recentLeaves) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", leave.getId());
            activity.put("content", leave.getApplicantName() + " 提交了请假申请");
            activity.put("time", getRelativeTime(leave.getCreateTime()));
            activity.put("type", "leave");
            activities.add(activity);
        }
        
        // 获取最近的报销申请
        List<ExpenseApplication> recentExpenses = expenseApplicationMapper.selectList(
                new LambdaQueryWrapper<ExpenseApplication>()
                        .orderByDesc(ExpenseApplication::getCreateTime)
                        .last("LIMIT 5"));
        
        for (ExpenseApplication expense : recentExpenses) {
            Map<String, Object> activity = new HashMap<>();
            activity.put("id", expense.getId());
            activity.put("content", expense.getApplicantName() + " 提交了报销申请");
            activity.put("time", getRelativeTime(expense.getCreateTime()));
            activity.put("type", "expense");
            activities.add(activity);
        }
        
        // 按时间排序，取前10条
        activities.sort((a, b) -> {
            String timeA = (String) a.get("time");
            String timeB = (String) b.get("time");
            return timeA.compareTo(timeB);
        });
        
        return activities.size() > 10 ? activities.subList(0, 10) : activities;
    }

    @Override
    public Integer getPendingApprovalCount(Long userId, String roleCode) {
        // 简单实现：统计所有running状态的申请
        // 实际应该根据用户角色和审批权限来筛选
        Long pendingLeave = leaveApplicationMapper.selectCount(new LambdaQueryWrapper<LeaveApplication>()
                .eq(LeaveApplication::getStatus, "running"));
        Long pendingExpense = expenseApplicationMapper.selectCount(new LambdaQueryWrapper<ExpenseApplication>()
                .eq(ExpenseApplication::getStatus, "running"));
        return (int) (pendingLeave + pendingExpense);
    }
    
    private String formatTime(LocalDateTime time) {
        if (time == null) return "-";
        return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
    
    private String getRelativeTime(LocalDateTime time) {
        if (time == null) return "-";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(time, now).toMinutes();
        
        if (minutes < 1) return "刚刚";
        if (minutes < 60) return minutes + "分钟前";
        
        long hours = minutes / 60;
        if (hours < 24) return hours + "小时前";
        
        long days = hours / 24;
        if (days < 7) return days + "天前";
        
        return time.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"));
    }
}
