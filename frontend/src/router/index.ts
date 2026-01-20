import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import { ElMessage } from 'element-plus'

// 通用占位页面组件
const PlaceholderPage = () => import('@/views/common/PlaceholderPage.vue')

// 路由配置
const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', public: true },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      // ========== 工作台 ==========
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台' },
      },

      // ========== 我的工作 ==========
      {
        path: 'my/todo',
        name: 'MyTodo',
        component: () => import('@/views/workflow/todo/index.vue'),
        meta: { title: '待办事项' },
      },
      {
        path: 'my/apply',
        name: 'MyApply',
        component: () => import('@/views/workflow/apply/index.vue'),
        meta: { title: '我的申请' },
      },
      {
        path: 'my/approved',
        name: 'MyApproved',
        component: PlaceholderPage,
        meta: { title: '审批记录' },
      },
      {
        path: 'my/task',
        name: 'MyTask',
        component: PlaceholderPage,
        meta: { title: '我的任务' },
      },
      {
        path: 'my/profile',
        name: 'MyProfile',
        component: () => import('@/views/my/profile/index.vue'),
        meta: { title: '个人中心' },
      },

      // ========== 审批中心 ==========
      {
        path: 'approve/pending',
        name: 'ApprovePending',
        component: () => import('@/views/workflow/todo/index.vue'),
        meta: { title: '待我审批' },
      },
      {
        path: 'approve/done',
        name: 'ApproveDone',
        component: () => import('@/views/workflow/done/index.vue'),
        meta: { title: '已审批' },
      },
      {
        path: 'approve/cc',
        name: 'ApproveCc',
        component: PlaceholderPage,
        meta: { title: '抄送给我' },
      },
      
      // ========== 审批申请页面 ==========
      {
        path: 'workflow/apply',
        name: 'WorkflowApply',
        component: () => import('@/views/workflow/apply/index.vue'),
        meta: { title: '发起申请' },
      },
      {
        path: 'workflow/leave/create',
        name: 'LeaveCreate',
        component: () => import('@/views/workflow/leave/create.vue'),
        meta: { title: '请假申请' },
      },
      {
        path: 'workflow/expense/create',
        name: 'ExpenseCreate',
        component: PlaceholderPage,
        meta: { title: '报销申请' },
      },

      // ========== 日程管理 ==========
      {
        path: 'schedule/my',
        name: 'MySchedule',
        component: PlaceholderPage,
        meta: { title: '我的日程' },
      },
      {
        path: 'schedule/meeting',
        name: 'MeetingSchedule',
        component: PlaceholderPage,
        meta: { title: '会议日程' },
      },

      // ========== 工作日志 ==========
      {
        path: 'worklog/write',
        name: 'WriteLog',
        component: PlaceholderPage,
        meta: { title: '写日志' },
      },
      {
        path: 'worklog/my',
        name: 'MyLog',
        component: PlaceholderPage,
        meta: { title: '我的日志' },
      },
      {
        path: 'worklog/team',
        name: 'TeamLog',
        component: PlaceholderPage,
        meta: { title: '团队日志' },
      },

      // ========== 考勤管理 ==========
      {
        path: 'attendance/clock',
        name: 'ClockIn',
        component: PlaceholderPage,
        meta: { title: '打卡签到' },
      },
      {
        path: 'attendance/my',
        name: 'MyAttendance',
        component: PlaceholderPage,
        meta: { title: '我的考勤' },
      },
      {
        path: 'attendance/team',
        name: 'TeamAttendance',
        component: PlaceholderPage,
        meta: { title: '团队考勤' },
      },
      {
        path: 'attendance/rule',
        name: 'AttendanceRule',
        component: PlaceholderPage,
        meta: { title: '考勤规则' },
      },
      {
        path: 'attendance/report',
        name: 'AttendanceReport',
        component: PlaceholderPage,
        meta: { title: '考勤报表' },
      },

      // ========== 公告通知 ==========
      {
        path: 'notice',
        name: 'Notice',
        component: PlaceholderPage,
        meta: { title: '公告通知' },
      },

      // ========== 文档中心 ==========
      {
        path: 'document/public',
        name: 'PublicDoc',
        component: PlaceholderPage,
        meta: { title: '公共文档' },
      },
      {
        path: 'document/dept',
        name: 'DeptDoc',
        component: PlaceholderPage,
        meta: { title: '部门文档' },
      },
      {
        path: 'document/my',
        name: 'MyDoc',
        component: PlaceholderPage,
        meta: { title: '我的文档' },
      },

      // ========== 团队管理 ==========
      {
        path: 'team/member',
        name: 'TeamMember',
        component: PlaceholderPage,
        meta: { title: '部门成员' },
      },
      {
        path: 'team/task',
        name: 'TeamTask',
        component: PlaceholderPage,
        meta: { title: '任务分配' },
      },

      // ========== 人事管理 ==========
      {
        path: 'hr/employee',
        name: 'HrEmployee',
        component: () => import('@/views/org/employee/index.vue'),
        meta: { title: '员工档案' },
      },
      {
        path: 'hr/onboard',
        name: 'Onboard',
        component: PlaceholderPage,
        meta: { title: '入职管理' },
      },
      {
        path: 'hr/resign',
        name: 'Resign',
        component: PlaceholderPage,
        meta: { title: '离职管理' },
      },
      {
        path: 'hr/regular',
        name: 'Regular',
        component: PlaceholderPage,
        meta: { title: '转正管理' },
      },

      // ========== 组织架构 ==========
      {
        path: 'org/dept',
        name: 'OrgDept',
        component: () => import('@/views/org/dept/index.vue'),
        meta: { title: '部门管理' },
      },
      {
        path: 'org/position',
        name: 'OrgPosition',
        component: () => import('@/views/org/position/index.vue'),
        meta: { title: '岗位管理' },
      },
      {
        path: 'org/chart',
        name: 'OrgChart',
        component: () => import('@/views/org/chart/index.vue'),
        meta: { title: '组织架构图' },
      },

      // ========== 行政管理 ==========
      {
        path: 'admin/supplies',
        name: 'Supplies',
        component: PlaceholderPage,
        meta: { title: '办公用品' },
      },
      {
        path: 'admin/assets',
        name: 'Assets',
        component: PlaceholderPage,
        meta: { title: '资产管理' },
      },
      {
        path: 'admin/meeting-room',
        name: 'MeetingRoom',
        component: PlaceholderPage,
        meta: { title: '会议室管理' },
      },

      // ========== 财务管理 ==========
      {
        path: 'finance/expense',
        name: 'ExpenseManage',
        component: PlaceholderPage,
        meta: { title: '报销管理' },
      },
      {
        path: 'finance/stat',
        name: 'FinanceStat',
        component: PlaceholderPage,
        meta: { title: '费用统计' },
      },
      {
        path: 'finance/report',
        name: 'FinanceReport',
        component: PlaceholderPage,
        meta: { title: '财务报表' },
      },

      // ========== 数据报表 ==========
      {
        path: 'report/hr',
        name: 'HrReport',
        component: PlaceholderPage,
        meta: { title: '人事报表' },
      },
      {
        path: 'report/attendance',
        name: 'AttendanceReportAll',
        component: PlaceholderPage,
        meta: { title: '考勤报表' },
      },
      {
        path: 'report/finance',
        name: 'FinanceReportAll',
        component: PlaceholderPage,
        meta: { title: '财务报表' },
      },
      {
        path: 'report/work',
        name: 'WorkReport',
        component: PlaceholderPage,
        meta: { title: '工作报表' },
      },

      // ========== 系统管理 ==========
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理' },
      },
      {
        path: 'system/menu',
        name: 'SystemMenu',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理' },
      },
      {
        path: 'system/dict',
        name: 'SystemDict',
        component: () => import('@/views/system/dict/index.vue'),
        meta: { title: '字典管理' },
      },
      {
        path: 'system/log',
        name: 'SystemLog',
        component: () => import('@/views/system/log/index.vue'),
        meta: { title: '操作日志' },
      },
      {
        path: 'system/config',
        name: 'SystemConfig',
        component: () => import('@/views/system/config/index.vue'),
        meta: { title: '系统配置' },
      },

      // ========== 流程管理 ==========
      {
        path: 'workflow/design',
        name: 'ProcessDesign',
        component: PlaceholderPage,
        meta: { title: '流程设计' },
      },
      {
        path: 'workflow/deploy',
        name: 'ProcessDeploy',
        component: PlaceholderPage,
        meta: { title: '流程部署' },
      },
      {
        path: 'workflow/monitor',
        name: 'ProcessMonitor',
        component: PlaceholderPage,
        meta: { title: '流程监控' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', public: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 配置 NProgress
NProgress.configure({ showSpinner: false })

// 路由守卫
router.beforeEach(async (to, from, next) => {
  NProgress.start()
  document.title = `${to.meta.title || 'OA办公系统'} - OA办公系统`

  const userStore = useUserStore()

  // 公开页面直接放行
  if (to.meta.public) {
    next()
    return
  }

  // 未登录跳转登录页
  if (!userStore.isLoggedIn()) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }

  // 已登录但没有用户信息，获取用户信息
  if (!userStore.userInfo) {
    await userStore.getInfo()
  }

  // 检查路径权限
  const permissionStore = usePermissionStore()
  if (!permissionStore.hasPathPermission(to.path)) {
    ElMessage.error('您没有访问该页面的权限')
    next({ path: '/dashboard' })
    return
  }

  next()
})

router.afterEach(() => {
  NProgress.done()
})

export default router
