/**
 * 菜单配置文件
 * 定义所有菜单及其权限
 */

export interface MenuItem {
  id: string
  title: string
  icon?: string
  path?: string
  roles: string[]  // 哪些角色可以看到此菜单
  children?: MenuItem[]
  badge?: number | string  // 角标
  hidden?: boolean  // 是否隐藏
}

// 所有角色编码
export const ROLES = {
  SUPER_ADMIN: 'super_admin',
  COMPANY_LEADER: 'company_leader',
  HR_ADMIN: 'hr_admin',
  ADMIN_OFFICER: 'admin_officer',
  FINANCE_ADMIN: 'finance_admin',
  DEPT_MANAGER: 'dept_manager',
  EMPLOYEE: 'employee',
} as const

// 角色组合，方便复用
const ALL_ROLES = Object.values(ROLES)
const MANAGER_AND_ABOVE = [ROLES.SUPER_ADMIN, ROLES.COMPANY_LEADER, ROLES.HR_ADMIN, ROLES.ADMIN_OFFICER, ROLES.FINANCE_ADMIN, ROLES.DEPT_MANAGER]
const ADMIN_ROLES = [ROLES.SUPER_ADMIN, ROLES.HR_ADMIN]
const ORG_ROLES = [ROLES.SUPER_ADMIN, ROLES.HR_ADMIN]
const ADMIN_HR_OFFICER = [ROLES.SUPER_ADMIN, ROLES.HR_ADMIN, ROLES.ADMIN_OFFICER]

/**
 * 完整菜单配置
 */
export const menuConfig: MenuItem[] = [
  // ========== 工作台 ==========
  {
    id: 'dashboard',
    title: '工作台',
    icon: 'dashboard',
    path: '/dashboard',
    roles: ALL_ROLES,
  },

  // ========== 我的工作 ==========
  {
    id: 'my-work',
    title: '我的工作',
    icon: 'work',
    roles: ALL_ROLES,
    children: [
      {
        id: 'my-todo',
        title: '待办事项',
        icon: 'todo',
        path: '/my/todo',
        roles: ALL_ROLES,
      },
      {
        id: 'my-apply',
        title: '我的申请',
        icon: 'apply',
        path: '/my/apply',
        roles: ALL_ROLES,
      },
      {
        id: 'my-approved',
        title: '审批记录',
        icon: 'record',
        path: '/my/approved',
        roles: ALL_ROLES,
      },
      {
        id: 'my-task',
        title: '我的任务',
        icon: 'task',
        path: '/my/task',
        roles: ALL_ROLES,
      },
    ],
  },

  // ========== 审批中心 ==========
  {
    id: 'approve',
    title: '审批中心',
    icon: 'approve',
    roles: MANAGER_AND_ABOVE,
    children: [
      {
        id: 'approve-pending',
        title: '待我审批',
        icon: 'pending',
        path: '/approve/pending',
        roles: MANAGER_AND_ABOVE,
      },
      {
        id: 'approve-done',
        title: '已审批',
        icon: 'done',
        path: '/approve/done',
        roles: MANAGER_AND_ABOVE,
      },
      {
        id: 'approve-cc',
        title: '抄送给我',
        icon: 'cc',
        path: '/approve/cc',
        roles: MANAGER_AND_ABOVE,
      },
    ],
  },

  // ========== 日程管理 ==========
  {
    id: 'schedule',
    title: '日程管理',
    icon: 'calendar',
    roles: ALL_ROLES,
    children: [
      {
        id: 'my-schedule',
        title: '我的日程',
        icon: 'my-calendar',
        path: '/schedule/my',
        roles: ALL_ROLES,
      },
      {
        id: 'meeting-schedule',
        title: '会议日程',
        icon: 'meeting',
        path: '/schedule/meeting',
        roles: ALL_ROLES,
      },
    ],
  },

  // ========== 工作日志 ==========
  {
    id: 'worklog',
    title: '工作日志',
    icon: 'log',
    roles: ALL_ROLES,
    children: [
      {
        id: 'write-log',
        title: '写日志',
        icon: 'write',
        path: '/worklog/write',
        roles: ALL_ROLES,
      },
      {
        id: 'my-log',
        title: '我的日志',
        icon: 'my-log',
        path: '/worklog/my',
        roles: ALL_ROLES,
      },
      {
        id: 'team-log',
        title: '团队日志',
        icon: 'team-log',
        path: '/worklog/team',
        roles: [...MANAGER_AND_ABOVE],
      },
    ],
  },

  // ========== 考勤管理 ==========
  {
    id: 'attendance',
    title: '考勤管理',
    icon: 'attendance',
    roles: ALL_ROLES,
    children: [
      {
        id: 'clock-in',
        title: '打卡签到',
        icon: 'clock',
        path: '/attendance/clock',
        roles: ALL_ROLES,
      },
      {
        id: 'my-attendance',
        title: '我的考勤',
        icon: 'my-attendance',
        path: '/attendance/my',
        roles: ALL_ROLES,
      },
      {
        id: 'team-attendance',
        title: '团队考勤',
        icon: 'team-attendance',
        path: '/attendance/team',
        roles: [ROLES.SUPER_ADMIN, ROLES.HR_ADMIN, ROLES.DEPT_MANAGER],
      },
      {
        id: 'attendance-rule',
        title: '考勤规则',
        icon: 'rule',
        path: '/attendance/rule',
        roles: [ROLES.SUPER_ADMIN, ROLES.HR_ADMIN],
      },
      {
        id: 'attendance-report',
        title: '考勤报表',
        icon: 'report',
        path: '/attendance/report',
        roles: [ROLES.SUPER_ADMIN, ROLES.HR_ADMIN],
      },
    ],
  },

  // ========== 公告通知 ==========
  {
    id: 'notice',
    title: '公告通知',
    icon: 'notice',
    path: '/notice',
    roles: ALL_ROLES,
  },

  // ========== 文档中心 ==========
  {
    id: 'document',
    title: '文档中心',
    icon: 'document',
    roles: ALL_ROLES,
    children: [
      {
        id: 'public-doc',
        title: '公共文档',
        icon: 'public',
        path: '/document/public',
        roles: ALL_ROLES,
      },
      {
        id: 'dept-doc',
        title: '部门文档',
        icon: 'dept',
        path: '/document/dept',
        roles: ALL_ROLES,
      },
      {
        id: 'my-doc',
        title: '我的文档',
        icon: 'my-doc',
        path: '/document/my',
        roles: ALL_ROLES,
      },
    ],
  },

  // ========== 团队管理 ==========
  {
    id: 'team',
    title: '团队管理',
    icon: 'team',
    roles: [ROLES.SUPER_ADMIN, ROLES.COMPANY_LEADER, ROLES.HR_ADMIN, ROLES.DEPT_MANAGER],
    children: [
      {
        id: 'team-member',
        title: '部门成员',
        icon: 'member',
        path: '/team/member',
        roles: [ROLES.SUPER_ADMIN, ROLES.COMPANY_LEADER, ROLES.HR_ADMIN, ROLES.DEPT_MANAGER],
      },
      {
        id: 'task-assign',
        title: '任务分配',
        icon: 'assign',
        path: '/team/task',
        roles: [ROLES.SUPER_ADMIN, ROLES.COMPANY_LEADER, ROLES.HR_ADMIN, ROLES.DEPT_MANAGER],
      },
    ],
  },

  // ========== 人事管理 (HR核心) ==========
  {
    id: 'hr',
    title: '人事管理',
    icon: 'hr',
    roles: [ROLES.SUPER_ADMIN, ROLES.HR_ADMIN],
    children: [
      {
        id: 'employee-archive',
        title: '员工档案',
        icon: 'archive',
        path: '/hr/employee',
        roles: [ROLES.SUPER_ADMIN, ROLES.HR_ADMIN],
      },
      {
        id: 'onboard',
        title: '入职管理',
        icon: 'onboard',
        path: '/hr/onboard',
        roles: [ROLES.SUPER_ADMIN, ROLES.HR_ADMIN],
      },
      {
        id: 'resign',
        title: '离职管理',
        icon: 'resign',
        path: '/hr/resign',
        roles: [ROLES.SUPER_ADMIN, ROLES.HR_ADMIN],
      },
      {
        id: 'regular',
        title: '转正管理',
        icon: 'regular',
        path: '/hr/regular',
        roles: [ROLES.SUPER_ADMIN, ROLES.HR_ADMIN],
      },
    ],
  },

  // ========== 组织架构 ==========
  {
    id: 'org',
    title: '组织架构',
    icon: 'org',
    roles: ORG_ROLES,
    children: [
      {
        id: 'dept',
        title: '部门管理',
        icon: 'dept',
        path: '/org/dept',
        roles: ORG_ROLES,
      },
      {
        id: 'position',
        title: '岗位管理',
        icon: 'position',
        path: '/org/position',
        roles: ORG_ROLES,
      },
      {
        id: 'org-chart',
        title: '组织架构图',
        icon: 'chart',
        path: '/org/chart',
        roles: ORG_ROLES,
      },
    ],
  },

  // ========== 行政管理 ==========
  {
    id: 'admin',
    title: '行政管理',
    icon: 'admin',
    roles: [ROLES.SUPER_ADMIN, ROLES.ADMIN_OFFICER],
    children: [
      {
        id: 'supplies',
        title: '办公用品',
        icon: 'supplies',
        path: '/admin/supplies',
        roles: [ROLES.SUPER_ADMIN, ROLES.ADMIN_OFFICER],
      },
      {
        id: 'assets',
        title: '资产管理',
        icon: 'assets',
        path: '/admin/assets',
        roles: [ROLES.SUPER_ADMIN, ROLES.ADMIN_OFFICER],
      },
      {
        id: 'meeting-room',
        title: '会议室管理',
        icon: 'room',
        path: '/admin/meeting-room',
        roles: [ROLES.SUPER_ADMIN, ROLES.ADMIN_OFFICER],
      },
    ],
  },

  // ========== 财务管理 ==========
  {
    id: 'finance',
    title: '财务管理',
    icon: 'finance',
    roles: [ROLES.SUPER_ADMIN, ROLES.FINANCE_ADMIN],
    children: [
      {
        id: 'expense-manage',
        title: '报销管理',
        icon: 'expense',
        path: '/finance/expense',
        roles: [ROLES.SUPER_ADMIN, ROLES.FINANCE_ADMIN],
      },
      {
        id: 'cost-stat',
        title: '费用统计',
        icon: 'stat',
        path: '/finance/stat',
        roles: [ROLES.SUPER_ADMIN, ROLES.FINANCE_ADMIN],
      },
      {
        id: 'finance-report',
        title: '财务报表',
        icon: 'report',
        path: '/finance/report',
        roles: [ROLES.SUPER_ADMIN, ROLES.FINANCE_ADMIN],
      },
    ],
  },

  // ========== 数据报表 (高管) ==========
  {
    id: 'report',
    title: '数据报表',
    icon: 'report',
    roles: [ROLES.SUPER_ADMIN, ROLES.COMPANY_LEADER],
    children: [
      {
        id: 'hr-report',
        title: '人事报表',
        icon: 'hr-report',
        path: '/report/hr',
        roles: [ROLES.SUPER_ADMIN, ROLES.COMPANY_LEADER],
      },
      {
        id: 'attendance-report-all',
        title: '考勤报表',
        icon: 'attendance-report',
        path: '/report/attendance',
        roles: [ROLES.SUPER_ADMIN, ROLES.COMPANY_LEADER],
      },
      {
        id: 'finance-report-all',
        title: '财务报表',
        icon: 'finance-report',
        path: '/report/finance',
        roles: [ROLES.SUPER_ADMIN, ROLES.COMPANY_LEADER],
      },
      {
        id: 'work-report',
        title: '工作报表',
        icon: 'work-report',
        path: '/report/work',
        roles: [ROLES.SUPER_ADMIN, ROLES.COMPANY_LEADER],
      },
    ],
  },

  // ========== 系统管理 (超管独有) ==========
  {
    id: 'system',
    title: '系统管理',
    icon: 'system',
    roles: [ROLES.SUPER_ADMIN],
    children: [
      {
        id: 'user',
        title: '用户管理',
        icon: 'user',
        path: '/system/user',
        roles: [ROLES.SUPER_ADMIN],
      },
      {
        id: 'role',
        title: '角色管理',
        icon: 'role',
        path: '/system/role',
        roles: [ROLES.SUPER_ADMIN],
      },
      {
        id: 'menu',
        title: '菜单管理',
        icon: 'menu',
        path: '/system/menu',
        roles: [ROLES.SUPER_ADMIN],
      },
      {
        id: 'dict',
        title: '字典管理',
        icon: 'dict',
        path: '/system/dict',
        roles: [ROLES.SUPER_ADMIN],
      },
      {
        id: 'log',
        title: '操作日志',
        icon: 'log',
        path: '/system/log',
        roles: [ROLES.SUPER_ADMIN],
      },
      {
        id: 'config',
        title: '系统配置',
        icon: 'config',
        path: '/system/config',
        roles: [ROLES.SUPER_ADMIN],
      },
    ],
  },

  // ========== 流程管理 (超管独有) ==========
  {
    id: 'workflow',
    title: '流程管理',
    icon: 'workflow',
    roles: [ROLES.SUPER_ADMIN],
    children: [
      {
        id: 'process-design',
        title: '流程设计',
        icon: 'design',
        path: '/workflow/design',
        roles: [ROLES.SUPER_ADMIN],
      },
      {
        id: 'process-deploy',
        title: '流程部署',
        icon: 'deploy',
        path: '/workflow/deploy',
        roles: [ROLES.SUPER_ADMIN],
      },
      {
        id: 'process-monitor',
        title: '流程监控',
        icon: 'monitor',
        path: '/workflow/monitor',
        roles: [ROLES.SUPER_ADMIN],
      },
    ],
  },

  // ========== 个人中心 ==========
  {
    id: 'profile',
    title: '个人中心',
    icon: 'profile',
    path: '/my/profile',
    roles: ALL_ROLES,
    hidden: true, // 通常在顶部用户菜单中，不在侧边栏显示
  },
]

/**
 * 根据角色过滤菜单
 */
export function filterMenuByRole(menus: MenuItem[], roleCode: string): MenuItem[] {
  return menus
    .filter(menu => !menu.hidden && menu.roles.includes(roleCode))
    .map(menu => {
      if (menu.children) {
        return {
          ...menu,
          children: filterMenuByRole(menu.children, roleCode),
        }
      }
      return menu
    })
    .filter(menu => !menu.children || menu.children.length > 0)
}

/**
 * 获取菜单图标的 SVG 路径
 */
export const menuIcons: Record<string, string> = {
  dashboard: 'M3 3h7v9H3zM14 3h7v5h-7zM14 12h7v9h-7zM3 16h7v5H3z',
  work: 'M9 11l3 3L22 4M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11',
  todo: 'M9 11l3 3L22 4M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11',
  apply: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8zM14 2v6h6M12 18v-6M9 15h6',
  record: 'M12 8v4l3 3m6-3a9 9 0 1 1-18 0a9 9 0 0 1 18 0z',
  task: 'M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2M9 5a2 2 0 0 0 2 2h2a2 2 0 0 0 2-2M9 5a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2',
  approve: 'M12 12m-10 0a10 10 0 1 0 20 0a10 10 0 1 0-20 0M12 6v6l4 2',
  pending: 'M12 12m-10 0a10 10 0 1 0 20 0a10 10 0 1 0-20 0M12 6v6l4 2',
  done: 'M22 11.08V12a10 10 0 1 1-5.93-9.14M22 4L12 14.01l-3-3',
  cc: 'M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2zM22 6l-10 7-10-7',
  calendar: 'M8 2v4M16 2v4M3 10h18M5 4h14a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z',
  log: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8zM14 2v6h6M16 13H8M16 17H8M10 9H8',
  attendance: 'M12 2v4M12 18v4M4.93 4.93l2.83 2.83M16.24 16.24l2.83 2.83M2 12h4M18 12h4M4.93 19.07l2.83-2.83M16.24 7.76l2.83-2.83',
  notice: 'M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9M13.73 21a2 2 0 0 1-3.46 0',
  document: 'M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8zM14 2v6h6M16 13H8M16 17H8M10 9H8',
  team: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 7m-4 0a4 4 0 1 0 8 0a4 4 0 1 0-8 0M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75',
  hr: 'M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2M12 7m-4 0a4 4 0 1 0 8 0a4 4 0 1 0-8 0M19 8v6M22 11h-6',
  org: 'M3 3h7v7H3zM14 3h7v7h-7zM14 14h7v7h-7zM3 14h7v7H3z',
  admin: 'M12 15a3 3 0 1 0 0-6a3 3 0 0 0 0 6zM19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83a2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33a1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0a2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09a1.65 1.65 0 0 0 1.51-1a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83a2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51a1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0a2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z',
  finance: 'M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6',
  report: 'M18 20V10M12 20V4M6 20v-6',
  system: 'M12 15a3 3 0 1 0 0-6a3 3 0 0 0 0 6zM19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33a1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82a1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1 0-4h.09a1.65 1.65 0 0 0 1.51-1a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 4 0v.09a1.65 1.65 0 0 0 1 1.51a1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V9a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z',
  workflow: 'M3 3h6v6H3zM15 3h6v6h-6zM9 15h6v6H9zM6 9v3H6a3 3 0 0 0 3 3h6a3 3 0 0 0 3-3v-3',
  profile: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 7m-4 0a4 4 0 1 0 8 0a4 4 0 1 0-8 0',
  user: 'M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2M12 7m-4 0a4 4 0 1 0 8 0a4 4 0 1 0-8 0',
  role: 'M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 7m-4 0a4 4 0 1 0 8 0a4 4 0 1 0-8 0M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75',
  menu: 'M3 12h18M3 6h18M3 18h18',
  config: 'M12 15a3 3 0 1 0 0-6a3 3 0 0 0 0 6z',
  design: 'M12 20h9M16.5 3.5a2.12 2.12 0 0 1 3 3L7 19l-4 1l1-4z',
  deploy: 'M22 12h-4l-3 9L9 3l-3 9H2',
  monitor: 'M22 12h-4l-3 9L9 3l-3 9H2',
}
