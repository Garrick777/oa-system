package com.oasystem.modules.system.service;

import java.util.List;
import java.util.Map;

/**
 * 工作台服务接口
 */
public interface DashboardService {

    /**
     * 获取统计概览
     */
    Map<String, Object> getStats();

    /**
     * 获取我的待办
     */
    List<Map<String, Object>> getMyTodos(Long userId);

    /**
     * 获取最近活动
     */
    List<Map<String, Object>> getRecentActivities();

    /**
     * 获取待审批数量
     */
    Integer getPendingApprovalCount(Long userId, String roleCode);
}
