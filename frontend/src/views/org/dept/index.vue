<template>
  <div class="page-container">
    <el-card>
      <!-- 工具栏 -->
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchName"
            placeholder="部门名称"
            style="width: 200px"
            clearable
            @keyup.enter="loadData"
          />
          <el-button type="primary" @click="loadData">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleExpandAll">
            <el-icon><DArrowRight /></el-icon>{{ isExpandAll ? '全部折叠' : '全部展开' }}
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-button type="primary" @click="handleAdd()">
            <el-icon><Plus /></el-icon>新增部门
          </el-button>
        </div>
      </div>

      <!-- 部门树表格 -->
      <el-table
        ref="tableRef"
        :data="treeData"
        v-loading="loading"
        row-key="id"
        :tree-props="{ children: 'children' }"
        :default-expand-all="isExpandAll"
        stripe
        border
      >
        <el-table-column prop="deptName" label="部门名称" min-width="200">
          <template #default="{ row }">
            <span class="dept-name">
              <el-icon v-if="row.children?.length" class="folder-icon"><Folder /></el-icon>
              <el-icon v-else class="file-icon"><Document /></el-icon>
              {{ row.deptName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="deptCode" label="部门编码" width="120" />
        <el-table-column prop="leaderName" label="负责人" width="100">
          <template #default="{ row }">
            <span v-if="row.leaderName">{{ row.leaderName }}</span>
            <span v-else class="text-muted">未设置</span>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="联系电话" width="130" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleAdd(row)">
              <el-icon><Plus /></el-icon>添加
            </el-button>
            <el-button link type="primary" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button link type="danger" @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="550px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级部门">
          <el-tree-select
            v-model="form.parentId"
            :data="treeSelectData"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            check-strictly
            clearable
            placeholder="选择上级部门（顶级留空）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="部门编码" prop="deptCode">
          <el-input v-model="form.deptCode" placeholder="请输入部门编码，如：tech_dept" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人" prop="leaderId">
              <el-select
                v-model="form.leaderId"
                filterable
                clearable
                placeholder="选择负责人"
                style="width: 100%"
              >
                <el-option
                  v-for="user in userList"
                  :key="user.id"
                  :label="`${user.realName} (${user.username})`"
                  :value="user.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="form.sort" :min="0" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phone" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
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
import * as api from '@/api/org'
import * as userApi from '@/api/system'

const loading = ref(false)
const submitLoading = ref(false)
const treeData = ref<any[]>([])
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const tableRef = ref()
const searchName = ref('')
const isExpandAll = ref(true)
const userList = ref<any[]>([])

const form = reactive({
  id: undefined as number | undefined,
  parentId: null as number | null,
  deptName: '',
  deptCode: '',
  leaderId: undefined as number | undefined,
  phone: '',
  email: '',
  sort: 0,
  status: 1,
})

const dialogTitle = computed(() => (form.id ? '编辑部门' : '新增部门'))

const treeSelectData = computed(() => {
  // 过滤掉当前编辑的部门及其子部门
  const filterNode = (nodes: any[]): any[] => {
    return nodes
      .filter(node => node.id !== form.id)
      .map(node => ({
        ...node,
        children: node.children ? filterNode(node.children) : [],
      }))
  }
  return filterNode(treeData.value)
})

const rules: FormRules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  deptCode: [
    { required: true, message: '请输入部门编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '部门编码必须以字母开头，只能包含字母、数字和下划线', trigger: 'blur' },
  ],
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.getDeptTree()
    treeData.value = res.data || []
    
    // 搜索过滤
    if (searchName.value) {
      treeData.value = filterTree(treeData.value, searchName.value)
    }
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const loadUsers = async () => {
  try {
    const res = await userApi.getUsers({ current: 1, size: 1000, status: 1 })
    userList.value = res.records || []
  } catch (error) {
    console.error('加载用户列表失败:', error)
  }
}

const filterTree = (tree: any[], keyword: string): any[] => {
  return tree
    .map(node => {
      const matchSelf = node.deptName?.toLowerCase().includes(keyword.toLowerCase())
      let matchedChildren: any[] = []
      
      if (node.children?.length) {
        matchedChildren = filterTree(node.children, keyword)
      }
      
      if (matchSelf || matchedChildren.length > 0) {
        return {
          ...node,
          children: matchedChildren.length > 0 ? matchedChildren : node.children,
        }
      }
      return null
    })
    .filter(Boolean) as any[]
}

const handleExpandAll = () => {
  isExpandAll.value = !isExpandAll.value
  // 需要重新渲染表格以应用展开/折叠状态
  const temp = treeData.value
  treeData.value = []
  setTimeout(() => {
    treeData.value = temp
  }, 0)
}

const handleAdd = (parent?: any) => {
  Object.assign(form, {
    id: undefined,
    parentId: parent?.id || null,
    deptName: '',
    deptCode: '',
    leaderId: undefined,
    phone: '',
    email: '',
    sort: 0,
    status: 1,
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId || null,
    deptName: row.deptName,
    deptCode: row.deptCode,
    leaderId: row.leaderId,
    phone: row.phone,
    email: row.email,
    sort: row.sort,
    status: row.status,
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    const submitData = {
      ...form,
      parentId: form.parentId || 0,
    }

    if (form.id) {
      await api.updateDept(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await api.createDept(submitData)
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

const handleDelete = (row: any) => {
  if (row.children?.length > 0) {
    ElMessage.warning('该部门下有子部门，无法删除')
    return
  }

  ElMessageBox.confirm(
    `确定要删除部门 "${row.deptName}" 吗？删除后该部门下的员工将失去部门归属。`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    await api.deleteDept(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

onMounted(() => {
  loadData()
  loadUsers()
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

.dept-name {
  display: flex;
  align-items: center;
  gap: 6px;

  .folder-icon {
    color: #e6a23c;
  }

  .file-icon {
    color: #909399;
  }
}

.text-muted {
  color: #909399;
  font-size: 13px;
}

:deep(.el-table) {
  .el-table__row {
    &:hover {
      .el-button {
        opacity: 1;
      }
    }
  }
}
</style>
