-- ========================================
-- OA办公系统 - 工作流模块数据库脚本
-- ========================================

USE oa_system;

-- ========================================
-- 1. 流程定义扩展表
-- ========================================
CREATE TABLE IF NOT EXISTS wf_process_definition_ext (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    process_definition_id VARCHAR(64) COMMENT 'Flowable流程定义ID',
    process_key VARCHAR(64) NOT NULL COMMENT '流程定义Key',
    process_name VARCHAR(128) NOT NULL COMMENT '流程名称',
    category VARCHAR(32) NOT NULL COMMENT '流程分类',
    icon VARCHAR(64) COMMENT '流程图标',
    form_id BIGINT COMMENT '关联表单ID',
    description VARCHAR(500) COMMENT '流程描述',
    version INT DEFAULT 1 COMMENT '版本号',
    status TINYINT DEFAULT 1 COMMENT '状态（0-停用，1-启用）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_process_key (process_key),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程定义扩展表';

-- ========================================
-- 2. 流程实例扩展表
-- ========================================
CREATE TABLE IF NOT EXISTS wf_process_instance_ext (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    process_instance_id VARCHAR(64) NOT NULL COMMENT 'Flowable流程实例ID',
    process_definition_id VARCHAR(64) COMMENT '流程定义ID',
    process_key VARCHAR(64) NOT NULL COMMENT '流程定义Key',
    process_name VARCHAR(128) COMMENT '流程名称',
    business_id VARCHAR(64) NOT NULL COMMENT '业务ID',
    business_type VARCHAR(32) NOT NULL COMMENT '业务类型',
    title VARCHAR(200) NOT NULL COMMENT '申请标题',
    initiator_id BIGINT NOT NULL COMMENT '发起人ID',
    initiator_name VARCHAR(64) COMMENT '发起人姓名',
    dept_id BIGINT COMMENT '发起人部门ID',
    dept_name VARCHAR(64) COMMENT '发起人部门名称',
    status VARCHAR(20) DEFAULT 'running' COMMENT '流程状态',
    current_assignee VARCHAR(200) COMMENT '当前审批人',
    current_task_name VARCHAR(128) COMMENT '当前任务节点名称',
    end_time DATETIME COMMENT '结束时间',
    duration BIGINT COMMENT '流程耗时（分钟）',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_process_instance_id (process_instance_id),
    INDEX idx_business (business_id, business_type),
    INDEX idx_initiator_id (initiator_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='流程实例扩展表';

-- ========================================
-- 3. 审批记录表
-- ========================================
CREATE TABLE IF NOT EXISTS wf_approval_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
    task_id VARCHAR(64) COMMENT '任务ID',
    task_name VARCHAR(128) COMMENT '任务名称',
    task_key VARCHAR(64) COMMENT '任务节点Key',
    assignee_id BIGINT NOT NULL COMMENT '审批人ID',
    assignee_name VARCHAR(64) COMMENT '审批人姓名',
    action VARCHAR(20) NOT NULL COMMENT '审批操作',
    comment VARCHAR(500) COMMENT '审批意见',
    attachments TEXT COMMENT '附件JSON',
    approval_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    duration BIGINT COMMENT '任务耗时（分钟）',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_process_instance_id (process_instance_id),
    INDEX idx_assignee_id (assignee_id),
    INDEX idx_approval_time (approval_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批记录表';

-- ========================================
-- 4. 表单定义表
-- ========================================
CREATE TABLE IF NOT EXISTS wf_form_definition (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    form_key VARCHAR(64) NOT NULL UNIQUE COMMENT '表单Key',
    form_name VARCHAR(128) NOT NULL COMMENT '表单名称',
    form_type VARCHAR(20) DEFAULT 'system' COMMENT '表单类型',
    form_config TEXT COMMENT '表单配置JSON',
    description VARCHAR(500) COMMENT '表单描述',
    status TINYINT DEFAULT 1 COMMENT '状态',
    version INT DEFAULT 1 COMMENT '版本号',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_form_key (form_key),
    INDEX idx_form_type (form_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='表单定义表';

-- ========================================
-- 5. 请假申请表
-- ========================================
CREATE TABLE IF NOT EXISTS biz_leave_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    apply_no VARCHAR(32) NOT NULL UNIQUE COMMENT '申请单号',
    process_instance_id VARCHAR(64) COMMENT '流程实例ID',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_name VARCHAR(64) COMMENT '申请人姓名',
    dept_id BIGINT COMMENT '部门ID',
    dept_name VARCHAR(64) COMMENT '部门名称',
    leave_type VARCHAR(20) NOT NULL COMMENT '请假类型',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    duration DECIMAL(10,2) NOT NULL COMMENT '请假时长（小时）',
    reason VARCHAR(500) NOT NULL COMMENT '请假事由',
    attachments TEXT COMMENT '附件JSON',
    handover_user_id BIGINT COMMENT '工作交接人ID',
    handover_user_name VARCHAR(64) COMMENT '工作交接人姓名',
    handover_remark VARCHAR(500) COMMENT '工作交接说明',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_apply_no (apply_no),
    INDEX idx_applicant_id (applicant_id),
    INDEX idx_status (status),
    INDEX idx_leave_type (leave_type),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假申请表';

-- ========================================
-- 6. 假期余额表
-- ========================================
CREATE TABLE IF NOT EXISTS biz_leave_balance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    year INT NOT NULL COMMENT '年度',
    leave_type VARCHAR(20) NOT NULL COMMENT '假期类型',
    total_days DECIMAL(5,2) DEFAULT 0 COMMENT '总天数',
    used_days DECIMAL(5,2) DEFAULT 0 COMMENT '已用天数',
    remaining_days DECIMAL(5,2) DEFAULT 0 COMMENT '剩余天数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_year_type (user_id, year, leave_type),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='假期余额表';

-- ========================================
-- 7. 报销申请表
-- ========================================
CREATE TABLE IF NOT EXISTS biz_expense_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    apply_no VARCHAR(32) NOT NULL UNIQUE COMMENT '申请单号',
    process_instance_id VARCHAR(64) COMMENT '流程实例ID',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_name VARCHAR(64) COMMENT '申请人姓名',
    dept_id BIGINT COMMENT '部门ID',
    dept_name VARCHAR(64) COMMENT '部门名称',
    expense_type VARCHAR(20) NOT NULL COMMENT '报销类型',
    total_amount DECIMAL(12,2) NOT NULL COMMENT '报销总金额',
    expense_details TEXT COMMENT '报销明细JSON',
    reason VARCHAR(500) NOT NULL COMMENT '报销事由',
    attachments TEXT COMMENT '附件JSON',
    payee_name VARCHAR(64) COMMENT '收款人姓名',
    payee_bank VARCHAR(64) COMMENT '收款银行',
    payee_account VARCHAR(32) COMMENT '收款账号',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_apply_no (apply_no),
    INDEX idx_applicant_id (applicant_id),
    INDEX idx_status (status),
    INDEX idx_expense_type (expense_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销申请表';

-- ========================================
-- 8. 报销明细表
-- ========================================
CREATE TABLE IF NOT EXISTS biz_expense_detail (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    expense_id BIGINT NOT NULL COMMENT '报销申请ID',
    fee_type VARCHAR(20) NOT NULL COMMENT '费用类型',
    fee_date DATE NOT NULL COMMENT '费用发生日期',
    amount DECIMAL(10,2) NOT NULL COMMENT '费用金额',
    description VARCHAR(200) COMMENT '费用说明',
    invoice_no VARCHAR(64) COMMENT '发票号',
    invoice_attachment VARCHAR(500) COMMENT '发票附件',
    INDEX idx_expense_id (expense_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报销明细表';

-- ========================================
-- 9. 加班申请表
-- ========================================
CREATE TABLE IF NOT EXISTS biz_overtime_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    apply_no VARCHAR(32) NOT NULL UNIQUE COMMENT '申请单号',
    process_instance_id VARCHAR(64) COMMENT '流程实例ID',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_name VARCHAR(64) COMMENT '申请人姓名',
    dept_id BIGINT COMMENT '部门ID',
    dept_name VARCHAR(64) COMMENT '部门名称',
    overtime_type VARCHAR(20) NOT NULL COMMENT '加班类型（workday-工作日, weekend-周末, holiday-节假日）',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    duration DECIMAL(10,2) NOT NULL COMMENT '加班时长（小时）',
    reason VARCHAR(500) NOT NULL COMMENT '加班事由',
    compensate_type VARCHAR(20) DEFAULT 'overtime_pay' COMMENT '补偿方式（overtime_pay-加班费, leave-调休）',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_applicant_id (applicant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='加班申请表';

-- ========================================
-- 10. 出差申请表
-- ========================================
CREATE TABLE IF NOT EXISTS biz_business_trip_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    apply_no VARCHAR(32) NOT NULL UNIQUE COMMENT '申请单号',
    process_instance_id VARCHAR(64) COMMENT '流程实例ID',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_name VARCHAR(64) COMMENT '申请人姓名',
    dept_id BIGINT COMMENT '部门ID',
    dept_name VARCHAR(64) COMMENT '部门名称',
    destination VARCHAR(200) NOT NULL COMMENT '出差目的地',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    duration INT NOT NULL COMMENT '出差天数',
    reason VARCHAR(500) NOT NULL COMMENT '出差事由',
    transport_type VARCHAR(20) COMMENT '交通方式',
    estimated_cost DECIMAL(10,2) COMMENT '预计费用',
    companions VARCHAR(500) COMMENT '同行人员',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_applicant_id (applicant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='出差申请表';

-- ========================================
-- 11. 外出申请表
-- ========================================
CREATE TABLE IF NOT EXISTS biz_go_out_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    apply_no VARCHAR(32) NOT NULL UNIQUE COMMENT '申请单号',
    process_instance_id VARCHAR(64) COMMENT '流程实例ID',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_name VARCHAR(64) COMMENT '申请人姓名',
    dept_id BIGINT COMMENT '部门ID',
    dept_name VARCHAR(64) COMMENT '部门名称',
    destination VARCHAR(200) NOT NULL COMMENT '外出目的地',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    duration DECIMAL(10,2) NOT NULL COMMENT '外出时长（小时）',
    reason VARCHAR(500) NOT NULL COMMENT '外出事由',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_applicant_id (applicant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='外出申请表';

-- ========================================
-- 12. 补卡申请表
-- ========================================
CREATE TABLE IF NOT EXISTS biz_card_replace_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    apply_no VARCHAR(32) NOT NULL UNIQUE COMMENT '申请单号',
    process_instance_id VARCHAR(64) COMMENT '流程实例ID',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_name VARCHAR(64) COMMENT '申请人姓名',
    dept_id BIGINT COMMENT '部门ID',
    dept_name VARCHAR(64) COMMENT '部门名称',
    card_date DATE NOT NULL COMMENT '补卡日期',
    card_type VARCHAR(20) NOT NULL COMMENT '补卡类型（sign_in-补签到, sign_out-补签退）',
    card_time TIME NOT NULL COMMENT '补卡时间',
    reason VARCHAR(500) NOT NULL COMMENT '补卡原因',
    attachments TEXT COMMENT '附件',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_applicant_id (applicant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='补卡申请表';

-- ========================================
-- 13. 通用审批申请表
-- ========================================
CREATE TABLE IF NOT EXISTS biz_general_application (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    apply_no VARCHAR(32) NOT NULL UNIQUE COMMENT '申请单号',
    process_instance_id VARCHAR(64) COMMENT '流程实例ID',
    applicant_id BIGINT NOT NULL COMMENT '申请人ID',
    applicant_name VARCHAR(64) COMMENT '申请人姓名',
    dept_id BIGINT COMMENT '部门ID',
    dept_name VARCHAR(64) COMMENT '部门名称',
    title VARCHAR(200) NOT NULL COMMENT '申请标题',
    content TEXT NOT NULL COMMENT '申请内容',
    attachments TEXT COMMENT '附件',
    status VARCHAR(20) DEFAULT 'draft' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人',
    update_by BIGINT COMMENT '更新人',
    deleted TINYINT DEFAULT 0 COMMENT '删除标记',
    INDEX idx_applicant_id (applicant_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用审批申请表';

-- ========================================
-- 初始化流程定义数据
-- ========================================
INSERT INTO wf_process_definition_ext (process_key, process_name, category, icon, description, sort_order) VALUES
('leave', '请假申请', 'leave', 'Calendar', '员工请假审批流程', 1),
('expense', '报销申请', 'expense', 'Money', '费用报销审批流程', 2),
('overtime', '加班申请', 'overtime', 'Clock', '员工加班审批流程', 3),
('business_trip', '出差申请', 'business_trip', 'Location', '员工出差审批流程', 4),
('go_out', '外出申请', 'go_out', 'Position', '员工外出审批流程', 5),
('card_replace', '补卡申请', 'card_replace', 'Ticket', '考勤补卡审批流程', 6),
('general', '通用审批', 'general', 'Document', '通用事项审批流程', 7);

-- ========================================
-- 初始化表单定义数据
-- ========================================
INSERT INTO wf_form_definition (form_key, form_name, form_type, description, sort_order) VALUES
('leave_form', '请假申请表单', 'system', '标准请假申请表单', 1),
('expense_form', '报销申请表单', 'system', '标准报销申请表单', 2),
('overtime_form', '加班申请表单', 'system', '标准加班申请表单', 3),
('business_trip_form', '出差申请表单', 'system', '标准出差申请表单', 4),
('go_out_form', '外出申请表单', 'system', '标准外出申请表单', 5),
('card_replace_form', '补卡申请表单', 'system', '标准补卡申请表单', 6),
('general_form', '通用审批表单', 'system', '通用事项申请表单', 7);
