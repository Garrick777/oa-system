package com.oasystem.modules.workflow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oasystem.modules.workflow.entity.LeaveApplication;

import java.util.Map;

/**
 * 请假申请服务接口
 */
public interface LeaveService extends IService<LeaveApplication> {

    /**
     * 获取请假申请列表
     */
    IPage<LeaveApplication> getLeaveApplications(int page, int size, String status, String leaveType, String keyword);

    /**
     * 获取请假申请详情
     */
    LeaveApplication getLeaveApplicationById(Long id);

    /**
     * 创建请假申请（草稿）
     */
    LeaveApplication createLeaveApplication(LeaveApplication application);

    /**
     * 更新请假申请
     */
    void updateLeaveApplication(LeaveApplication application);

    /**
     * 提交请假申请（发起流程）
     */
    void submitLeaveApplication(Long id);

    /**
     * 取消请假申请
     */
    void cancelLeaveApplication(Long id, String reason);

    /**
     * 获取假期余额
     */
    Map<String, Object> getLeaveBalance(Long userId, Integer year);

    /**
     * 计算请假时长
     */
    Double calculateLeaveDuration(String startTime, String endTime);

    /**
     * 生成申请单号
     */
    String generateApplyNo();
}
