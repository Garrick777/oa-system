-- =========================================
-- OA办公系统数据库初始化脚本
-- =========================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS oa_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE oa_system;

-- =========================================
-- 系统管理表
-- =========================================

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username        VARCHAR(50)     NOT NULL UNIQUE COMMENT '用户名',
    password        VARCHAR(100)    NOT NULL COMMENT '密码',
    real_name       VARCHAR(50)     NULL COMMENT '真实姓名',
    avatar          VARCHAR(255)    NULL COMMENT '头像URL',
    phone           VARCHAR(20)     NULL COMMENT '手机号',
    email           VARCHAR(100)    NULL COMMENT '邮箱',
    gender          TINYINT(1)      DEFAULT 0 COMMENT '性别(0未知1男2女)',
    dept_id         BIGINT          NULL COMMENT '部门ID',
    position_id     BIGINT          NULL COMMENT '岗位ID',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0禁用1启用)',
    last_login_time DATETIME        NULL COMMENT '最后登录时间',
    last_login_ip   VARCHAR(50)     NULL COMMENT '最后登录IP',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT(1)      DEFAULT 0 COMMENT '是否删除',
    INDEX idx_dept_id (dept_id),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
DROP TABLE IF EXISTS sys_role;
CREATE TABLE sys_role (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name       VARCHAR(50)     NOT NULL COMMENT '角色名称',
    role_code       VARCHAR(50)     NOT NULL UNIQUE COMMENT '角色编码',
    sort            INT             DEFAULT 0 COMMENT '排序',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0禁用1启用)',
    data_scope      TINYINT(1)      DEFAULT 1 COMMENT '数据范围(1全部2本部门及以下3本部门4仅本人)',
    remark          VARCHAR(255)    NULL COMMENT '备注',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT(1)      DEFAULT 0 COMMENT '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
DROP TABLE IF EXISTS sys_user_role;
CREATE TABLE sys_user_role (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    role_id         BIGINT          NOT NULL COMMENT '角色ID',
    UNIQUE INDEX idx_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 菜单表
DROP TABLE IF EXISTS sys_menu;
CREATE TABLE sys_menu (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '菜单ID',
    parent_id       BIGINT          DEFAULT 0 COMMENT '父菜单ID',
    menu_name       VARCHAR(50)     NOT NULL COMMENT '菜单名称',
    menu_type       CHAR(1)         NOT NULL COMMENT '菜单类型(M目录C菜单F按钮)',
    path            VARCHAR(200)    NULL COMMENT '路由地址',
    component       VARCHAR(200)    NULL COMMENT '组件路径',
    perms           VARCHAR(100)    NULL COMMENT '权限标识',
    icon            VARCHAR(100)    NULL COMMENT '菜单图标',
    sort            INT             DEFAULT 0 COMMENT '排序',
    visible         TINYINT(1)      DEFAULT 1 COMMENT '是否可见(0否1是)',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0禁用1启用)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜单表';

-- 角色菜单关联表
DROP TABLE IF EXISTS sys_role_menu;
CREATE TABLE sys_role_menu (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    role_id         BIGINT          NOT NULL COMMENT '角色ID',
    menu_id         BIGINT          NOT NULL COMMENT '菜单ID',
    UNIQUE INDEX idx_role_menu (role_id, menu_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- 字典类型表
DROP TABLE IF EXISTS sys_dict_type;
CREATE TABLE sys_dict_type (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '字典ID',
    dict_name       VARCHAR(100)    NOT NULL COMMENT '字典名称',
    dict_type       VARCHAR(100)    NOT NULL UNIQUE COMMENT '字典类型',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0禁用1启用)',
    remark          VARCHAR(255)    NULL COMMENT '备注',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典类型表';

-- 字典数据表
DROP TABLE IF EXISTS sys_dict_data;
CREATE TABLE sys_dict_data (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '字典数据ID',
    dict_type       VARCHAR(100)    NOT NULL COMMENT '字典类型',
    dict_label      VARCHAR(100)    NOT NULL COMMENT '字典标签',
    dict_value      VARCHAR(100)    NOT NULL COMMENT '字典键值',
    sort            INT             DEFAULT 0 COMMENT '排序',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0禁用1启用)',
    remark          VARCHAR(255)    NULL COMMENT '备注',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='字典数据表';

-- 操作日志表
DROP TABLE IF EXISTS sys_log;
CREATE TABLE sys_log (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id         BIGINT          NULL COMMENT '操作用户ID',
    username        VARCHAR(50)     NULL COMMENT '操作用户名',
    operation       VARCHAR(50)     NULL COMMENT '操作描述',
    method          VARCHAR(200)    NULL COMMENT '请求方法',
    params          TEXT            NULL COMMENT '请求参数',
    result          TEXT            NULL COMMENT '返回结果',
    ip              VARCHAR(50)     NULL COMMENT '操作IP',
    duration        BIGINT          NULL COMMENT '执行时长(ms)',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0失败1成功)',
    error_msg       TEXT            NULL COMMENT '错误信息',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- =========================================
-- 组织架构表
-- =========================================

-- 部门表
DROP TABLE IF EXISTS org_department;
CREATE TABLE org_department (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '部门ID',
    parent_id       BIGINT          DEFAULT 0 COMMENT '父部门ID',
    ancestors       VARCHAR(500)    NULL COMMENT '祖级列表',
    dept_name       VARCHAR(50)     NOT NULL COMMENT '部门名称',
    dept_code       VARCHAR(50)     NULL COMMENT '部门编码',
    manager_id      BIGINT          NULL COMMENT '部门负责人ID',
    leader_id       BIGINT          NULL COMMENT '分管领导ID',
    phone           VARCHAR(20)     NULL COMMENT '联系电话',
    email           VARCHAR(100)    NULL COMMENT '邮箱',
    sort            INT             DEFAULT 0 COMMENT '排序',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0禁用1启用)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT(1)      DEFAULT 0 COMMENT '是否删除',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部门表';

-- 岗位表
DROP TABLE IF EXISTS org_position;
CREATE TABLE org_position (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '岗位ID',
    position_name   VARCHAR(50)     NOT NULL COMMENT '岗位名称',
    position_code   VARCHAR(50)     NOT NULL UNIQUE COMMENT '岗位编码',
    position_level  INT             DEFAULT 1 COMMENT '岗位职级',
    sort            INT             DEFAULT 0 COMMENT '排序',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0禁用1启用)',
    remark          VARCHAR(255)    NULL COMMENT '备注',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted         TINYINT(1)      DEFAULT 0 COMMENT '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='岗位表';

-- =========================================
-- 初始数据
-- =========================================

-- 初始角色
INSERT INTO sys_role (role_name, role_code, sort, data_scope, remark) VALUES
('超级管理员', 'super_admin', 1, 1, '系统最高权限'),
('公司高管', 'company_leader', 2, 1, '查看全公司数据'),
('HR管理员', 'hr_admin', 3, 1, '人事管理权限'),
('行政管理员', 'admin_officer', 4, 1, '行政管理权限'),
('财务管理员', 'finance_admin', 5, 1, '财务审批权限'),
('部门经理', 'dept_manager', 6, 2, '部门管理权限'),
('普通员工', 'employee', 7, 4, '基本操作权限');

-- 初始用户 (密码: admin123)
INSERT INTO sys_user (username, password, real_name, status) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKtYjqLOCwm.Y6A5Q5j3EAHqT5ve', '超级管理员', 1);

-- 关联超级管理员角色
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);

-- 初始部门
INSERT INTO org_department (dept_name, dept_code, sort) VALUES
('总公司', 'HQ', 1);

-- 初始岗位
INSERT INTO org_position (position_name, position_code, position_level, sort) VALUES
('总经理', 'GM', 10, 1),
('副总经理', 'DGM', 9, 2),
('部门经理', 'DM', 7, 3),
('主管', 'SUP', 5, 4),
('专员', 'STAFF', 3, 5);

-- 初始字典 - 性别
INSERT INTO sys_dict_type (dict_name, dict_type) VALUES ('性别', 'sys_gender');
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort) VALUES
('sys_gender', '未知', '0', 1),
('sys_gender', '男', '1', 2),
('sys_gender', '女', '2', 3);

-- 初始字典 - 状态
INSERT INTO sys_dict_type (dict_name, dict_type) VALUES ('状态', 'sys_status');
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort) VALUES
('sys_status', '禁用', '0', 1),
('sys_status', '启用', '1', 2);

-- 初始菜单
INSERT INTO sys_menu (parent_id, menu_name, menu_type, path, component, perms, icon, sort) VALUES
-- 系统管理
(0, '系统管理', 'M', '/system', '', '', 'Setting', 1),
(1, '用户管理', 'C', 'user', 'system/user/index', 'system:user:list', 'User', 1),
(1, '角色管理', 'C', 'role', 'system/role/index', 'system:role:list', 'UserFilled', 2),
(1, '菜单管理', 'C', 'menu', 'system/menu/index', 'system:menu:list', 'Menu', 3),
(1, '字典管理', 'C', 'dict', 'system/dict/index', 'system:dict:list', 'Collection', 4),
-- 组织架构
(0, '组织架构', 'M', '/org', '', '', 'OfficeBuilding', 2),
(6, '部门管理', 'C', 'dept', 'org/dept/index', 'org:dept:list', 'Grid', 1),
(6, '岗位管理', 'C', 'position', 'org/position/index', 'org:position:list', 'Stamp', 2),
(6, '员工档案', 'C', 'employee', 'org/employee/index', 'org:employee:list', 'Avatar', 3);

-- 给超级管理员分配所有菜单
INSERT INTO sys_role_menu (role_id, menu_id) 
SELECT 1, id FROM sys_menu;

SELECT '数据库初始化完成!' AS message;
