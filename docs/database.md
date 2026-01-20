# OA 办公系统 - 数据库设计文档

> 数据库：MySQL 8.0  
> 字符集：utf8mb4  
> 排序规则：utf8mb4_general_ci

---

## 一、表命名规范

| 前缀 | 模块 | 示例 |
|------|------|------|
| `sys_` | 系统管理 | sys_user, sys_role |
| `org_` | 组织架构 | org_department, org_employee |
| `wf_` | 工作流 | wf_process_def, wf_form_def |
| `biz_` | 业务申请 | biz_leave, biz_expense |
| `oa_` | 日常办公 | oa_schedule, oa_notice |

---

## 二、公共字段

所有表包含以下公共字段：

```sql
id              BIGINT          PRIMARY KEY AUTO_INCREMENT  -- 主键
create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP   -- 创建时间
update_time     DATETIME        ON UPDATE CURRENT_TIMESTAMP -- 更新时间
create_by       BIGINT          NULL                        -- 创建人
update_by       BIGINT          NULL                        -- 更新人
deleted         TINYINT(1)      DEFAULT 0                   -- 逻辑删除(0否1是)
```

---

## 三、表结构设计

### 3.1 系统管理模块

#### sys_user 用户表

```sql
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
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT(1)      DEFAULT 0,
    INDEX idx_dept_id (dept_id),
    INDEX idx_username (username)
) COMMENT '用户表';
```

#### sys_role 角色表

```sql
CREATE TABLE sys_role (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_name       VARCHAR(50)     NOT NULL COMMENT '角色名称',
    role_code       VARCHAR(50)     NOT NULL UNIQUE COMMENT '角色编码',
    sort            INT             DEFAULT 0 COMMENT '排序',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0禁用1启用)',
    data_scope      TINYINT(1)      DEFAULT 1 COMMENT '数据范围(1全部2本部门及以下3本部门4仅本人)',
    remark          VARCHAR(255)    NULL COMMENT '备注',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT(1)      DEFAULT 0
) COMMENT '角色表';
```

#### sys_user_role 用户角色关联表

```sql
CREATE TABLE sys_user_role (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    role_id         BIGINT          NOT NULL COMMENT '角色ID',
    UNIQUE INDEX idx_user_role (user_id, role_id)
) COMMENT '用户角色关联表';
```

#### sys_menu 菜单表

```sql
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
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id)
) COMMENT '菜单表';
```

#### sys_role_menu 角色菜单关联表

```sql
CREATE TABLE sys_role_menu (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT,
    role_id         BIGINT          NOT NULL COMMENT '角色ID',
    menu_id         BIGINT          NOT NULL COMMENT '菜单ID',
    UNIQUE INDEX idx_role_menu (role_id, menu_id)
) COMMENT '角色菜单关联表';
```

#### sys_dict_type 字典类型表

```sql
CREATE TABLE sys_dict_type (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '字典ID',
    dict_name       VARCHAR(100)    NOT NULL COMMENT '字典名称',
    dict_type       VARCHAR(100)    NOT NULL UNIQUE COMMENT '字典类型',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0禁用1启用)',
    remark          VARCHAR(255)    NULL COMMENT '备注',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP
) COMMENT '字典类型表';
```

#### sys_dict_data 字典数据表

```sql
CREATE TABLE sys_dict_data (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '字典数据ID',
    dict_type       VARCHAR(100)    NOT NULL COMMENT '字典类型',
    dict_label      VARCHAR(100)    NOT NULL COMMENT '字典标签',
    dict_value      VARCHAR(100)    NOT NULL COMMENT '字典键值',
    sort            INT             DEFAULT 0 COMMENT '排序',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0禁用1启用)',
    remark          VARCHAR(255)    NULL COMMENT '备注',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_dict_type (dict_type)
) COMMENT '字典数据表';
```

#### sys_config 系统参数表

```sql
CREATE TABLE sys_config (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '参数ID',
    config_name     VARCHAR(100)    NOT NULL COMMENT '参数名称',
    config_key      VARCHAR(100)    NOT NULL UNIQUE COMMENT '参数键名',
    config_value    VARCHAR(500)    NOT NULL COMMENT '参数键值',
    config_type     CHAR(1)         DEFAULT 'N' COMMENT '系统内置(Y是N否)',
    remark          VARCHAR(255)    NULL COMMENT '备注',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP
) COMMENT '系统参数表';
```

#### sys_log 操作日志表

```sql
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
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) COMMENT '操作日志表';
```

#### sys_login_log 登录日志表

```sql
CREATE TABLE sys_login_log (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id         BIGINT          NULL COMMENT '用户ID',
    username        VARCHAR(50)     NULL COMMENT '用户名',
    ip              VARCHAR(50)     NULL COMMENT '登录IP',
    location        VARCHAR(100)    NULL COMMENT '登录地点',
    browser         VARCHAR(50)     NULL COMMENT '浏览器',
    os              VARCHAR(50)     NULL COMMENT '操作系统',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0失败1成功)',
    msg             VARCHAR(255)    NULL COMMENT '提示消息',
    login_time      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time)
) COMMENT '登录日志表';
```

---

### 3.2 组织架构模块

#### org_department 部门表

```sql
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
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT(1)      DEFAULT 0,
    INDEX idx_parent_id (parent_id)
) COMMENT '部门表';
```

#### org_position 岗位表

```sql
CREATE TABLE org_position (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '岗位ID',
    position_name   VARCHAR(50)     NOT NULL COMMENT '岗位名称',
    position_code   VARCHAR(50)     NOT NULL UNIQUE COMMENT '岗位编码',
    position_level  INT             DEFAULT 1 COMMENT '岗位职级',
    sort            INT             DEFAULT 0 COMMENT '排序',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0禁用1启用)',
    remark          VARCHAR(255)    NULL COMMENT '备注',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT(1)      DEFAULT 0
) COMMENT '岗位表';
```

#### org_employee 员工档案表

```sql
CREATE TABLE org_employee (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '员工ID',
    user_id         BIGINT          NOT NULL UNIQUE COMMENT '关联用户ID',
    emp_no          VARCHAR(50)     NOT NULL UNIQUE COMMENT '工号',
    real_name       VARCHAR(50)     NOT NULL COMMENT '姓名',
    gender          TINYINT(1)      DEFAULT 0 COMMENT '性别(0未知1男2女)',
    birthday        DATE            NULL COMMENT '生日',
    id_card         VARCHAR(18)     NULL COMMENT '身份证号',
    phone           VARCHAR(20)     NULL COMMENT '手机号',
    email           VARCHAR(100)    NULL COMMENT '邮箱',
    address         VARCHAR(255)    NULL COMMENT '家庭住址',
    dept_id         BIGINT          NULL COMMENT '部门ID',
    position_id     BIGINT          NULL COMMENT '岗位ID',
    hire_date       DATE            NULL COMMENT '入职日期',
    regular_date    DATE            NULL COMMENT '转正日期',
    contract_start  DATE            NULL COMMENT '合同开始日期',
    contract_end    DATE            NULL COMMENT '合同结束日期',
    education       VARCHAR(20)     NULL COMMENT '最高学历',
    school          VARCHAR(100)    NULL COMMENT '毕业院校',
    major           VARCHAR(100)    NULL COMMENT '专业',
    emergency_name  VARCHAR(50)     NULL COMMENT '紧急联系人',
    emergency_phone VARCHAR(20)     NULL COMMENT '紧急联系电话',
    emergency_relation VARCHAR(20)  NULL COMMENT '紧急联系人关系',
    bank_name       VARCHAR(100)    NULL COMMENT '开户银行',
    bank_account    VARCHAR(50)     NULL COMMENT '银行账号',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(1在职2试用3离职)',
    leave_date      DATE            NULL COMMENT '离职日期',
    leave_reason    VARCHAR(255)    NULL COMMENT '离职原因',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT(1)      DEFAULT 0,
    INDEX idx_user_id (user_id),
    INDEX idx_dept_id (dept_id)
) COMMENT '员工档案表';
```

---

### 3.3 工作流模块

#### wf_process_def 流程定义扩展表

```sql
CREATE TABLE wf_process_def (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    process_key     VARCHAR(100)    NOT NULL UNIQUE COMMENT '流程标识',
    process_name    VARCHAR(100)    NOT NULL COMMENT '流程名称',
    category        VARCHAR(50)     NULL COMMENT '流程分类',
    form_id         BIGINT          NULL COMMENT '关联表单ID',
    bpmn_xml        TEXT            NULL COMMENT 'BPMN XML内容',
    version         INT             DEFAULT 1 COMMENT '版本号',
    status          TINYINT(1)      DEFAULT 0 COMMENT '状态(0草稿1已发布2已停用)',
    remark          VARCHAR(255)    NULL COMMENT '备注',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    create_by       BIGINT          NULL,
    deleted         TINYINT(1)      DEFAULT 0
) COMMENT '流程定义扩展表';
```

#### wf_form_def 表单定义表

```sql
CREATE TABLE wf_form_def (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT '表单ID',
    form_name       VARCHAR(100)    NOT NULL COMMENT '表单名称',
    form_key        VARCHAR(100)    NOT NULL UNIQUE COMMENT '表单标识',
    form_json       TEXT            NOT NULL COMMENT '表单JSON配置',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0禁用1启用)',
    remark          VARCHAR(255)    NULL COMMENT '备注',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    create_by       BIGINT          NULL,
    deleted         TINYINT(1)      DEFAULT 0
) COMMENT '表单定义表';
```

#### wf_process_inst_ext 流程实例扩展表

```sql
CREATE TABLE wf_process_inst_ext (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    process_inst_id VARCHAR(64)     NOT NULL UNIQUE COMMENT 'Flowable流程实例ID',
    process_def_id  VARCHAR(64)     NOT NULL COMMENT 'Flowable流程定义ID',
    process_key     VARCHAR(100)    NOT NULL COMMENT '流程标识',
    title           VARCHAR(200)    NOT NULL COMMENT '流程标题',
    initiator_id    BIGINT          NOT NULL COMMENT '发起人ID',
    initiator_name  VARCHAR(50)     NULL COMMENT '发起人姓名',
    business_key    VARCHAR(100)    NULL COMMENT '业务主键',
    business_type   VARCHAR(50)     NULL COMMENT '业务类型(leave/expense/overtime等)',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(1进行中2已完成3已取消4已驳回)',
    start_time      DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time        DATETIME        NULL COMMENT '结束时间',
    duration        BIGINT          NULL COMMENT '耗时(ms)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_initiator_id (initiator_id),
    INDEX idx_process_key (process_key),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) COMMENT '流程实例扩展表';
```

#### wf_approval_record 审批记录表

```sql
CREATE TABLE wf_approval_record (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    process_inst_id VARCHAR(64)     NOT NULL COMMENT '流程实例ID',
    task_id         VARCHAR(64)     NOT NULL COMMENT '任务ID',
    task_name       VARCHAR(100)    NULL COMMENT '任务名称',
    assignee_id     BIGINT          NOT NULL COMMENT '审批人ID',
    assignee_name   VARCHAR(50)     NULL COMMENT '审批人姓名',
    action          VARCHAR(20)     NOT NULL COMMENT '操作(approve/reject/return/transfer/withdraw)',
    comment         VARCHAR(500)    NULL COMMENT '审批意见',
    duration        BIGINT          NULL COMMENT '处理耗时(ms)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '审批时间',
    INDEX idx_process_inst_id (process_inst_id),
    INDEX idx_assignee_id (assignee_id)
) COMMENT '审批记录表';
```

---

### 3.4 业务申请模块

#### biz_leave 请假申请表

```sql
CREATE TABLE biz_leave (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    process_inst_id VARCHAR(64)     NULL COMMENT '流程实例ID',
    user_id         BIGINT          NOT NULL COMMENT '申请人ID',
    leave_type      VARCHAR(20)     NOT NULL COMMENT '请假类型(annual/personal/sick/marriage/maternity/paternity/funeral/compensatory)',
    start_time      DATETIME        NOT NULL COMMENT '开始时间',
    end_time        DATETIME        NOT NULL COMMENT '结束时间',
    duration        DECIMAL(5,1)    NOT NULL COMMENT '请假时长(天)',
    reason          VARCHAR(500)    NOT NULL COMMENT '请假事由',
    attachments     VARCHAR(1000)   NULL COMMENT '附件URL(JSON数组)',
    status          TINYINT(1)      DEFAULT 0 COMMENT '状态(0草稿1审批中2已通过3已驳回4已撤回)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) COMMENT '请假申请表';
```

#### biz_expense 报销申请表

```sql
CREATE TABLE biz_expense (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    process_inst_id VARCHAR(64)     NULL COMMENT '流程实例ID',
    user_id         BIGINT          NOT NULL COMMENT '申请人ID',
    expense_no      VARCHAR(50)     NOT NULL UNIQUE COMMENT '报销单号',
    expense_type    VARCHAR(20)     NOT NULL COMMENT '报销类型(travel/traffic/meal/office/entertainment/training/other)',
    total_amount    DECIMAL(10,2)   NOT NULL COMMENT '报销总金额',
    remark          VARCHAR(500)    NULL COMMENT '备注说明',
    status          TINYINT(1)      DEFAULT 0 COMMENT '状态(0草稿1审批中2已通过3已驳回4已撤回)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) COMMENT '报销申请表';
```

#### biz_expense_item 报销明细表

```sql
CREATE TABLE biz_expense_item (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    expense_id      BIGINT          NOT NULL COMMENT '报销申请ID',
    item_type       VARCHAR(20)     NOT NULL COMMENT '费用类型',
    amount          DECIMAL(10,2)   NOT NULL COMMENT '金额',
    occur_date      DATE            NOT NULL COMMENT '发生日期',
    remark          VARCHAR(255)    NULL COMMENT '说明',
    invoice_url     VARCHAR(500)    NULL COMMENT '发票附件URL',
    INDEX idx_expense_id (expense_id)
) COMMENT '报销明细表';
```

#### biz_overtime 加班申请表

```sql
CREATE TABLE biz_overtime (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    process_inst_id VARCHAR(64)     NULL COMMENT '流程实例ID',
    user_id         BIGINT          NOT NULL COMMENT '申请人ID',
    overtime_type   VARCHAR(20)     NOT NULL COMMENT '加班类型(workday/weekend/holiday)',
    start_time      DATETIME        NOT NULL COMMENT '开始时间',
    end_time        DATETIME        NOT NULL COMMENT '结束时间',
    duration        DECIMAL(5,1)    NOT NULL COMMENT '加班时长(小时)',
    reason          VARCHAR(500)    NOT NULL COMMENT '加班事由',
    compensatory    TINYINT(1)      DEFAULT 0 COMMENT '是否转调休(0否1是)',
    status          TINYINT(1)      DEFAULT 0 COMMENT '状态(0草稿1审批中2已通过3已驳回4已撤回)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) COMMENT '加班申请表';
```

#### biz_business_trip 出差申请表

```sql
CREATE TABLE biz_business_trip (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    process_inst_id VARCHAR(64)     NULL COMMENT '流程实例ID',
    user_id         BIGINT          NOT NULL COMMENT '申请人ID',
    destination     VARCHAR(100)    NOT NULL COMMENT '出差目的地',
    start_date      DATE            NOT NULL COMMENT '开始日期',
    end_date        DATE            NOT NULL COMMENT '结束日期',
    days            INT             NOT NULL COMMENT '出差天数',
    purpose         VARCHAR(500)    NOT NULL COMMENT '出差事由',
    budget          DECIMAL(10,2)   NULL COMMENT '预算金额',
    transport       VARCHAR(50)     NULL COMMENT '交通方式',
    status          TINYINT(1)      DEFAULT 0 COMMENT '状态(0草稿1审批中2已通过3已驳回4已撤回)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) COMMENT '出差申请表';
```

#### biz_card_replacement 补卡申请表

```sql
CREATE TABLE biz_card_replacement (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    process_inst_id VARCHAR(64)     NULL COMMENT '流程实例ID',
    user_id         BIGINT          NOT NULL COMMENT '申请人ID',
    replacement_date DATE           NOT NULL COMMENT '补卡日期',
    replacement_time TIME           NOT NULL COMMENT '补卡时间',
    replacement_type VARCHAR(20)    NOT NULL COMMENT '补卡类型(check_in/check_out)',
    reason          VARCHAR(500)    NOT NULL COMMENT '补卡原因',
    status          TINYINT(1)      DEFAULT 0 COMMENT '状态(0草稿1审批中2已通过3已驳回4已撤回)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) COMMENT '补卡申请表';
```

---

### 3.5 日常办公模块

#### oa_schedule 日程表

```sql
CREATE TABLE oa_schedule (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    title           VARCHAR(100)    NOT NULL COMMENT '日程标题',
    content         VARCHAR(500)    NULL COMMENT '日程内容',
    start_time      DATETIME        NOT NULL COMMENT '开始时间',
    end_time        DATETIME        NOT NULL COMMENT '结束时间',
    all_day         TINYINT(1)      DEFAULT 0 COMMENT '是否全天(0否1是)',
    location        VARCHAR(100)    NULL COMMENT '地点',
    remind_time     DATETIME        NULL COMMENT '提醒时间',
    category        VARCHAR(20)     DEFAULT 'work' COMMENT '分类(work/personal/meeting)',
    color           VARCHAR(20)     NULL COMMENT '颜色',
    repeat_type     VARCHAR(20)     NULL COMMENT '重复类型(none/daily/weekly/monthly)',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0取消1正常)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_start_time (start_time)
) COMMENT '日程表';
```

#### oa_notice 公告表

```sql
CREATE TABLE oa_notice (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    title           VARCHAR(200)    NOT NULL COMMENT '公告标题',
    content         TEXT            NOT NULL COMMENT '公告内容',
    notice_type     VARCHAR(20)     NOT NULL COMMENT '公告类型(notice/policy/news/activity)',
    publish_scope   VARCHAR(20)     DEFAULT 'all' COMMENT '发布范围(all/dept/custom)',
    scope_ids       VARCHAR(500)    NULL COMMENT '范围ID列表(JSON)',
    top             TINYINT(1)      DEFAULT 0 COMMENT '是否置顶(0否1是)',
    require_confirm TINYINT(1)      DEFAULT 0 COMMENT '是否需要确认(0否1是)',
    publish_time    DATETIME        NULL COMMENT '发布时间',
    attachments     VARCHAR(1000)   NULL COMMENT '附件URL(JSON数组)',
    creator_id      BIGINT          NOT NULL COMMENT '创建人ID',
    creator_name    VARCHAR(50)     NULL COMMENT '创建人姓名',
    status          TINYINT(1)      DEFAULT 0 COMMENT '状态(0草稿1已发布2已撤回)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT(1)      DEFAULT 0,
    INDEX idx_status (status),
    INDEX idx_publish_time (publish_time)
) COMMENT '公告表';
```

#### oa_notice_read 公告阅读记录表

```sql
CREATE TABLE oa_notice_read (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    notice_id       BIGINT          NOT NULL COMMENT '公告ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    read_time       DATETIME        DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    confirmed       TINYINT(1)      DEFAULT 0 COMMENT '是否确认(0否1是)',
    confirm_time    DATETIME        NULL COMMENT '确认时间',
    UNIQUE INDEX idx_notice_user (notice_id, user_id)
) COMMENT '公告阅读记录表';
```

#### oa_attendance 考勤记录表

```sql
CREATE TABLE oa_attendance (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    attendance_date DATE            NOT NULL COMMENT '考勤日期',
    check_in_time   DATETIME        NULL COMMENT '签到时间',
    check_out_time  DATETIME        NULL COMMENT '签退时间',
    check_in_status TINYINT(1)      NULL COMMENT '签到状态(0正常1迟到2缺勤3补卡)',
    check_out_status TINYINT(1)     NULL COMMENT '签退状态(0正常1早退2缺勤3补卡)',
    work_hours      DECIMAL(4,1)    NULL COMMENT '工作时长(小时)',
    check_in_ip     VARCHAR(50)     NULL COMMENT '签到IP',
    check_out_ip    VARCHAR(50)     NULL COMMENT '签退IP',
    check_in_location VARCHAR(200)  NULL COMMENT '签到地点',
    check_out_location VARCHAR(200) NULL COMMENT '签退地点',
    remark          VARCHAR(255)    NULL COMMENT '备注',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_user_date (user_id, attendance_date),
    INDEX idx_attendance_date (attendance_date)
) COMMENT '考勤记录表';
```

#### oa_attendance_rule 考勤规则表

```sql
CREATE TABLE oa_attendance_rule (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    rule_name       VARCHAR(50)     NOT NULL COMMENT '规则名称',
    work_start_time TIME            NOT NULL COMMENT '上班时间',
    work_end_time   TIME            NOT NULL COMMENT '下班时间',
    late_minutes    INT             DEFAULT 0 COMMENT '迟到阈值(分钟)',
    early_minutes   INT             DEFAULT 0 COMMENT '早退阈值(分钟)',
    flexible        TINYINT(1)      DEFAULT 0 COMMENT '是否弹性(0否1是)',
    flexible_minutes INT            DEFAULT 0 COMMENT '弹性时间(分钟)',
    is_default      TINYINT(1)      DEFAULT 0 COMMENT '是否默认规则',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0禁用1启用)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP
) COMMENT '考勤规则表';
```

#### oa_leave_balance 假期余额表

```sql
CREATE TABLE oa_leave_balance (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    year            INT             NOT NULL COMMENT '年份',
    leave_type      VARCHAR(20)     NOT NULL COMMENT '假期类型',
    total_days      DECIMAL(5,1)    NOT NULL DEFAULT 0 COMMENT '总天数',
    used_days       DECIMAL(5,1)    NOT NULL DEFAULT 0 COMMENT '已用天数',
    remaining_days  DECIMAL(5,1)    NOT NULL DEFAULT 0 COMMENT '剩余天数',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX idx_user_year_type (user_id, year, leave_type)
) COMMENT '假期余额表';
```

#### oa_meeting_room 会议室表

```sql
CREATE TABLE oa_meeting_room (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    room_name       VARCHAR(50)     NOT NULL COMMENT '会议室名称',
    location        VARCHAR(100)    NULL COMMENT '位置',
    capacity        INT             NOT NULL COMMENT '容纳人数',
    facilities      VARCHAR(255)    NULL COMMENT '设备设施(JSON)',
    description     VARCHAR(500)    NULL COMMENT '描述',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0停用1可用2维护中)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT(1)      DEFAULT 0
) COMMENT '会议室表';
```

#### oa_meeting 会议表

```sql
CREATE TABLE oa_meeting (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    room_id         BIGINT          NOT NULL COMMENT '会议室ID',
    title           VARCHAR(100)    NOT NULL COMMENT '会议主题',
    content         VARCHAR(500)    NULL COMMENT '会议内容',
    start_time      DATETIME        NOT NULL COMMENT '开始时间',
    end_time        DATETIME        NOT NULL COMMENT '结束时间',
    organizer_id    BIGINT          NOT NULL COMMENT '组织者ID',
    organizer_name  VARCHAR(50)     NULL COMMENT '组织者姓名',
    attendees       VARCHAR(1000)   NULL COMMENT '参会人员ID(JSON)',
    attachments     VARCHAR(1000)   NULL COMMENT '附件URL(JSON)',
    minutes         TEXT            NULL COMMENT '会议纪要',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0取消1待开始2进行中3已结束)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_room_id (room_id),
    INDEX idx_start_time (start_time)
) COMMENT '会议表';
```

#### oa_task 任务表

```sql
CREATE TABLE oa_task (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    title           VARCHAR(200)    NOT NULL COMMENT '任务标题',
    content         TEXT            NULL COMMENT '任务描述',
    assignee_id     BIGINT          NULL COMMENT '负责人ID',
    creator_id      BIGINT          NOT NULL COMMENT '创建人ID',
    priority        TINYINT(1)      DEFAULT 2 COMMENT '优先级(1紧急2高3中4低)',
    due_date        DATE            NULL COMMENT '截止日期',
    parent_id       BIGINT          DEFAULT 0 COMMENT '父任务ID',
    tags            VARCHAR(255)    NULL COMMENT '标签(JSON)',
    progress        INT             DEFAULT 0 COMMENT '进度(0-100)',
    status          TINYINT(1)      DEFAULT 0 COMMENT '状态(0待开始1进行中2已完成3已取消)',
    complete_time   DATETIME        NULL COMMENT '完成时间',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT(1)      DEFAULT 0,
    INDEX idx_assignee_id (assignee_id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_status (status)
) COMMENT '任务表';
```

#### oa_task_comment 任务评论表

```sql
CREATE TABLE oa_task_comment (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    task_id         BIGINT          NOT NULL COMMENT '任务ID',
    user_id         BIGINT          NOT NULL COMMENT '评论人ID',
    user_name       VARCHAR(50)     NULL COMMENT '评论人姓名',
    content         VARCHAR(1000)   NOT NULL COMMENT '评论内容',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_task_id (task_id)
) COMMENT '任务评论表';
```

#### oa_work_log 工作日志表

```sql
CREATE TABLE oa_work_log (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id         BIGINT          NOT NULL COMMENT '用户ID',
    log_type        VARCHAR(20)     NOT NULL COMMENT '日志类型(daily/weekly/monthly)',
    log_date        DATE            NOT NULL COMMENT '日志日期',
    content         TEXT            NOT NULL COMMENT '日志内容',
    tomorrow_plan   TEXT            NULL COMMENT '明日计划(日报)',
    problems        TEXT            NULL COMMENT '遇到的问题',
    submit_to       BIGINT          NULL COMMENT '提交给(上级ID)',
    status          TINYINT(1)      DEFAULT 0 COMMENT '状态(0草稿1已提交)',
    submit_time     DATETIME        NULL COMMENT '提交时间',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_log_date (log_date)
) COMMENT '工作日志表';
```

#### oa_document 文档表

```sql
CREATE TABLE oa_document (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    parent_id       BIGINT          DEFAULT 0 COMMENT '父文件夹ID',
    doc_name        VARCHAR(200)    NOT NULL COMMENT '文档名称',
    doc_type        VARCHAR(20)     NOT NULL COMMENT '类型(folder/file)',
    file_path       VARCHAR(500)    NULL COMMENT '文件路径',
    file_size       BIGINT          NULL COMMENT '文件大小(字节)',
    file_ext        VARCHAR(20)     NULL COMMENT '文件扩展名',
    creator_id      BIGINT          NOT NULL COMMENT '创建人ID',
    creator_name    VARCHAR(50)     NULL COMMENT '创建人姓名',
    download_count  INT             DEFAULT 0 COMMENT '下载次数',
    status          TINYINT(1)      DEFAULT 1 COMMENT '状态(0禁用1正常)',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NULL ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT(1)      DEFAULT 0,
    INDEX idx_parent_id (parent_id),
    INDEX idx_creator_id (creator_id)
) COMMENT '文档表';
```

#### oa_message 站内消息表

```sql
CREATE TABLE oa_message (
    id              BIGINT          PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    sender_id       BIGINT          NULL COMMENT '发送人ID(NULL为系统)',
    receiver_id     BIGINT          NOT NULL COMMENT '接收人ID',
    title           VARCHAR(200)    NOT NULL COMMENT '消息标题',
    content         TEXT            NOT NULL COMMENT '消息内容',
    msg_type        VARCHAR(20)     NOT NULL COMMENT '消息类型(system/approval/schedule/task)',
    business_type   VARCHAR(50)     NULL COMMENT '关联业务类型',
    business_id     BIGINT          NULL COMMENT '关联业务ID',
    is_read         TINYINT(1)      DEFAULT 0 COMMENT '是否已读(0否1是)',
    read_time       DATETIME        NULL COMMENT '阅读时间',
    create_time     DATETIME        DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_receiver_id (receiver_id),
    INDEX idx_is_read (is_read),
    INDEX idx_create_time (create_time)
) COMMENT '站内消息表';
```

---

## 四、初始数据

### 4.1 默认角色

```sql
INSERT INTO sys_role (role_name, role_code, sort, data_scope, remark) VALUES
('超级管理员', 'super_admin', 1, 1, '系统最高权限'),
('公司高管', 'company_leader', 2, 1, '查看全公司数据'),
('HR管理员', 'hr_admin', 3, 1, '人事管理权限'),
('行政管理员', 'admin_officer', 4, 1, '行政管理权限'),
('财务管理员', 'finance_admin', 5, 1, '财务审批权限'),
('部门经理', 'dept_manager', 6, 2, '部门管理权限'),
('普通员工', 'employee', 7, 4, '基本操作权限');
```

### 4.2 默认用户

```sql
INSERT INTO sys_user (username, password, real_name, status) VALUES
('admin', '$2a$10$...', '超级管理员', 1);

INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
```

### 4.3 默认字典

```sql
-- 性别
INSERT INTO sys_dict_type (dict_name, dict_type) VALUES ('性别', 'sys_gender');
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort) VALUES
('sys_gender', '未知', '0', 1),
('sys_gender', '男', '1', 2),
('sys_gender', '女', '2', 3);

-- 请假类型
INSERT INTO sys_dict_type (dict_name, dict_type) VALUES ('请假类型', 'leave_type');
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort) VALUES
('leave_type', '年假', 'annual', 1),
('leave_type', '事假', 'personal', 2),
('leave_type', '病假', 'sick', 3),
('leave_type', '婚假', 'marriage', 4),
('leave_type', '产假', 'maternity', 5),
('leave_type', '陪产假', 'paternity', 6),
('leave_type', '丧假', 'funeral', 7),
('leave_type', '调休', 'compensatory', 8);

-- 报销类型
INSERT INTO sys_dict_type (dict_name, dict_type) VALUES ('报销类型', 'expense_type');
INSERT INTO sys_dict_data (dict_type, dict_label, dict_value, sort) VALUES
('expense_type', '差旅费', 'travel', 1),
('expense_type', '交通费', 'traffic', 2),
('expense_type', '餐饮费', 'meal', 3),
('expense_type', '办公费', 'office', 4),
('expense_type', '招待费', 'entertainment', 5),
('expense_type', '培训费', 'training', 6),
('expense_type', '其他', 'other', 7);
```

---

## 五、索引设计原则

1. **主键索引**：所有表使用自增 BIGINT 作为主键
2. **唯一索引**：用户名、角色编码、字典类型等唯一字段
3. **外键索引**：所有外键字段添加普通索引
4. **查询索引**：高频查询字段（如状态、时间、用户ID）
5. **组合索引**：多条件查询场景（如用户+日期）

---

## 六、数据库维护

### 6.1 备份策略

- 每日全量备份
- 每小时增量备份
- 备份保留7天

### 6.2 性能优化

- 定期执行 ANALYZE TABLE
- 监控慢查询日志
- 大表考虑分区
