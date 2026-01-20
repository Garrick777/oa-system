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
          />
          <el-input
            v-model="queryParams.realName"
            placeholder="姓名"
            style="width: 130px"
            clearable
          />
          <el-tree-select
            v-model="queryParams.deptId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            check-strictly
            clearable
            placeholder="部门"
            style="width: 150px"
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
            <el-icon><Plus /></el-icon>新增员工
          </el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="positionName" label="岗位" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column prop="initialPassword" label="初始密码" width="120">
          <template #default="{ row }">
            <span v-if="row.initialPassword" class="initial-pwd">
              <el-tooltip content="用户尚未修改密码" placement="top">
                <span>{{ row.initialPassword }}</span>
              </el-tooltip>
            </span>
            <span v-else class="pwd-changed">已修改</span>
          </template>
        </el-table-column>
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
        <el-table-column prop="lastLoginTime" label="最后登录" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
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
      <div style="margin-top: 16px; display: flex; justify-content: flex-end;">
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" :disabled="!!form.id" placeholder="请输入用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="!form.id">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入密码" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" placeholder="请确认密码" />
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
            <el-form-item label="岗位" prop="positionId">
              <el-select v-model="form.positionId" placeholder="选择岗位" style="width: 100%" clearable>
                <el-option
                  v-for="pos in positionList"
                  :key="pos.id"
                  :label="pos.positionName"
                  :value="pos.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="角色" prop="roleId">
              <el-select v-model="form.roleId" placeholder="选择角色" style="width: 100%">
                <el-option
                  v-for="role in roleList"
                  :key="role.id"
                  :label="role.roleName"
                  :value="role.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.gender">
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
                <el-radio :value="0">未知</el-radio>
              </el-radio-group>
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
        <el-form-item label="状态">
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
const tableData = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const deptTree = ref<any[]>([])
const positionList = ref<any[]>([])
const roleList = ref<any[]>([])

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
  deptId: undefined as number | undefined,
  positionId: undefined as number | undefined,
  roleId: undefined as number | undefined,
  phone: '',
  email: '',
  gender: 0,
  status: 1,
})

const dialogTitle = computed(() => (form.id ? '编辑员工' : '新增员工'))

const validatePassword = (rule: any, value: any, callback: any) => {
  if (!form.id && !value) {
    callback(new Error('请输入密码'))
  } else if (form.confirmPassword && value !== form.confirmPassword) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (!form.id && !value) {
    callback(new Error('请确认密码'))
  } else if (value !== form.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ validator: validateConfirmPassword, trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择角色', trigger: 'change' }],
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
    const [deptRes, posRes, roleRes] = await Promise.all([
      orgApi.getDeptTree(),
      orgApi.getAllPositions(),
      userApi.getAllRoles(),
    ])
    deptTree.value = deptRes.data || []
    positionList.value = posRes.data || []
    roleList.value = roleRes.data || []
  } catch (error) {
    console.error('加载选项失败:', error)
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
    deptId: undefined,
    positionId: undefined,
    roleId: undefined,
    phone: '',
    email: '',
    gender: 0,
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
    deptId: row.deptId,
    positionId: row.positionId,
    roleId: row.roleId,
    phone: row.phone,
    email: row.email,
    gender: row.gender,
    status: row.status,
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const data = { ...form }
    delete (data as any).confirmPassword
    
    if (form.id) {
      await userApi.updateUser(form.id, data)
      ElMessage.success('更新成功')
    } else {
      await userApi.createUser(data)
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
  ElMessageBox.confirm(`确定要重置 "${row.realName}" 的密码为 123456 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await userApi.resetUserPassword(row.id, '123456')
    ElMessage.success('密码重置成功')
  })
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除员工 "${row.realName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await userApi.deleteUser(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

onMounted(() => {
  loadData()
  loadOptions()
})
</script>

<style scoped lang="scss">
.page-container {
  :deep(.el-table) {
    width: 100%;
  }
}

.table-toolbar {
  .toolbar-left {
    flex: 1;
    min-width: 0;
  }
}

.initial-pwd {
  color: #e6a23c;
  font-family: monospace;
  background: #fdf6ec;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 13px;
}

.pwd-changed {
  color: #67c23a;
  font-size: 13px;
}
</style>
