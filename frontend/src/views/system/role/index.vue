<template>
  <div class="page-container">
    <el-card>
      <!-- 搜索栏 -->
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="queryParams.roleName"
            placeholder="角色名称"
            style="width: 150px"
            clearable
            @keyup.enter="handleSearch"
          />
          <el-input
            v-model="queryParams.roleCode"
            placeholder="角色编码"
            style="width: 150px"
            clearable
            @keyup.enter="handleSearch"
          />
          <el-select v-model="queryParams.status" placeholder="状态" style="width: 120px" clearable>
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
            <el-icon><Plus /></el-icon>新增角色
          </el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="dataScope" label="数据范围" width="130">
          <template #default="{ row }">
            <el-tag :type="getDataScopeType(row.dataScope)">
              {{ getDataScopeText(row.dataScope) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              :disabled="row.roleCode === 'super_admin'"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="success" @click="handleAssignMenu(row)">权限</el-button>
            <el-button
              link
              type="danger"
              :disabled="row.roleCode === 'super_admin'"
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
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input
            v-model="form.roleCode"
            :disabled="form.roleCode === 'super_admin'"
            placeholder="请输入角色编码"
          />
        </el-form-item>
        <el-form-item label="数据范围" prop="dataScope">
          <el-select v-model="form.dataScope" placeholder="请选择数据范围" style="width: 100%">
            <el-option label="全部数据" :value="1" />
            <el-option label="本部门及以下" :value="2" />
            <el-option label="本部门数据" :value="3" />
            <el-option label="仅本人数据" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限弹窗 -->
    <el-dialog v-model="menuDialogVisible" title="分配菜单权限" width="500px">
      <el-tree
        ref="menuTreeRef"
        :data="menuTree"
        :props="{ label: 'menuName', children: 'children' }"
        show-checkbox
        node-key="id"
        default-expand-all
        :default-checked-keys="checkedMenuIds"
      />
      <template #footer>
        <el-button @click="menuDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="menuSubmitLoading" @click="handleMenuSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import * as api from '@/api/system'

const loading = ref(false)
const submitLoading = ref(false)
const menuSubmitLoading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const menuDialogVisible = ref(false)
const formRef = ref<FormInstance>()
const menuTreeRef = ref()
const menuTree = ref<any[]>([])
const checkedMenuIds = ref<number[]>([])
const currentRoleId = ref<number>()

const queryParams = reactive({
  current: 1,
  size: 10,
  roleName: '',
  roleCode: '',
  status: undefined as number | undefined,
})

const form = reactive({
  id: undefined as number | undefined,
  roleName: '',
  roleCode: '',
  dataScope: 1,
  sort: 0,
  status: 1,
  remark: '',
})

const dialogTitle = computed(() => (form.id ? '编辑角色' : '新增角色'))

const rules: FormRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
}

const getDataScopeType = (scope: number) => {
  const types: Record<number, string> = { 1: 'success', 2: 'primary', 3: 'warning', 4: 'info' }
  return types[scope] || 'info'
}

const getDataScopeText = (scope: number) => {
  const texts: Record<number, string> = {
    1: '全部数据',
    2: '本部门及以下',
    3: '本部门数据',
    4: '仅本人数据',
  }
  return texts[scope] || '未知'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.getRoles(queryParams)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const loadMenuTree = async () => {
  try {
    const res = await api.getMenuTree()
    menuTree.value = res.data || []
  } catch (error) {
    // 使用默认菜单树
    menuTree.value = [
      {
        id: 1,
        menuName: '系统管理',
        children: [
          { id: 2, menuName: '用户管理' },
          { id: 3, menuName: '角色管理' },
          { id: 4, menuName: '菜单管理' },
          { id: 5, menuName: '字典管理' },
        ],
      },
      {
        id: 6,
        menuName: '组织架构',
        children: [
          { id: 7, menuName: '部门管理' },
          { id: 8, menuName: '岗位管理' },
          { id: 9, menuName: '员工档案' },
        ],
      },
    ]
  }
}

const handleSearch = () => {
  queryParams.current = 1
  loadData()
}

const handleReset = () => {
  queryParams.roleName = ''
  queryParams.roleCode = ''
  queryParams.status = undefined
  handleSearch()
}

const handleAdd = () => {
  Object.assign(form, {
    id: undefined,
    roleName: '',
    roleCode: '',
    dataScope: 1,
    sort: 0,
    status: 1,
    remark: '',
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, {
    id: row.id,
    roleName: row.roleName,
    roleCode: row.roleCode,
    dataScope: row.dataScope,
    sort: row.sort,
    status: row.status,
    remark: row.remark,
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (form.id) {
      await api.updateRole(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await api.createRole(form)
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
    await api.updateRoleStatus(row.id, row.status)
    ElMessage.success('状态更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await api.deleteRole(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

const handleAssignMenu = async (row: any) => {
  currentRoleId.value = row.id
  try {
    const res = await api.getRoleMenus(row.id)
    checkedMenuIds.value = res.data || []
  } catch (error) {
    checkedMenuIds.value = []
  }
  menuDialogVisible.value = true
}

const handleMenuSubmit = async () => {
  if (!currentRoleId.value) return

  menuSubmitLoading.value = true
  try {
    const checkedKeys = menuTreeRef.value?.getCheckedKeys() || []
    const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || []
    const menuIds = [...checkedKeys, ...halfCheckedKeys]

    await api.assignRoleMenus(currentRoleId.value, menuIds)
    ElMessage.success('分配权限成功')
    menuDialogVisible.value = false
  } catch (error) {
    console.error('分配权限失败:', error)
  } finally {
    menuSubmitLoading.value = false
  }
}

onMounted(() => {
  loadData()
  loadMenuTree()
})
</script>

<style scoped lang="scss">
</style>
