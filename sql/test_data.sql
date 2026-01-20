-- ========================================
-- OA办公系统 - 完整测试数据
-- ========================================

USE oa_system;

-- ========================================
-- 1. 部门数据
-- ========================================
DELETE FROM org_department WHERE id > 0;
INSERT INTO org_department (id, parent_id, ancestors, dept_name, dept_code, leader_id, sort, status) VALUES
(1, 0, '0', '星辰科技有限公司', 'ROOT', 1, 0, 1),
(2, 1, '0,1', '总经理办公室', 'CEO', 2, 1, 1),
(3, 1, '0,1', '技术研发中心', 'TECH', 3, 2, 1),
(4, 1, '0,1', '市场营销部', 'MARKET', 4, 3, 1),
(5, 1, '0,1', '人力资源部', 'HR', 5, 4, 1),
(6, 1, '0,1', '财务部', 'FINANCE', 6, 5, 1),
(7, 1, '0,1', '行政部', 'ADMIN', 7, 6, 1),
(8, 3, '0,1,3', '前端开发组', 'TECH-FE', 8, 1, 1),
(9, 3, '0,1,3', '后端开发组', 'TECH-BE', 9, 2, 1),
(10, 3, '0,1,3', '测试组', 'TECH-QA', 10, 3, 1),
(11, 4, '0,1,4', '市场推广组', 'MARKET-PROMO', 11, 1, 1),
(12, 4, '0,1,4', '客户服务组', 'MARKET-CS', 12, 2, 1);

-- ========================================
-- 2. 岗位数据
-- ========================================
DELETE FROM org_position WHERE id > 0;
INSERT INTO org_position (id, position_name, position_code, position_level, sort, status, remark) VALUES
(1, '总经理', 'CEO', 1, 1, 1, '公司最高管理者'),
(2, '副总经理', 'VP', 2, 2, 1, '协助总经理管理公司'),
(3, '技术总监', 'CTO', 2, 3, 1, '技术研发负责人'),
(4, '市场总监', 'CMO', 2, 4, 1, '市场营销负责人'),
(5, '人力资源总监', 'HRD', 2, 5, 1, '人力资源负责人'),
(6, '财务总监', 'CFO', 2, 6, 1, '财务管理负责人'),
(7, '行政总监', 'CAO', 2, 7, 1, '行政事务负责人'),
(8, '部门经理', 'MANAGER', 3, 8, 1, '部门管理者'),
(9, '高级工程师', 'SENIOR_ENG', 4, 9, 1, '高级技术人员'),
(10, '中级工程师', 'MID_ENG', 5, 10, 1, '中级技术人员'),
(11, '初级工程师', 'JUNIOR_ENG', 6, 11, 1, '初级技术人员'),
(12, '产品经理', 'PM', 4, 12, 1, '产品规划与管理'),
(13, '设计师', 'DESIGNER', 5, 13, 1, 'UI/UX设计'),
(14, '测试工程师', 'QA', 5, 14, 1, '软件测试'),
(15, '市场专员', 'MARKET_STAFF', 6, 15, 1, '市场推广'),
(16, '客服专员', 'CS_STAFF', 6, 16, 1, '客户服务'),
(17, 'HR专员', 'HR_STAFF', 6, 17, 1, '人力资源专员'),
(18, '财务专员', 'FIN_STAFF', 6, 18, 1, '财务处理'),
(19, '行政专员', 'ADMIN_STAFF', 6, 19, 1, '行政事务'),
(20, '实习生', 'INTERN', 7, 20, 1, '实习人员');

-- ========================================
-- 3. 角色数据
-- ========================================
-- 保留已有角色，添加缺失角色
INSERT IGNORE INTO sys_role (id, role_name, role_code, remark, status, sort) VALUES
(1, '超级管理员', 'super_admin', '系统超级管理员，拥有所有权限', 1, 1),
(2, '公司高管', 'company_leader', '公司领导层，查看全公司数据', 1, 2),
(3, 'HR管理员', 'hr_admin', '人力资源管理员，管理员工档案和考勤', 1, 3),
(4, '行政管理员', 'admin_officer', '行政事务管理员', 1, 4),
(5, '财务管理员', 'finance_admin', '财务管理员，处理报销和财务事宜', 1, 5),
(6, '部门经理', 'dept_manager', '部门负责人，管理本部门事务', 1, 6),
(7, '普通员工', 'employee', '普通员工，基础功能权限', 1, 7);

-- ========================================
-- 4. 用户数据（密码都是 123456）
-- ========================================
-- 正确的BCrypt密码哈希: $2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu (123456)
DELETE FROM sys_user WHERE id > 1;
UPDATE sys_user SET password = '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', initial_password = NULL, password_changed = 1 WHERE id = 1;

INSERT INTO sys_user (id, username, password, initial_password, password_changed, real_name, avatar, phone, email, gender, dept_id, position_id, status) VALUES
-- 高管
(2, 'zhangwei', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '张伟', NULL, '13800001001', 'zhangwei@example.com', 1, 2, 1, 1),
(3, 'lina', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '李娜', NULL, '13800001002', 'lina@example.com', 2, 2, 2, 1),

-- 部门经理
(4, 'wangqiang', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '王强', NULL, '13800002001', 'wangqiang@example.com', 1, 3, 3, 1),
(5, 'zhaojing', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '赵静', NULL, '13800002002', 'zhaojing@example.com', 2, 4, 4, 1),
(6, 'sunli', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '孙丽', NULL, '13800002003', 'sunli@example.com', 2, 5, 5, 1),
(7, 'zhoujun', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '周军', NULL, '13800002004', 'zhoujun@example.com', 1, 6, 6, 1),
(8, 'wuying', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '吴英', NULL, '13800002005', 'wuying@example.com', 2, 7, 7, 1),

-- 技术部员工
(9, 'chenfei', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '陈飞', NULL, '13800003001', 'chenfei@example.com', 1, 8, 8, 1),
(10, 'liuyang', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '刘洋', NULL, '13800003002', 'liuyang@example.com', 1, 8, 9, 1),
(11, 'huangxin', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '黄鑫', NULL, '13800003003', 'huangxin@example.com', 1, 8, 10, 1),
(12, 'linmei', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '林美', NULL, '13800003004', 'linmei@example.com', 2, 8, 11, 1),
(13, 'yangkai', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '杨凯', NULL, '13800003005', 'yangkai@example.com', 1, 9, 8, 1),
(14, 'xujie', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '徐杰', NULL, '13800003006', 'xujie@example.com', 1, 9, 9, 1),
(15, 'hedan', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '何丹', NULL, '13800003007', 'hedan@example.com', 2, 9, 10, 1),
(16, 'guomin', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '郭敏', NULL, '13800003008', 'guomin@example.com', 2, 10, 14, 1),
(17, 'malong', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '马龙', NULL, '13800003009', 'malong@example.com', 1, 10, 14, 1),

-- 市场部员工
(18, 'gaofeng', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '高峰', NULL, '13800004001', 'gaofeng@example.com', 1, 11, 15, 1),
(19, 'fangling', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '方玲', NULL, '13800004002', 'fangling@example.com', 2, 11, 15, 1),
(20, 'dengchao', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '邓超', NULL, '13800004003', 'dengchao@example.com', 1, 12, 16, 1),
(21, 'xiaoyan', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '肖燕', NULL, '13800004004', 'xiaoyan@example.com', 2, 12, 16, 1),

-- HR部员工
(22, 'tanghua', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '唐华', NULL, '13800005001', 'tanghua@example.com', 2, 5, 17, 1),
(23, 'songjie', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '宋洁', NULL, '13800005002', 'songjie@example.com', 2, 5, 17, 1),

-- 财务部员工
(24, 'hanjun', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '韩军', NULL, '13800006001', 'hanjun@example.com', 1, 6, 18, 1),
(25, 'pengyan', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '彭艳', NULL, '13800006002', 'pengyan@example.com', 2, 6, 18, 1),

-- 行政部员工
(26, 'cenghui', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '曾辉', NULL, '13800007001', 'cenghui@example.com', 1, 7, 19, 1),
(27, 'luoxia', '$2a$10$eDQYO3v/i3tdFlrCPlNEtuHKU4ajjbFCRf.JU1a6UWYzKGbgUzsLu', '123456', 0, '罗霞', NULL, '13800007002', 'luoxia@example.com', 2, 7, 19, 1);

-- ========================================
-- 5. 用户角色关联
-- ========================================
DELETE FROM sys_user_role WHERE user_id > 1;
INSERT INTO sys_user_role (user_id, role_id) VALUES
-- admin已有super_admin角色
(2, 2),  -- 张伟 - 高管
(3, 2),  -- 李娜 - 高管
(4, 6),  -- 王强 - 部门经理（技术总监）
(5, 6),  -- 赵静 - 部门经理（市场总监）
(6, 3),  -- 孙丽 - HR管理员
(7, 5),  -- 周军 - 财务管理员
(8, 4),  -- 吴英 - 行政管理员
(9, 6),  -- 陈飞 - 部门经理（前端组长）
(10, 7), -- 刘洋 - 员工
(11, 7), -- 黄鑫 - 员工
(12, 7), -- 林美 - 员工
(13, 6), -- 杨凯 - 部门经理（后端组长）
(14, 7), -- 徐杰 - 员工
(15, 7), -- 何丹 - 员工
(16, 7), -- 郭敏 - 员工
(17, 7), -- 马龙 - 员工
(18, 7), -- 高峰 - 员工
(19, 7), -- 方玲 - 员工
(20, 7), -- 邓超 - 员工
(21, 7), -- 肖燕 - 员工
(22, 7), -- 唐华 - 员工
(23, 7), -- 宋洁 - 员工
(24, 7), -- 韩军 - 员工
(25, 7), -- 彭艳 - 员工
(26, 7), -- 曾辉 - 员工
(27, 7); -- 罗霞 - 员工

-- ========================================
-- 6. 字典类型数据
-- ========================================
DELETE FROM sys_dict_type WHERE id > 0;
INSERT INTO sys_dict_type (id, dict_type, dict_name, remark, status) VALUES
(1, 'sys_user_gender', '用户性别', '用户性别字典', 1),
(2, 'sys_user_status', '用户状态', '用户状态字典', 1),
(3, 'leave_type', '请假类型', '请假类型字典', 1),
(4, 'expense_type', '报销类型', '报销类型字典', 1),
(5, 'overtime_type', '加班类型', '加班类型字典', 1),
(6, 'approval_status', '审批状态', '审批状态字典', 1),
(7, 'attendance_status', '考勤状态', '考勤状态字典', 1);

-- ========================================
-- 7. 字典数据
-- ========================================
DELETE FROM sys_dict_data WHERE id > 0;
INSERT INTO sys_dict_data (id, dict_type, dict_label, dict_value, remark, sort, status) VALUES
-- 性别
(1, 'sys_user_gender', '男', '1', '男性', 1, 1),
(2, 'sys_user_gender', '女', '2', '女性', 2, 1),
(3, 'sys_user_gender', '未知', '0', '未知性别', 3, 1),
-- 用户状态
(4, 'sys_user_status', '正常', '1', '正常状态', 1, 1),
(5, 'sys_user_status', '停用', '0', '停用状态', 2, 1),
-- 请假类型
(6, 'leave_type', '年假', 'annual', '带薪年假', 1, 1),
(7, 'leave_type', '病假', 'sick', '因病请假', 2, 1),
(8, 'leave_type', '事假', 'personal', '私事请假', 3, 1),
(9, 'leave_type', '婚假', 'marriage', '结婚请假', 4, 1),
(10, 'leave_type', '产假', 'maternity', '产假', 5, 1),
(11, 'leave_type', '陪产假', 'paternity', '陪产假', 6, 1),
(12, 'leave_type', '丧假', 'bereavement', '丧事请假', 7, 1),
(13, 'leave_type', '调休', 'compensatory', '调休假', 8, 1),
-- 报销类型
(14, 'expense_type', '差旅费', 'travel', '出差相关费用', 1, 1),
(15, 'expense_type', '餐饮费', 'meal', '工作餐费用', 2, 1),
(16, 'expense_type', '交通费', 'transport', '交通出行费用', 3, 1),
(17, 'expense_type', '通讯费', 'communication', '电话通讯费', 4, 1),
(18, 'expense_type', '办公费', 'office', '办公用品费用', 5, 1),
(19, 'expense_type', '其他', 'other', '其他费用', 6, 1),
-- 加班类型
(20, 'overtime_type', '工作日加班', 'workday', '工作日晚上加班', 1, 1),
(21, 'overtime_type', '周末加班', 'weekend', '周末加班', 2, 1),
(22, 'overtime_type', '节假日加班', 'holiday', '法定节假日加班', 3, 1),
-- 审批状态
(23, 'approval_status', '草稿', 'draft', '未提交', 1, 1),
(24, 'approval_status', '审批中', 'pending', '等待审批', 2, 1),
(25, 'approval_status', '已通过', 'approved', '审批通过', 3, 1),
(26, 'approval_status', '已驳回', 'rejected', '审批驳回', 4, 1),
(27, 'approval_status', '已取消', 'cancelled', '已撤销取消', 5, 1),
-- 考勤状态
(28, 'attendance_status', '正常', 'normal', '正常打卡', 1, 1),
(29, 'attendance_status', '迟到', 'late', '迟到', 2, 1),
(30, 'attendance_status', '早退', 'early', '早退', 3, 1),
(31, 'attendance_status', '缺卡', 'missing', '缺卡', 4, 1),
(32, 'attendance_status', '请假', 'leave', '请假', 5, 1),
(33, 'attendance_status', '出差', 'business', '出差', 6, 1);

-- ========================================
-- 8. 假期余额数据（2026年）
-- ========================================
TRUNCATE TABLE biz_leave_balance;
INSERT INTO biz_leave_balance (user_id, year, leave_type, total_days, used_days, remaining_days) VALUES
-- 为所有员工初始化假期余额
(2, 2026, 'annual', 15, 2, 13),
(2, 2026, 'sick', 15, 0, 15),
(2, 2026, 'personal', 5, 1, 4),
(3, 2026, 'annual', 15, 3, 12),
(3, 2026, 'sick', 15, 1, 14),
(3, 2026, 'personal', 5, 0, 5),
(4, 2026, 'annual', 12, 1, 11),
(4, 2026, 'sick', 15, 0, 15),
(4, 2026, 'personal', 5, 2, 3),
(5, 2026, 'annual', 12, 0, 12),
(5, 2026, 'sick', 15, 0, 15),
(5, 2026, 'personal', 5, 0, 5),
(6, 2026, 'annual', 10, 2, 8),
(6, 2026, 'sick', 15, 1, 14),
(6, 2026, 'personal', 5, 1, 4),
(7, 2026, 'annual', 10, 0, 10),
(7, 2026, 'sick', 15, 0, 15),
(7, 2026, 'personal', 5, 0, 5),
(8, 2026, 'annual', 10, 1, 9),
(8, 2026, 'sick', 15, 0, 15),
(8, 2026, 'personal', 5, 0, 5),
(9, 2026, 'annual', 10, 0, 10),
(9, 2026, 'sick', 15, 2, 13),
(9, 2026, 'personal', 5, 1, 4),
(10, 2026, 'annual', 7, 1, 6),
(10, 2026, 'sick', 15, 0, 15),
(10, 2026, 'personal', 5, 0, 5),
(11, 2026, 'annual', 5, 0, 5),
(11, 2026, 'sick', 15, 1, 14),
(11, 2026, 'personal', 5, 1, 4);

-- ========================================
-- 9. 请假申请示例数据
-- ========================================
TRUNCATE TABLE biz_leave_application;
INSERT INTO biz_leave_application (id, apply_no, process_instance_id, applicant_id, applicant_name, dept_id, dept_name, leave_type, start_time, end_time, duration, reason, handover_user_id, handover_user_name, status) VALUES
(1, 'QJ202601150001', NULL, 10, '刘洋', 8, '前端开发组', 'annual', '2026-01-20 09:00:00', '2026-01-21 18:00:00', 16, '回老家办理证件，需要请假两天', 11, '黄鑫', 'approved'),
(2, 'QJ202601160001', NULL, 11, '黄鑫', 8, '前端开发组', 'sick', '2026-01-18 09:00:00', '2026-01-18 18:00:00', 8, '感冒发烧，需要休息一天', 10, '刘洋', 'approved'),
(3, 'QJ202601170001', NULL, 14, '徐杰', 9, '后端开发组', 'personal', '2026-01-25 14:00:00', '2026-01-25 18:00:00', 4, '银行办理业务，需要请半天事假', 15, '何丹', 'pending'),
(4, 'QJ202601180001', NULL, 18, '高峰', 11, '市场推广组', 'annual', '2026-02-01 09:00:00', '2026-02-05 18:00:00', 40, '春节后回老家探亲，请年假5天', 19, '方玲', 'draft'),
(5, 'QJ202601190001', NULL, 22, '唐华', 5, '人力资源部', 'marriage', '2026-03-10 09:00:00', '2026-03-20 18:00:00', 88, '结婚请婚假，共11天', 23, '宋洁', 'pending');

-- ========================================
-- 10. 报销申请示例数据
-- ========================================
TRUNCATE TABLE biz_expense_application;
INSERT INTO biz_expense_application (id, apply_no, process_instance_id, applicant_id, applicant_name, dept_id, dept_name, expense_type, total_amount, reason, payee_name, payee_bank, payee_account, status) VALUES
(1, 'BX202601150001', NULL, 4, '王强', 3, '技术研发中心', 'travel', 3580.00, '北京出差参加技术大会，包含机票、酒店、餐饮费用', '王强', '中国工商银行', '6222021234567890123', 'approved'),
(2, 'BX202601160001', NULL, 18, '高峰', 11, '市场推广组', 'transport', 850.00, '客户拜访交通费报销', '高峰', '中国建设银行', '6217001234567890123', 'pending'),
(3, 'BX202601170001', NULL, 10, '刘洋', 8, '前端开发组', 'meal', 320.00, '项目加班晚餐费用报销', '刘洋', '中国农业银行', '6228481234567890123', 'approved'),
(4, 'BX202601180001', NULL, 5, '赵静', 4, '市场营销部', 'travel', 12500.00, '上海参加行业展会费用，含机票、酒店、展位费', '赵静', '招商银行', '6225881234567890123', 'pending'),
(5, 'BX202601190001', NULL, 26, '曾辉', 7, '行政部', 'office', 1680.00, '办公用品采购：打印纸、文件夹、笔等', '曾辉', '中国银行', '6216611234567890123', 'draft');

-- ========================================
-- 11. 报销明细示例数据
-- ========================================
TRUNCATE TABLE biz_expense_detail;
INSERT INTO biz_expense_detail (expense_id, fee_type, fee_date, amount, description, invoice_no) VALUES
(1, 'transport', '2026-01-10', 1200.00, '北京往返机票', 'FP20260110001'),
(1, 'meal', '2026-01-11', 280.00, '住宿期间餐费', 'FP20260111001'),
(1, 'travel', '2026-01-10', 1800.00, '酒店住宿2晚', 'FP20260112001'),
(1, 'transport', '2026-01-12', 300.00, '市内交通费', 'FP20260112002'),
(2, 'transport', '2026-01-14', 450.00, '客户拜访打车费', 'FP20260114001'),
(2, 'transport', '2026-01-15', 400.00, '客户拜访打车费', 'FP20260115001'),
(3, 'meal', '2026-01-13', 120.00, '项目加班晚餐', 'FP20260113001'),
(3, 'meal', '2026-01-14', 100.00, '项目加班晚餐', 'FP20260114002'),
(3, 'meal', '2026-01-15', 100.00, '项目加班晚餐', 'FP20260115002'),
(4, 'transport', '2026-01-20', 2400.00, '上海往返机票', 'FP20260120001'),
(4, 'travel', '2026-01-20', 3600.00, '酒店住宿3晚', 'FP20260120002'),
(4, 'other', '2026-01-21', 5800.00, '展位费用', 'FP20260121001'),
(4, 'meal', '2026-01-22', 700.00, '展会期间餐费', 'FP20260122001'),
(5, 'office', '2026-01-16', 680.00, 'A4打印纸20箱', 'FP20260116001'),
(5, 'office', '2026-01-16', 450.00, '文件夹、档案盒', 'FP20260116002'),
(5, 'office', '2026-01-16', 550.00, '签字笔、记号笔等', 'FP20260116003');

-- ========================================
-- 12. 更新部门负责人（已在插入时设置）
-- ========================================

SELECT '======================================' AS '';
SELECT '测试数据初始化完成！' AS message;
SELECT '======================================' AS '';
SELECT CONCAT('共创建 ', COUNT(*), ' 个用户') AS info FROM sys_user WHERE deleted = 0;
SELECT CONCAT('共创建 ', COUNT(*), ' 个部门') AS info FROM org_department WHERE deleted = 0;
SELECT CONCAT('共创建 ', COUNT(*), ' 个岗位') AS info FROM org_position WHERE deleted = 0;
SELECT CONCAT('共创建 ', COUNT(*), ' 个角色') AS info FROM sys_role WHERE deleted = 0;
SELECT CONCAT('共创建 ', COUNT(*), ' 条字典类型') AS info FROM sys_dict_type WHERE deleted = 0;
SELECT CONCAT('共创建 ', COUNT(*), ' 条字典数据') AS info FROM sys_dict_data WHERE deleted = 0;
SELECT CONCAT('共创建 ', COUNT(*), ' 条请假申请') AS info FROM biz_leave_application WHERE deleted = 0;
SELECT CONCAT('共创建 ', COUNT(*), ' 条报销申请') AS info FROM biz_expense_application WHERE deleted = 0;
