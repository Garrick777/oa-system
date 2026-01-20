<template>
  <div class="page-container">
    <el-card>
      <!-- 搜索栏 -->
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="queryParams.username"
            placeholder="用户名"
            style="width: 130px"
            clearable
            @keyup.enter="handleSearch"
          />
          <el-input
            v-model="queryParams.realName"
            placeholder="真实姓名"
            style="width: 130px"
            clearable
            @keyup.enter="handleSearch"
          />
          <el-tree-select
            v-model="queryParams.deptId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            check-strictly
            clearable
            placeholder="部门"
            style="width: 160px"
          />
          <el-select v-model="queryParams.status" placeholder="状态" style="width: 100px" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增
          </el-button>
          <el-dropdown @command="handleImportCommand">
            <el-button type="success">
              <el-icon><Upload /></el-icon>导入<el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="import">
                  <el-icon><Upload /></el-icon>导入用户
                </el-dropdown-item>
                <el-dropdown-item command="template">
                  <el-icon><Download /></el-icon>下载模板
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
          <el-button type="warning" @click="handleExport">
            <el-icon><Download /></el-icon>导出
          </el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" width="120">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.username }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="真实姓名" width="100" />
        <el-table-column prop="roleName" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.roleCode)" size="small">{{ row.roleName || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="部门" width="130" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :disabled="row.username === 'admin'"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleView(row)">详情</el-button>
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button
              link
              type="danger"
              :disabled="row.username === 'admin'"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="table-pagination">
        <el-pagination
          v-model:current-page="queryParams.current"
          v-model:page-size="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadData"
          @current-change="loadData"
        />
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" :disabled="!!form.id" placeholder="请输入用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="!form.id">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" placeholder="请确认密码" show-password />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部门" prop="deptId">
              <el-tree-select
                v-model="form.deptId"
                :data="deptTree"
                :props="{ label: 'deptName', value: 'id', children: 'children' }"
                check-strictly
                clearable
                placeholder="选择部门"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" prop="roleId">
              <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
                <el-option
                  v-for="role in roleOptions"
                  :key="role.id"
                  :label="role.roleName"
                  :value="role.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 用户详情抽屉 -->
    <el-drawer
      v-model="detailVisible"
      title="用户详情"
      size="480px"
      direction="rtl"
    >
      <div class="user-detail" v-if="detailData">
        <!-- 基本信息卡片 -->
        <div class="detail-card">
          <div class="detail-header">
            <el-avatar :size="64" :src="detailData.avatar">
              {{ detailData.realName?.charAt(0) || detailData.username?.charAt(0) }}
            </el-avatar>
            <div class="header-info">
              <h3>{{ detailData.realName || detailData.username }}</h3>
              <p>{{ detailData.deptName }} · {{ detailData.positionName || '未设置岗位' }}</p>
              <el-tag :type="detailData.status === 1 ? 'success' : 'danger'" size="small">
                {{ detailData.status === 1 ? '在职' : '离职' }}
              </el-tag>
            </div>
          </div>
        </div>

        <!-- 账号信息 -->
        <div class="detail-section">
          <h4><el-icon><User /></el-icon> 账号信息</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="用户名">{{ detailData.username }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag :type="getRoleTagType(detailData.roleCode)">{{ detailData.roleName || '-' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="初始密码">
              <span v-if="detailData.initialPassword" class="initial-pwd">
                {{ detailData.initialPassword }}
                <el-tag type="warning" size="small">未修改</el-tag>
              </span>
              <span v-else class="pwd-changed">已修改</span>
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="detailData.status === 1 ? 'success' : 'danger'">
                {{ detailData.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 联系方式 -->
        <div class="detail-section">
          <h4><el-icon><Phone /></el-icon> 联系方式</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="手机号">{{ detailData.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ detailData.email || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 组织信息 -->
        <div class="detail-section">
          <h4><el-icon><OfficeBuilding /></el-icon> 组织信息</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="所属部门">{{ detailData.deptName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="岗位">{{ detailData.positionName || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 时间信息 -->
        <div class="detail-section">
          <h4><el-icon><Clock /></el-icon> 时间信息</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="创建时间">{{ detailData.createTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="最后登录">{{ detailData.lastLoginTime || '从未登录' }}</el-descriptions-item>
            <el-descriptions-item label="登录IP">{{ detailData.lastLoginIp || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="handleEdit(detailData); detailVisible = false">编辑</el-button>
      </template>
    </el-drawer>

    <!-- 导入用户弹窗 -->
    <el-dialog v-model="importDialogVisible" title="导入用户" width="500px" destroy-on-close>
      <el-upload
        ref="uploadRef"
        drag
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleFileChange"
        :on-exceed="handleExceed"
      >
        <el-icon class="el-icon--upload"><Upload /></el-icon>
        <div class="el-upload__text">
          将文件拖到此处，或<em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            仅支持 .xlsx / .xls 格式文件，
            <el-link type="primary" @click="handleDownloadTemplate">下载导入模板</el-link>
          </div>
        </template>
      </el-upload>
      <template #footer>
        <el-button @click="importDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="importLoading" @click="handleImportConfirm">
          确认导入
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import * as userApi from '@/api/system'
import * as orgApi from '@/api/org'

const loading = ref(false)
const submitLoading = ref(false)
const importLoading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const importDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const uploadRef = ref()
const detailData = ref<any>(null)
const deptTree = ref<any[]>([])
const roleOptions = ref<any[]>([])
const importFile = ref<File | null>(null)

const queryParams = reactive({
  current: 1,
  size: 10,
  username: '',
  realName: '',
  deptId: undefined as number | undefined,
  status: undefined as number | undefined,
})

const form = reactive({
  id: undefined as number | undefined,
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  phone: '',
  email: '',
  deptId: undefined as number | undefined,
  roleId: undefined as number | undefined,
  status: 1,
})

const dialogTitle = computed(() => (form.id ? '编辑用户' : '新增用户'))

// 密码验证
const validatePassword = (rule: any, value: any, callback: any) => {
  if (!form.id && !value) {
    callback(new Error('请输入密码'))
  } else if (value && value.length < 6) {
    callback(new Error('密码长度不能少于6位'))
  } else {
    if (form.confirmPassword) {
      formRef.value?.validateField('confirmPassword')
    }
    callback()
  }
}

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (!form.id && !value) {
    callback(new Error('请确认密码'))
  } else if (value !== form.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度3-20个字符', trigger: 'blur' },
  ],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
}

// 角色标签颜色
const getRoleTagType = (roleCode: string) => {
  const types: Record<string, string> = {
    super_admin: 'danger',
    company_leader: 'warning',
    hr_admin: 'success',
    admin_officer: 'primary',
    finance_admin: '',
    dept_manager: 'info',
    employee: '',
  }
  return types[roleCode] || ''
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await userApi.getUsers(queryParams)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  try {
    const [deptRes, roleRes] = await Promise.all([
      orgApi.getDeptTree(),
      userApi.getAllRoles(),
    ])
    deptTree.value = deptRes.data || []
    roleOptions.value = roleRes.data || []
  } catch (error) {
    // 使用默认角色
    roleOptions.value = [
      { id: 1, roleName: '超级管理员', roleCode: 'super_admin' },
      { id: 2, roleName: '公司高管', roleCode: 'company_leader' },
      { id: 3, roleName: 'HR管理员', roleCode: 'hr_admin' },
      { id: 4, roleName: '行政管理员', roleCode: 'admin_officer' },
      { id: 5, roleName: '财务管理员', roleCode: 'finance_admin' },
      { id: 6, roleName: '部门经理', roleCode: 'dept_manager' },
      { id: 7, roleName: '普通员工', roleCode: 'employee' },
    ]
  }
}

const handleSearch = () => {
  queryParams.current = 1
  loadData()
}

const handleReset = () => {
  queryParams.username = ''
  queryParams.realName = ''
  queryParams.deptId = undefined
  queryParams.status = undefined
  handleSearch()
}

const handleAdd = () => {
  Object.assign(form, {
    id: undefined,
    username: '',
    password: '',
    confirmPassword: '',
    realName: '',
    phone: '',
    email: '',
    deptId: undefined,
    roleId: undefined,
    status: 1,
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, {
    id: row.id,
    username: row.username,
    password: '',
    confirmPassword: '',
    realName: row.realName,
    phone: row.phone,
    email: row.email,
    deptId: row.deptId,
    roleId: row.roleId,
    status: row.status,
  })
  dialogVisible.value = true
}

const handleView = async (row: any) => {
  detailData.value = row
  detailVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const submitData: any = {
      username: form.username,
      realName: form.realName,
      phone: form.phone,
      email: form.email,
      deptId: form.deptId,
      roleId: form.roleId,
      status: form.status,
    }

    if (!form.id) {
      submitData.password = form.password
    }

    if (form.id) {
      await userApi.updateUser(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await userApi.createUser(submitData)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    submitLoading.value = false
  }
}

const handleStatusChange = async (row: any) => {
  try {
    await userApi.updateUserStatus(row.id, row.status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
  }
}

const handleResetPwd = (row: any) => {
  ElMessageBox.confirm(`确定要重置用户 "${row.realName || row.username}" 的密码吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await userApi.resetUserPassword(row.id)
    ElMessage.success('密码已重置为: 123456')
    loadData()
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除用户 "${row.realName || row.username}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await userApi.deleteUser(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

// ========== 导入/导出功能 ==========

const handleImportCommand = (command: string) => {
  if (command === 'import') {
    importFile.value = null
    uploadRef.value?.clearFiles()
    importDialogVisible.value = true
  } else if (command === 'template') {
    handleDownloadTemplate()
  }
}

const handleFileChange = (file: any) => {
  importFile.value = file.raw
}

const handleExceed = () => {
  ElMessage.warning('只能上传一个文件，请先删除已选文件')
}

const handleDownloadTemplate = async () => {
  try {
    ElMessage.info('正在下载模板...')
    const res = await userApi.downloadUserTemplate()
    downloadFile(res, '用户导入模板.xlsx')
  } catch (error) {
    // 如果后端未实现，提供模拟下载
    ElMessage.warning('模板下载功能暂不可用，请使用以下格式：用户名、真实姓名、手机号、邮箱、部门、角色')
  }
}

const handleImportConfirm = async () => {
  if (!importFile.value) {
    ElMessage.warning('请先选择要导入的文件')
    return
  }

  importLoading.value = true
  try {
    const res = await userApi.importUsers(importFile.value)
    ElMessage.success(`导入成功！共导入 ${res.data?.successCount || 0} 条记录`)
    importDialogVisible.value = false
    loadData()
  } catch (error: any) {
    ElMessage.error(error.message || '导入失败，请检查文件格式')
  } finally {
    importLoading.value = false
  }
}

const handleExport = async () => {
  try {
    ElMessage.info('正在导出...')
    const res = await userApi.exportUsers(queryParams)
    const fileName = `用户列表_${new Date().toLocaleDateString().replace(/\//g, '-')}.xlsx`
    downloadFile(res, fileName)
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.warning('导出功能暂不可用')
  }
}

// 文件下载辅助函数
const downloadFile = (data: any, fileName: string) => {
  const blob = data instanceof Blob ? data : new Blob([data])
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.click()
  window.URL.revokeObjectURL(url)
}

onMounted(() => {
  loadData()
  loadOptions()
})
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;

  .toolbar-left {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
    align-items: center;
  }

  .toolbar-right {
    display: flex;
    gap: 8px;
  }
}

.table-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

// 用户详情样式
.user-detail {
  padding: 0 10px;

  .detail-card {
    .detail-header {
      display: flex;
      gap: 16px;
      padding: 20px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12px;
      color: white;
      margin-bottom: 20px;

      .header-info {
        h3 {
          margin: 0 0 4px;
          font-size: 20px;
        }
        p {
          margin: 0 0 8px;
          opacity: 0.9;
          font-size: 14px;
        }
      }
    }
  }

  .detail-section {
    margin-bottom: 20px;

    h4 {
      display: flex;
      align-items: center;
      gap: 8px;
      margin: 0 0 12px;
      font-size: 15px;
      color: #303133;
      font-weight: 600;

      .el-icon {
        color: #409eff;
      }
    }

    :deep(.el-descriptions) {
      .el-descriptions__label {
        width: 100px;
        font-weight: 500;
      }
    }
  }
}

.initial-pwd {
  color: #e6a23c;
  font-family: monospace;
  display: flex;
  align-items: center;
  gap: 8px;
}

.pwd-changed {
  color: #67c23a;
}
</style>
