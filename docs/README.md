# OA 办公自动化系统 - 开发文档

> 版本：1.0.0  
> 创建日期：2026-01-20  
> 项目路径：/Users/huang/Desktop/python_tutor_system

---

## 目录

1. [项目概述](#一项目概述)
2. [技术架构](#二技术架构)
3. [角色权限设计](#三角色权限设计)
4. [功能模块清单](#四功能模块清单)
5. [数据库设计](#五数据库设计)
6. [接口设计](#六接口设计)
7. [开发阶段规划](#七开发阶段规划)
8. [开发规范](#八开发规范)

---

## 一、项目概述

### 1.1 项目简介

OA办公自动化系统是一套面向中小企业的轻量级办公协同平台，实现日常办公流程数字化，提升企业协作效率。

### 1.2 核心价值

- 📋 **流程规范化**：审批流程标准化、可追溯
- 🚀 **效率提升**：减少纸质流转，加快审批速度
- 📊 **数据可视化**：考勤、审批等数据实时统计
- 🔐 **权限精细化**：角色权限、数据权限分离控制

### 1.3 系统边界

| 包含 | 不包含 |
|------|--------|
| 审批流程管理 | 财务核算 |
| 考勤打卡 | 薪酬计算 |
| 日程/任务协作 | 招聘管理 |
| 公告/文档管理 | 绩效考核 |
| 会议管理 | 培训管理 |

---

## 二、技术架构

### 2.1 技术栈

| 层级 | 技术选型 | 版本 |
|------|----------|------|
| **后端框架** | Spring Boot | 3.2.x |
| **JDK** | OpenJDK Temurin | 21 |
| **ORM** | MyBatis Plus | 3.5.5 |
| **数据库** | MySQL | 8.0 |
| **缓存** | Redis | 8.0 |
| **工作流** | Flowable | 7.0.x |
| **安全框架** | Spring Security + JWT | - |
| **API文档** | Knife4j | 4.4.x |
| **前端框架** | Vue 3 + TypeScript | 3.4.x |
| **UI组件** | Element Plus | 2.5.x |
| **构建工具** | Vite | 5.x |
| **状态管理** | Pinia | 2.x |
| **HTTP客户端** | Axios | 1.6.x |

### 2.2 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                     前端 (Vue 3 + TypeScript)                │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐           │
│  │ 工作台  │ │审批中心 │ │ 考勤   │ │ 设置   │           │
│  └─────────┘ └─────────┘ └─────────┘ └─────────┘           │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTP / WebSocket
┌─────────────────────────▼───────────────────────────────────┐
│                    Nginx (反向代理/静态资源)                  │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                   后端 (Spring Boot 3)                       │
│  ┌────────────────────────────────────────────────────────┐ │
│  │                    Controller 层                        │ │
│  │  AuthController | UserController | WorkflowController  │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │                    Service 层                           │ │
│  │  用户服务 | 审批服务 | 流程引擎 | 考勤服务 | 文件服务   │ │
│  └────────────────────────────────────────────────────────┘ │
│  ┌────────────────────────────────────────────────────────┐ │
│  │                    Mapper 层 (MyBatis Plus)             │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────┬───────────────────────────────────┘
                          │
        ┌─────────────────┼─────────────────┐
        ▼                 ▼                 ▼
   ┌─────────┐       ┌─────────┐       ┌─────────┐
   │ MySQL   │       │  Redis  │       │  MinIO  │
   │ 主数据库 │       │ 缓存/会话│       │ 文件存储 │
   └─────────┘       └─────────┘       └─────────┘
```

### 2.3 项目结构

#### 后端结构

```
oa-system/
├── pom.xml
└── src/main/java/com/oasystem/
    ├── OaSystemApplication.java          # 启动类
    ├── common/                           # 公共模块
    │   ├── Result.java                   # 统一响应
    │   ├── PageResult.java               # 分页响应
    │   ├── BaseEntity.java               # 基础实体
    │   ├── constant/                     # 常量定义
    │   ├── enums/                        # 枚举定义
    │   ├── exception/                    # 异常处理
    │   └── utils/                        # 工具类
    ├── config/                           # 配置类
    │   ├── SecurityConfig.java
    │   ├── MyBatisPlusConfig.java
    │   ├── RedisConfig.java
    │   ├── FlowableConfig.java
    │   └── CorsConfig.java
    ├── security/                         # 安全模块
    │   ├── JwtUtils.java
    │   ├── JwtFilter.java
    │   └── LoginUser.java
    └── modules/                          # 业务模块
        ├── system/                       # 系统管理
        │   ├── entity/
        │   ├── mapper/
        │   ├── service/
        │   ├── controller/
        │   └── dto/
        ├── org/                          # 组织架构
        ├── workflow/                     # 工作流引擎
        ├── approval/                     # 审批业务
        ├── attendance/                   # 考勤管理
        ├── schedule/                     # 日程管理
        ├── task/                         # 任务协作
        ├── worklog/                      # 工作日志
        ├── notice/                       # 公告通知
        ├── meeting/                      # 会议管理
        ├── document/                     # 文档中心
        └── message/                      # 消息中心
```

#### 前端结构

```
oa-frontend/
├── package.json
├── vite.config.ts
├── tsconfig.json
└── src/
    ├── main.ts
    ├── App.vue
    ├── api/                              # API接口
    │   ├── system/
    │   ├── org/
    │   ├── workflow/
    │   └── ...
    ├── assets/                           # 静态资源
    ├── components/                       # 公共组件
    │   ├── IconSelect/
    │   ├── RichEditor/
    │   ├── FileUpload/
    │   └── FlowDesigner/
    ├── layouts/                          # 布局组件
    │   ├── MainLayout.vue
    │   └── components/
    ├── router/                           # 路由配置
    │   └── index.ts
    ├── stores/                           # 状态管理
    │   ├── user.ts
    │   ├── app.ts
    │   └── permission.ts
    ├── styles/                           # 全局样式
    ├── utils/                            # 工具函数
    │   ├── request.ts
    │   ├── auth.ts
    │   └── validate.ts
    ├── views/                            # 页面组件
    │   ├── login/
    │   ├── dashboard/
    │   ├── system/
    │   ├── org/
    │   ├── approval/
    │   ├── attendance/
    │   └── ...
    └── types/                            # TypeScript类型
```

---

## 三、角色权限设计

### 3.1 角色定义

| 角色编码 | 角色名称 | 说明 | 数据范围 |
|----------|----------|------|----------|
| `super_admin` | 超级管理员 | 系统最高权限，可管理所有功能和数据 | 全部数据 |
| `company_leader` | 公司高管 | 查看全公司数据，参与重要审批 | 全公司（只读为主） |
| `hr_admin` | HR管理员 | 管理组织架构、员工档案、考勤 | 全公司人事数据 |
| `admin_officer` | 行政管理员 | 管理公告、会议室、文档 | 全公司行政数据 |
| `finance_admin` | 财务管理员 | 财务相关审批 | 财务审批数据 |
| `dept_manager` | 部门经理 | 管理本部门，审批下属申请 | 本部门及下属部门 |
| `employee` | 普通员工 | 日常办公、发起申请 | 仅个人数据 |

### 3.2 各角色完整菜单设计

#### 👤 普通员工 (employee)

```
📊 工作台                    ← 个人仪表盘、待办提醒、快捷入口
📁 我的工作
   ├── 待办事项              ← 需要处理的任务
   ├── 我的申请              ← 请假、报销、出差等申请
   ├── 我的审批记录          ← 查看自己提交的审批进度
   └── 我的任务              ← 分配给我的工作任务
📅 日程管理
   ├── 我的日程              ← 个人日程安排
   └── 会议日程              ← 参与的会议
📝 工作日志
   ├── 写日志                ← 日报/周报/月报
   └── 我的日志              ← 历史日志查看
📋 工作计划
   ├── 我的计划              ← 个人工作计划
   └── 计划总结              ← 计划完成情况
📢 公告通知                  ← 查看公司公告
🕐 考勤管理
   ├── 打卡签到              ← 上下班打卡
   ├── 我的考勤              ← 考勤记录查询
   └── 加班申请              ← 申请加班
📂 文档中心
   ├── 公共文档              ← 公司共享文档
   ├── 部门文档              ← 部门共享文档
   └── 我的文档              ← 个人文档
👤 个人中心
   ├── 个人信息              ← 查看/修改个人资料
   ├── 修改密码              ← 密码管理
   └── 消息通知              ← 系统消息
```

#### 👨‍💻 部门经理 (dept_manager)

```
【包含普通员工全部菜单】 +

✅ 审批中心                  ← 【新增】
   ├── 待我审批              ← 需要我审批的申请
   ├── 已审批                ← 我已处理的审批
   └── 抄送给我              ← 抄送给我知晓的
👥 团队管理                  ← 【新增】
   ├── 部门成员              ← 查看部门员工
   ├── 任务分配              ← 给下属分配任务
   ├── 团队日志              ← 查看下属日志
   └── 团队考勤              ← 查看部门考勤统计
```

#### 👥 HR管理员 (hr_admin)

```
📊 工作台
📁 我的工作
✅ 审批中心
👥 人事管理                  ← 【核心模块】
   ├── 员工档案              ← 全员档案管理
   ├── 入职管理              ← 新员工入职办理
   ├── 离职管理              ← 员工离职办理
   ├── 转正管理              ← 试用期转正
   ├── 调岗调薪              ← 岗位/薪资调整
   └── 合同管理              ← 劳动合同管理
🏢 组织架构
   ├── 部门管理              ← 部门增删改
   ├── 岗位管理              ← 岗位设置
   └── 组织图                ← 可视化组织架构
🕐 考勤管理                  ← 【完整权限】
   ├── 考勤规则              ← 设置考勤规则
   ├── 考勤统计              ← 全员考勤统计
   ├── 异常处理              ← 考勤异常审批
   ├── 加班统计              ← 加班数据汇总
   └── 考勤报表              ← 导出考勤报表
📅 日程管理
📝 工作日志                  ← 可查看全员
📋 工作计划
📢 公告通知
   ├── 查看公告
   └── 发布公告              ← 【可发布人事公告】
📂 文档中心
👤 个人中心
```

#### 📋 行政管理员 (admin_officer)

```
📊 工作台
📁 我的工作
✅ 审批中心
🏢 行政管理                  ← 【核心模块】
   ├── 办公用品              ← 物资申领管理
   ├── 资产管理              ← 固定资产管理
   ├── 车辆管理              ← 公司车辆调度
   ├── 会议室管理            ← 会议室预约
   └── 访客管理              ← 来访登记
📅 会议管理
   ├── 会议室预约
   ├── 会议安排              ← 发起会议
   └── 会议纪要              ← 会议记录
📢 公告通知
   ├── 查看公告
   └── 发布公告              ← 【可发布行政公告】
📂 文档中心                  ← 可管理公共文档
📅 日程管理
📝 工作日志
📋 工作计划
🕐 考勤管理                  ← 仅个人
👤 个人中心
```

#### 💰 财务管理员 (finance_admin)

```
📊 工作台
📁 我的工作
✅ 审批中心
💰 财务管理                  ← 【核心模块】
   ├── 报销管理              ← 报销单审核
   ├── 借款管理              ← 借款审批/核销
   ├── 费用统计              ← 部门费用统计
   ├── 预算管理              ← 部门预算控制
   └── 财务报表              ← 各类财务报表
📅 日程管理
📝 工作日志
📋 工作计划
📢 公告通知
🕐 考勤管理                  ← 仅个人
📂 文档中心
👤 个人中心
```

#### 👔 公司高管 (company_leader)

```
📊 工作台                    ← 【管理驾驶舱】
   ├── 公司概览              ← 关键指标展示
   ├── 审批统计              ← 各类审批数据
   └── 人员概况              ← 员工数据统计
📁 我的工作
✅ 审批中心                  ← 【最终审批权】
   ├── 待我审批              ← 重要事项审批
   ├── 已审批
   └── 审批设置              ← 委托代理人
📊 数据报表                  ← 【管理视图】
   ├── 人事报表
   ├── 考勤报表
   ├── 财务报表
   └── 工作报表
👥 团队管理                  ← 查看全公司
📅 日程管理
📝 工作日志                  ← 查看全员
📋 工作计划                  ← 查看全员
📢 公告通知
   └── 发布公告              ← 【可发布全员公告】
📂 文档中心
👤 个人中心
```

#### 👑 超级管理员 (super_admin)

```
【拥有以上所有角色的全部菜单】 +

⚙️ 系统管理                  ← 【独有】
   ├── 用户管理              ← 账号管理
   ├── 角色管理              ← 角色权限配置
   ├── 菜单管理              ← 系统菜单配置（开发用）
   ├── 权限管理              ← 功能权限分配
   ├── 操作日志              ← 系统操作记录
   ├── 登录日志              ← 用户登录记录
   └── 系统配置              ← 系统参数设置
🔧 流程管理                  ← 【独有】
   ├── 流程设计              ← 可视化流程设计器
   ├── 流程部署              ← 流程发布管理
   └── 流程监控              ← 运行中流程监控
```

### 3.3 权限矩阵总览

| 菜单模块 | 超管 | 高管 | HR | 行政 | 财务 | 经理 | 员工 |
|---------|:---:|:---:|:--:|:---:|:---:|:---:|:---:|
| 工作台 | ✅ | ✅★ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 我的工作 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 审批中心 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| 系统管理 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| 流程管理 | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| 人事管理 | ✅ | 👁️ | ✅ | ❌ | ❌ | ❌ | ❌ |
| 组织架构 | ✅ | 👁️ | ✅ | ❌ | ❌ | ❌ | ❌ |
| 行政管理 | ✅ | 👁️ | ❌ | ✅ | ❌ | ❌ | ❌ |
| 财务管理 | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ | ❌ |
| 数据报表 | ✅ | ✅ | 👁️ | ❌ | 👁️ | ❌ | ❌ |
| 团队管理 | ✅ | ✅全 | ✅全 | ❌ | ❌ | ✅部 | ❌ |
| 考勤管理 | ✅ | 👁️ | ✅ | 🔒 | 🔒 | 👁️部 | 🔒 |
| 日程管理 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 工作日志 | ✅ | 👁️全 | 👁️全 | ✅ | ✅ | 👁️部 | ✅ |
| 工作计划 | ✅ | 👁️全 | ✅ | ✅ | ✅ | 👁️部 | ✅ |
| 会议管理 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 公告通知 | ✅📢 | ✅📢 | ✅📢 | ✅📢 | ✅ | ✅ | ✅ |
| 文档中心 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| 个人中心 | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

#### 图例

| 符号 | 含义 |
|:----:|------|
| ✅ | 完全权限（增删改查） |
| ✅★ | 增强版（如管理驾驶舱） |
| 👁️ | 只读权限 |
| 👁️全 | 全员数据只读 |
| 👁️部 | 部门数据只读 |
| ✅全 | 全员数据管理 |
| ✅部 | 部门数据管理 |
| 🔒 | 仅个人数据 |
| 📢 | 可发布公告 |
| ❌ | 无权限 |

### 3.4 审批流程角色

| 节点角色 | 说明 | 确定方式 |
|----------|------|----------|
| 直属领导 | 员工的直接上级 | 员工所属部门的负责人 |
| 部门经理 | 部门最高负责人 | 部门表中的manager_id |
| 分管领导 | 分管该部门的高管 | 部门表中的leader_id |
| HR | 人事审批节点 | 角色为hr_admin的用户 |
| 财务 | 财务审批节点 | 角色为finance_admin的用户 |
| 行政 | 行政审批节点 | 角色为admin_officer的用户 |
| 总经理 | 最终审批节点 | 指定用户或角色为company_leader |

---

## 四、功能模块清单

### 4.1 模块总览

```
OA办公系统 (18个模块，~300个功能点)
│
├── 基础平台 (4个模块)
│   ├── 1. 系统管理      [超管独有]
│   ├── 2. 组织架构      [HR核心]
│   ├── 3. 消息中心      [全员]
│   └── 4. 个人中心      [全员]
│
├── 协同办公 (7个模块)
│   ├── 5. 工作台        [全员，高管增强版]
│   ├── 6. 我的工作      [全员]
│   ├── 7. 审批中心      [除员工外] ⭐核心
│   ├── 8. 流程管理      [超管独有] ⭐核心
│   ├── 9. 任务协作      [全员]
│   ├── 10. 日程管理     [全员]
│   └── 11. 工作日志     [全员]
│
├── 业务管理 (4个模块)
│   ├── 12. 人事管理     [HR核心]
│   ├── 13. 行政管理     [行政核心]
│   ├── 14. 财务管理     [财务核心]
│   └── 15. 团队管理     [经理/高管/HR]
│
└── 行政办公 (3个模块)
    ├── 16. 考勤管理     [全员基础，HR完整]
    ├── 17. 公告通知     [全员查看，部分可发布]
    └── 18. 文档中心     [全员]
```

### 4.2 详细功能清单

#### 模块1：系统管理 【P0】

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| SYS-001 | 用户管理 | 用户列表、新增、编辑、删除 | P0 |
| SYS-002 | 用户管理 | 用户状态（启用/禁用） | P0 |
| SYS-003 | 用户管理 | 密码重置 | P0 |
| SYS-004 | 用户管理 | 用户导入/导出 | P1 |
| SYS-005 | 角色管理 | 角色列表、CRUD | P0 |
| SYS-006 | 角色管理 | 角色权限分配 | P0 |
| SYS-007 | 角色管理 | 数据权限配置 | P1 |
| SYS-008 | 菜单管理 | 菜单树CRUD | P0 |
| SYS-009 | 菜单管理 | 按钮权限配置 | P0 |
| SYS-010 | 字典管理 | 字典类型CRUD | P0 |
| SYS-011 | 字典管理 | 字典数据CRUD | P0 |
| SYS-012 | 参数配置 | 系统参数管理 | P1 |
| SYS-013 | 日志管理 | 操作日志查询 | P1 |
| SYS-014 | 日志管理 | 登录日志查询 | P1 |
| SYS-015 | 在线用户 | 在线用户列表 | P1 |
| SYS-016 | 在线用户 | 强制下线 | P1 |
| SYS-017 | 定时任务 | 任务配置管理 | P2 |
| SYS-018 | 定时任务 | 执行日志 | P2 |

#### 模块2：组织架构 【P0】

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| ORG-001 | 公司管理 | 公司信息维护 | P0 |
| ORG-002 | 部门管理 | 部门树CRUD | P0 |
| ORG-003 | 部门管理 | 部门负责人设置 | P0 |
| ORG-004 | 部门管理 | 部门排序 | P1 |
| ORG-005 | 岗位管理 | 岗位CRUD | P0 |
| ORG-006 | 岗位管理 | 岗位职级设置 | P1 |
| ORG-007 | 员工档案 | 基本信息维护 | P0 |
| ORG-008 | 员工档案 | 联系方式 | P0 |
| ORG-009 | 员工档案 | 工作信息 | P0 |
| ORG-010 | 员工档案 | 合同信息 | P1 |
| ORG-011 | 员工档案 | 学历信息 | P1 |
| ORG-012 | 员工档案 | 附件管理 | P1 |
| ORG-013 | 入职管理 | 入职办理 | P1 |
| ORG-014 | 入职管理 | 试用期管理 | P1 |
| ORG-015 | 离职管理 | 离职申请 | P1 |
| ORG-016 | 离职管理 | 工作交接 | P2 |
| ORG-017 | 调动管理 | 岗位调动 | P2 |
| ORG-018 | 组织架构图 | 可视化展示 | P1 |

#### 模块3：消息中心 【P1】

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| MSG-001 | 站内消息 | 消息列表 | P1 |
| MSG-002 | 站内消息 | 已读/未读状态 | P1 |
| MSG-003 | 站内消息 | 批量操作 | P1 |
| MSG-004 | 消息推送 | 系统通知推送 | P1 |
| MSG-005 | 消息推送 | 审批提醒 | P0 |
| MSG-006 | 消息推送 | 日程提醒 | P1 |
| MSG-007 | 消息模板 | 模板管理 | P2 |

#### 模块4：个人中心 【P1】

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| USR-001 | 个人信息 | 基本信息查看/修改 | P1 |
| USR-002 | 个人信息 | 头像上传 | P1 |
| USR-003 | 账号安全 | 修改密码 | P0 |
| USR-004 | 账号安全 | 绑定手机 | P2 |
| USR-005 | 偏好设置 | 主题设置 | P2 |
| USR-006 | 偏好设置 | 通知设置 | P2 |
| USR-007 | 我的收藏 | 收藏列表 | P2 |

#### 模块5：工作台 【P0】

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| WKB-001 | 数据概览 | 待办/日程/任务统计卡片 | P0 |
| WKB-002 | 待办事项 | 待我审批列表 | P0 |
| WKB-003 | 待办事项 | 我发起的列表 | P0 |
| WKB-004 | 待办事项 | 抄送我的列表 | P1 |
| WKB-005 | 快捷入口 | 常用功能快捷方式 | P1 |
| WKB-006 | 日程预览 | 今日/本周日程 | P1 |
| WKB-007 | 公告速览 | 最新公告展示 | P1 |
| WKB-008 | 考勤状态 | 今日打卡状态 | P1 |
| WKB-009 | 考勤状态 | 快捷打卡按钮 | P1 |

#### 模块6：审批流程 【P0】⭐核心

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| WF-001 | 流程设计器 | 可视化流程设计 | P0 |
| WF-002 | 流程设计器 | 审批节点配置 | P0 |
| WF-003 | 流程设计器 | 条件分支配置 | P0 |
| WF-004 | 流程设计器 | 流程发布/停用 | P0 |
| WF-005 | 表单设计器 | 可视化表单设计 | P0 |
| WF-006 | 表单设计器 | 字段类型配置 | P0 |
| WF-007 | 表单设计器 | 表单校验规则 | P1 |
| APR-001 | 请假申请 | 请假类型选择 | P0 |
| APR-002 | 请假申请 | 假期余额查询 | P0 |
| APR-003 | 请假申请 | 请假时长计算 | P0 |
| APR-004 | 请假申请 | 销假操作 | P1 |
| APR-005 | 报销申请 | 报销类型选择 | P0 |
| APR-006 | 报销申请 | 费用明细录入 | P0 |
| APR-007 | 报销申请 | 发票附件上传 | P0 |
| APR-008 | 出差申请 | 行程信息填写 | P1 |
| APR-009 | 出差申请 | 出差预算 | P1 |
| APR-010 | 加班申请 | 加班类型/时长 | P1 |
| APR-011 | 加班申请 | 调休转换 | P2 |
| APR-012 | 外出申请 | 外出登记 | P1 |
| APR-013 | 补卡申请 | 补卡信息填写 | P1 |
| APR-014 | 物品领用 | 领用申请 | P2 |
| APR-015 | 用印申请 | 用印申请 | P2 |
| APR-016 | 采购申请 | 采购申请 | P2 |
| APR-017 | 付款申请 | 付款申请 | P2 |
| APR-018 | 通用审批 | 自定义审批 | P1 |
| WF-010 | 审批操作 | 通过 | P0 |
| WF-011 | 审批操作 | 驳回 | P0 |
| WF-012 | 审批操作 | 退回修改 | P1 |
| WF-013 | 审批操作 | 转办 | P1 |
| WF-014 | 审批操作 | 加签 | P2 |
| WF-015 | 审批操作 | 催办 | P1 |
| WF-016 | 审批操作 | 撤回 | P0 |
| WF-017 | 审批委托 | 设置代理人 | P2 |
| WF-018 | 流程监控 | 流程跟踪 | P0 |
| WF-019 | 流程监控 | 审批记录查看 | P0 |

#### 模块7：任务协作 【P2】

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| TSK-001 | 任务管理 | 创建任务 | P2 |
| TSK-002 | 任务管理 | 任务分配 | P2 |
| TSK-003 | 任务管理 | 优先级设置 | P2 |
| TSK-004 | 任务管理 | 截止日期 | P2 |
| TSK-005 | 任务视图 | 列表视图 | P2 |
| TSK-006 | 任务视图 | 看板视图 | P2 |
| TSK-007 | 子任务 | 任务拆分 | P2 |
| TSK-008 | 任务状态 | 状态流转 | P2 |
| TSK-009 | 任务评论 | 评论交流 | P2 |
| TSK-010 | 任务提醒 | 到期提醒 | P2 |

#### 模块8：日程管理 【P1】

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| SCH-001 | 日程创建 | 新建日程 | P1 |
| SCH-002 | 日程创建 | 重复日程 | P2 |
| SCH-003 | 日程分类 | 分类管理 | P1 |
| SCH-004 | 日程视图 | 日/周/月视图 | P1 |
| SCH-005 | 日程提醒 | 提醒设置 | P1 |
| SCH-006 | 日程共享 | 共享设置 | P2 |
| SCH-007 | 日程邀请 | 邀请参与 | P2 |

#### 模块9：工作日志 【P2】

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| LOG-001 | 日报 | 撰写日报 | P2 |
| LOG-002 | 日报 | 日报模板 | P2 |
| LOG-003 | 周报 | 撰写周报 | P2 |
| LOG-004 | 月报 | 撰写月报 | P2 |
| LOG-005 | 日志查阅 | 我的日志 | P2 |
| LOG-006 | 日志查阅 | 下属日志 | P2 |
| LOG-007 | 日志评论 | 评论点赞 | P2 |

#### 模块10：工作计划 【P2】

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| PLN-001 | 个人计划 | 周/月计划 | P2 |
| PLN-002 | 部门计划 | 部门计划 | P2 |
| PLN-003 | 计划跟踪 | 进度更新 | P2 |
| PLN-004 | 计划审批 | 计划提交审批 | P2 |

#### 模块11：公告通知 【P1】

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| NTC-001 | 公告发布 | 新建公告 | P1 |
| NTC-002 | 公告发布 | 富文本编辑 | P1 |
| NTC-003 | 公告发布 | 附件上传 | P1 |
| NTC-004 | 公告发布 | 定时发布 | P2 |
| NTC-005 | 公告分类 | 分类管理 | P1 |
| NTC-006 | 公告管理 | 置顶/撤回/删除 | P1 |
| NTC-007 | 阅读管理 | 阅读确认 | P1 |
| NTC-008 | 阅读管理 | 已读统计 | P1 |

#### 模块12：考勤管理 【P1】

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| ATD-001 | 考勤规则 | 班次设置 | P1 |
| ATD-002 | 考勤规则 | 弹性时间 | P1 |
| ATD-003 | 考勤规则 | 迟到规则 | P1 |
| ATD-004 | 打卡签到 | 上班打卡 | P1 |
| ATD-005 | 打卡签到 | 下班打卡 | P1 |
| ATD-006 | 打卡签到 | 外勤打卡 | P2 |
| ATD-007 | 考勤记录 | 个人记录查询 | P1 |
| ATD-008 | 考勤记录 | 部门记录查询 | P1 |
| ATD-009 | 考勤记录 | 异常记录 | P1 |
| ATD-010 | 考勤统计 | 日/月统计 | P1 |
| ATD-011 | 考勤报表 | 月度报表 | P1 |
| ATD-012 | 考勤报表 | 导出Excel | P1 |
| ATD-013 | 假期管理 | 假期类型配置 | P1 |
| ATD-014 | 假期管理 | 假期额度管理 | P1 |
| ATD-015 | 排班管理 | 排班表 | P2 |

#### 模块13：会议管理 【P2】

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| MTG-001 | 会议室管理 | 会议室CRUD | P2 |
| MTG-002 | 会议室管理 | 设备配置 | P2 |
| MTG-003 | 会议预约 | 预约会议室 | P2 |
| MTG-004 | 会议预约 | 冲突检测 | P2 |
| MTG-005 | 会议创建 | 新建会议 | P2 |
| MTG-006 | 会议通知 | 发送邀请 | P2 |
| MTG-007 | 会议签到 | 参会签到 | P2 |
| MTG-008 | 会议纪要 | 纪要撰写 | P2 |

#### 模块14：文档中心 【P2】

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| DOC-001 | 文件管理 | 文件上传 | P2 |
| DOC-002 | 文件管理 | 文件夹管理 | P2 |
| DOC-003 | 文件管理 | 移动/复制/删除 | P2 |
| DOC-004 | 文件预览 | 在线预览 | P2 |
| DOC-005 | 文件分享 | 生成分享链接 | P2 |
| DOC-006 | 权限控制 | 文件夹权限 | P2 |
| DOC-007 | 版本管理 | 历史版本 | P2 |
| DOC-008 | 文件搜索 | 全文搜索 | P2 |
| DOC-009 | 个人网盘 | 个人空间 | P2 |

#### 模块15：人事管理 【P1】(HR专用)

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| HR-001 | 员工档案 | 全员档案列表 | P0 |
| HR-002 | 员工档案 | 档案详情查看 | P0 |
| HR-003 | 员工档案 | 档案编辑 | P0 |
| HR-004 | 员工档案 | 初始密码查看 | P0 |
| HR-005 | 入职管理 | 入职登记 | P1 |
| HR-006 | 入职管理 | 入职审批 | P1 |
| HR-007 | 入职管理 | 入职引导 | P2 |
| HR-008 | 离职管理 | 离职申请 | P1 |
| HR-009 | 离职管理 | 离职交接 | P1 |
| HR-010 | 离职管理 | 离职归档 | P1 |
| HR-011 | 转正管理 | 转正申请 | P1 |
| HR-012 | 转正管理 | 转正审批 | P1 |
| HR-013 | 调岗调薪 | 调岗申请 | P2 |
| HR-014 | 调岗调薪 | 调薪记录 | P2 |
| HR-015 | 合同管理 | 合同列表 | P2 |
| HR-016 | 合同管理 | 合同续签提醒 | P2 |

#### 模块16：行政管理 【P2】(行政专用)

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| ADM-001 | 办公用品 | 用品库存 | P2 |
| ADM-002 | 办公用品 | 领用申请 | P2 |
| ADM-003 | 办公用品 | 领用记录 | P2 |
| ADM-004 | 资产管理 | 资产列表 | P2 |
| ADM-005 | 资产管理 | 资产入库 | P2 |
| ADM-006 | 资产管理 | 资产领用 | P2 |
| ADM-007 | 资产管理 | 资产报废 | P2 |
| ADM-008 | 车辆管理 | 车辆列表 | P2 |
| ADM-009 | 车辆管理 | 用车申请 | P2 |
| ADM-010 | 车辆管理 | 用车调度 | P2 |
| ADM-011 | 访客管理 | 来访登记 | P2 |
| ADM-012 | 访客管理 | 访客记录 | P2 |

#### 模块17：财务管理 【P1】(财务专用)

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| FIN-001 | 报销管理 | 报销单列表 | P1 |
| FIN-002 | 报销管理 | 报销审核 | P1 |
| FIN-003 | 报销管理 | 报销统计 | P1 |
| FIN-004 | 借款管理 | 借款申请列表 | P2 |
| FIN-005 | 借款管理 | 借款审批 | P2 |
| FIN-006 | 借款管理 | 借款核销 | P2 |
| FIN-007 | 费用统计 | 部门费用 | P1 |
| FIN-008 | 费用统计 | 费用趋势 | P2 |
| FIN-009 | 预算管理 | 预算设置 | P2 |
| FIN-010 | 预算管理 | 预算执行 | P2 |
| FIN-011 | 财务报表 | 报销汇总表 | P1 |
| FIN-012 | 财务报表 | 费用明细表 | P1 |

#### 模块18：团队管理 【P1】(经理/高管/HR)

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| TM-001 | 部门成员 | 成员列表 | P1 |
| TM-002 | 部门成员 | 成员详情 | P1 |
| TM-003 | 任务分配 | 任务创建 | P1 |
| TM-004 | 任务分配 | 任务跟踪 | P1 |
| TM-005 | 团队日志 | 下属日志查看 | P1 |
| TM-006 | 团队日志 | 日志评论 | P2 |
| TM-007 | 团队考勤 | 考勤概览 | P1 |
| TM-008 | 团队考勤 | 异常记录 | P1 |
| TM-009 | 团队计划 | 计划查看 | P2 |
| TM-010 | 团队计划 | 计划审批 | P2 |

#### 模块19：数据报表 【P2】(高管专用)

| 功能编号 | 功能名称 | 子功能 | 优先级 |
|----------|----------|--------|--------|
| RPT-001 | 人事报表 | 人员结构分析 | P2 |
| RPT-002 | 人事报表 | 入离职统计 | P2 |
| RPT-003 | 考勤报表 | 全员考勤统计 | P2 |
| RPT-004 | 考勤报表 | 部门考勤对比 | P2 |
| RPT-005 | 财务报表 | 费用汇总 | P2 |
| RPT-006 | 财务报表 | 预算执行率 | P2 |
| RPT-007 | 工作报表 | 审批效率统计 | P2 |
| RPT-008 | 工作报表 | 任务完成率 | P2 |

---

## 五、数据库设计

### 5.1 数据库规范

- 数据库：MySQL 8.0
- 字符集：utf8mb4
- 排序规则：utf8mb4_general_ci
- 存储引擎：InnoDB
- 表名前缀：按模块划分（sys_、org_、wf_、biz_、oa_）

### 5.2 核心表设计

#### 系统管理表

```sql
-- 用户表
sys_user (
    id, username, password, real_name, avatar, phone, email,
    dept_id, position_id, status, create_time, update_time
)

-- 角色表
sys_role (
    id, role_name, role_code, sort, status, remark,
    data_scope, create_time
)

-- 用户角色关联
sys_user_role (id, user_id, role_id)

-- 菜单表
sys_menu (
    id, parent_id, menu_name, menu_type, path, component,
    perms, icon, sort, visible, status
)

-- 角色菜单关联
sys_role_menu (id, role_id, menu_id)

-- 字典类型
sys_dict_type (id, dict_name, dict_type, status, remark)

-- 字典数据
sys_dict_data (id, dict_type, dict_label, dict_value, sort, status)

-- 操作日志
sys_log (id, user_id, operation, method, params, ip, time, create_time)
```

#### 组织架构表

```sql
-- 部门表
org_department (
    id, parent_id, dept_name, dept_code, manager_id, leader_id,
    sort, status, create_time
)

-- 岗位表
org_position (id, position_name, position_code, level, sort, status)

-- 员工档案
org_employee (
    id, user_id, emp_no, real_name, gender, birthday, id_card,
    phone, email, address, dept_id, position_id, hire_date,
    regular_date, contract_start, contract_end, status
)
```

#### 工作流表

```sql
-- 流程定义（Flowable自带 + 扩展）
wf_process_def (
    id, process_key, process_name, category, form_id,
    version, status, create_time
)

-- 表单定义
wf_form_def (id, form_name, form_key, form_json, status, create_time)

-- 流程实例扩展
wf_process_inst_ext (
    id, process_inst_id, title, initiator_id, business_key,
    business_type, status, create_time, end_time
)

-- 审批记录
wf_approval_record (
    id, process_inst_id, task_id, task_name, assignee_id,
    action, comment, create_time
)
```

#### 业务申请表

```sql
-- 请假申请
biz_leave (
    id, process_inst_id, user_id, leave_type, start_time, end_time,
    duration, reason, status, create_time
)

-- 报销申请
biz_expense (
    id, process_inst_id, user_id, expense_type, total_amount,
    remark, status, create_time
)

-- 报销明细
biz_expense_item (
    id, expense_id, item_type, amount, occur_date, remark
)

-- 出差申请
biz_business_trip (
    id, process_inst_id, user_id, destination, start_date, end_date,
    purpose, budget, status, create_time
)

-- 加班申请
biz_overtime (
    id, process_inst_id, user_id, overtime_type, start_time, end_time,
    duration, reason, status, create_time
)
```

#### 日常办公表

```sql
-- 日程
oa_schedule (
    id, user_id, title, content, start_time, end_time, all_day,
    remind_time, category, color, repeat_type, status
)

-- 公告
oa_notice (
    id, title, content, notice_type, publish_scope, top,
    publish_time, creator_id, status, create_time
)

-- 公告阅读记录
oa_notice_read (id, notice_id, user_id, read_time)

-- 考勤记录
oa_attendance (
    id, user_id, attendance_date, check_in_time, check_out_time,
    check_in_status, check_out_status, work_hours
)

-- 会议室
oa_meeting_room (id, room_name, location, capacity, facilities, status)

-- 会议
oa_meeting (
    id, room_id, title, content, start_time, end_time,
    organizer_id, status, create_time
)

-- 任务
oa_task (
    id, title, content, assignee_id, creator_id, priority,
    due_date, status, parent_id, create_time
)

-- 工作日志
oa_work_log (
    id, user_id, log_type, log_date, content, submit_to,
    status, create_time
)

-- 文档
oa_document (
    id, parent_id, doc_name, doc_type, file_path, file_size,
    creator_id, create_time
)

-- 站内消息
oa_message (
    id, sender_id, receiver_id, title, content, msg_type,
    is_read, create_time
)
```

### 5.3 ER 图（核心关系）

```
sys_user ──1:N── sys_user_role ──N:1── sys_role
    │                                      │
    │ 1:1                                  │ N:M
    ▼                                      ▼
org_employee                          sys_role_menu ──N:1── sys_menu
    │
    │ N:1
    ▼
org_department ◄── org_position

sys_user ──1:N── wf_process_inst_ext ──1:N── wf_approval_record
    │
    │ 1:N
    ├──── biz_leave
    ├──── biz_expense ──1:N── biz_expense_item
    ├──── biz_overtime
    └──── ...

sys_user ──1:N── oa_attendance
sys_user ──1:N── oa_schedule
sys_user ──1:N── oa_task
```

---

## 六、接口设计

### 6.1 接口规范

- RESTful 风格
- 统一响应格式
- JWT 认证
- 接口版本：/api/v1/

#### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

#### 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "current": 1,
    "size": 10,
    "total": 100,
    "pages": 10
  }
}
```

### 6.2 核心接口清单

#### 认证接口

```
POST   /api/v1/auth/login              登录
POST   /api/v1/auth/logout             登出
GET    /api/v1/auth/me                 当前用户信息
PUT    /api/v1/auth/password           修改密码
POST   /api/v1/auth/refresh            刷新Token
```

#### 系统管理接口

```
# 用户管理
GET    /api/v1/system/users            用户列表
POST   /api/v1/system/users            新增用户
GET    /api/v1/system/users/{id}       用户详情
PUT    /api/v1/system/users/{id}       更新用户
DELETE /api/v1/system/users/{id}       删除用户
PUT    /api/v1/system/users/{id}/status  用户状态
POST   /api/v1/system/users/{id}/reset-password  重置密码
POST   /api/v1/system/users/import     导入用户
GET    /api/v1/system/users/export     导出用户

# 角色管理
GET    /api/v1/system/roles            角色列表
POST   /api/v1/system/roles            新增角色
PUT    /api/v1/system/roles/{id}       更新角色
DELETE /api/v1/system/roles/{id}       删除角色
PUT    /api/v1/system/roles/{id}/menus 分配菜单权限

# 菜单管理
GET    /api/v1/system/menus            菜单树
POST   /api/v1/system/menus            新增菜单
PUT    /api/v1/system/menus/{id}       更新菜单
DELETE /api/v1/system/menus/{id}       删除菜单

# 字典管理
GET    /api/v1/system/dicts/types      字典类型列表
POST   /api/v1/system/dicts/types      新增字典类型
GET    /api/v1/system/dicts/data/{type}  字典数据
```

#### 组织架构接口

```
# 部门管理
GET    /api/v1/org/departments         部门树
POST   /api/v1/org/departments         新增部门
PUT    /api/v1/org/departments/{id}    更新部门
DELETE /api/v1/org/departments/{id}    删除部门

# 岗位管理
GET    /api/v1/org/positions           岗位列表
POST   /api/v1/org/positions           新增岗位
PUT    /api/v1/org/positions/{id}      更新岗位
DELETE /api/v1/org/positions/{id}      删除岗位

# 员工档案
GET    /api/v1/org/employees           员工列表
POST   /api/v1/org/employees           新增员工
GET    /api/v1/org/employees/{id}      员工详情
PUT    /api/v1/org/employees/{id}      更新员工
```

#### 审批流程接口

```
# 流程定义
GET    /api/v1/workflow/definitions    流程定义列表
POST   /api/v1/workflow/definitions    创建流程
PUT    /api/v1/workflow/definitions/{id}  更新流程
POST   /api/v1/workflow/definitions/{id}/deploy  部署流程

# 表单定义
GET    /api/v1/workflow/forms          表单列表
POST   /api/v1/workflow/forms          创建表单
PUT    /api/v1/workflow/forms/{id}     更新表单

# 审批申请
POST   /api/v1/approval/leave          发起请假
POST   /api/v1/approval/expense        发起报销
POST   /api/v1/approval/overtime       发起加班
POST   /api/v1/approval/business-trip  发起出差
GET    /api/v1/approval/{type}/{id}    申请详情

# 审批操作
GET    /api/v1/workflow/tasks/todo     待办列表
GET    /api/v1/workflow/tasks/done     已办列表
GET    /api/v1/workflow/tasks/apply    我发起的
POST   /api/v1/workflow/tasks/{id}/approve  通过
POST   /api/v1/workflow/tasks/{id}/reject   驳回
POST   /api/v1/workflow/tasks/{id}/return   退回
POST   /api/v1/workflow/tasks/{id}/transfer 转办
POST   /api/v1/workflow/tasks/{id}/withdraw 撤回
```

#### 考勤接口

```
POST   /api/v1/attendance/check-in     签到打卡
POST   /api/v1/attendance/check-out    签退打卡
GET    /api/v1/attendance/today        今日打卡状态
GET    /api/v1/attendance/records      考勤记录列表
GET    /api/v1/attendance/statistics   考勤统计
GET    /api/v1/attendance/export       导出考勤报表
```

#### 日程接口

```
GET    /api/v1/schedules               日程列表
POST   /api/v1/schedules               创建日程
PUT    /api/v1/schedules/{id}          更新日程
DELETE /api/v1/schedules/{id}          删除日程
```

#### 公告接口

```
GET    /api/v1/notices                 公告列表
POST   /api/v1/notices                 发布公告
GET    /api/v1/notices/{id}            公告详情
PUT    /api/v1/notices/{id}            更新公告
DELETE /api/v1/notices/{id}            删除公告
POST   /api/v1/notices/{id}/read       标记已读
```

---

## 七、开发阶段规划

### 7.1 总体计划

| 阶段 | 周期 | 主要内容 | 优先级 |
|------|------|----------|--------|
| 阶段一 | 第1-2周 | 项目搭建 + 系统管理 + 组织架构 | P0 |
| 阶段二 | 第3-5周 | 审批流程（核心）| P0 |
| 阶段三 | 第6周 | 工作台 + 消息中心 | P0+P1 |
| 阶段四 | 第7周 | 考勤管理 + 日程管理 | P1 |
| 阶段五 | 第8周 | 公告通知 + 个人中心 | P1 |
| 阶段六 | 第9周 | 任务协作 + 工作日志 | P2 |
| 阶段七 | 第10周 | 会议管理 + 文档中心 + 工作计划 | P2 |
| 阶段八 | 第11周 | 测试 + 优化 + 部署 | - |

**总工期：约 11 周**

### 7.2 详细阶段任务

见 [todo.md](./todo.md)

---

## 八、开发规范

### 8.1 代码规范

#### 后端规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化代码
- 统一异常处理
- 参数校验使用 @Valid
- 日志使用 SLF4J

#### 前端规范

- 遵循 Vue 3 组合式 API 风格
- TypeScript 严格模式
- ESLint + Prettier 格式化
- 组件命名 PascalCase
- 文件命名 kebab-case

### 8.2 Git 规范

#### 分支策略

```
main        <- 生产分支
  └── develop    <- 开发分支
        ├── feature/xxx   <- 功能分支
        ├── bugfix/xxx    <- 修复分支
        └── hotfix/xxx    <- 紧急修复
```

#### 提交规范

```
feat: 新功能
fix: 修复bug
docs: 文档更新
style: 代码格式
refactor: 重构
test: 测试
chore: 构建/工具
```

### 8.3 接口规范

- RESTful 风格
- 统一响应格式
- 接口版本控制
- Swagger 文档注释

---

## 附录

### A. 环境要求

- JDK 21+
- Node.js 20+
- MySQL 8.0+
- Redis 7.0+
- Maven 3.9+

### B. 快速启动

```bash
# 1. 克隆项目
git clone <repo-url>

# 2. 初始化数据库
mysql -u root -p < sql/init.sql

# 3. 启动后端
cd backend
./mvnw spring-boot:run

# 4. 启动前端
cd frontend
npm install
npm run dev
```

### C. 相关文档

- [开发任务清单](./todo.md)
- [数据库设计](./database.md)
- [接口文档](./api.md)
