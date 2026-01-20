<template>
  <div class="page-container">
    <el-card>
      <!-- 工具栏 -->
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="searchName"
            placeholder="菜单名称"
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
            <el-icon><Plus /></el-icon>新增菜单
          </el-button>
        </div>
      </div>

      <!-- 菜单树表格 -->
      <el-table
        ref="tableRef"
        :data="treeData"
        v-loading="loading"
        row-key="id"
        :tree-props="{ children: 'children' }"
        :default-expand-all="isExpandAll"
        border
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="180">
          <template #default="{ row }">
            <span class="menu-name">
              <el-icon v-if="row.icon" :class="row.icon"><component :is="row.icon" /></el-icon>
              {{ row.menuName }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="menuType" label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="getMenuTypeTag(row.menuType)" size="small">
              {{ getMenuTypeText(row.menuType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="icon" label="图标" width="80" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon" :size="18"><component :is="row.icon" /></el-icon>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="路由地址" width="160" show-overflow-tooltip />
        <el-table-column prop="component" label="组件路径" width="200" show-overflow-tooltip />
        <el-table-column prop="permission" label="权限标识" width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <el-tag v-if="row.permission" type="info" size="small">{{ row.permission }}</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="visible" label="可见" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.visible === 1 ? 'success' : 'info'" size="small">
              {{ row.visible === 1 ? '显示' : '隐藏' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.menuType !== 'F'" link type="primary" @click="handleAdd(row)">
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="treeSelectData"
            :props="{ label: 'menuName', value: 'id', children: 'children' }"
            check-strictly
            clearable
            placeholder="选择上级菜单（顶级菜单留空）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio value="M">目录</el-radio>
            <el-radio value="C">菜单</el-radio>
            <el-radio value="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="菜单名称" prop="menuName">
              <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序" prop="sort">
              <el-input-number v-model="form.sort" :min="0" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="form.menuType !== 'F'">
          <el-col :span="12">
            <el-form-item label="路由地址" prop="path">
              <el-input v-model="form.path" placeholder="如：/system/user" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="图标">
              <el-select
                v-model="form.icon"
                filterable
                clearable
                placeholder="选择图标"
                style="width: 100%"
              >
                <el-option
                  v-for="icon in iconList"
                  :key="icon"
                  :label="icon"
                  :value="icon"
                >
                  <div style="display: flex; align-items: center; gap: 8px;">
                    <el-icon><component :is="icon" /></el-icon>
                    <span>{{ icon }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="form.menuType === 'C'" label="组件路径" prop="component">
          <el-input v-model="form.component" placeholder="如：views/system/user/index" />
        </el-form-item>
        <el-form-item v-if="form.menuType !== 'M'" label="权限标识" prop="permission">
          <el-input v-model="form.permission" placeholder="如：system:user:list" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item v-if="form.menuType !== 'F'" label="显示状态">
              <el-radio-group v-model="form.visible">
                <el-radio :value="1">显示</el-radio>
                <el-radio :value="0">隐藏</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单状态">
              <el-radio-group v-model="form.status">
                <el-radio :value="1">启用</el-radio>
                <el-radio :value="0">禁用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
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
import * as api from '@/api/system'

// 常用图标列表
const iconList = [
  'HomeFilled', 'Setting', 'User', 'UserFilled', 'Avatar',
  'Menu', 'Grid', 'Document', 'Folder', 'FolderOpened',
  'Calendar', 'Clock', 'Bell', 'Message', 'MessageBox',
  'Edit', 'Delete', 'Plus', 'Search', 'Refresh',
  'Check', 'Close', 'Warning', 'InfoFilled', 'QuestionFilled',
  'List', 'Tickets', 'Money', 'Goods', 'Box',
  'OfficeBuilding', 'Phone', 'Location', 'Van', 'VideoCamera',
  'Monitor', 'Connection', 'DataAnalysis', 'Stamp', 'Collection',
  'CopyDocument', 'DocumentChecked', 'Files', 'Wallet', 'CreditCard',
  'Upload', 'Download', 'Share', 'Switch', 'Tools',
]

const loading = ref(false)
const submitLoading = ref(false)
const treeData = ref<any[]>([])
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const tableRef = ref()
const searchName = ref('')
const isExpandAll = ref(true)

const form = reactive({
  id: undefined as number | undefined,
  parentId: null as number | null,
  menuName: '',
  menuType: 'C' as 'M' | 'C' | 'F',
  path: '',
  component: '',
  permission: '',
  icon: '',
  sort: 0,
  visible: 1,
  status: 1,
})

const dialogTitle = computed(() => (form.id ? '编辑菜单' : '新增菜单'))

const treeSelectData = computed(() => {
  // 只显示目录和菜单，不显示按钮
  const filterNode = (nodes: any[]): any[] => {
    return nodes
      .filter(node => node.id !== form.id && node.menuType !== 'F')
      .map(node => ({
        ...node,
        children: node.children ? filterNode(node.children) : [],
      }))
  }
  return filterNode(treeData.value)
})

const rules: FormRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  path: [{ required: true, message: '请输入路由地址', trigger: 'blur' }],
}

const getMenuTypeText = (type: string) => {
  const texts: Record<string, string> = { M: '目录', C: '菜单', F: '按钮' }
  return texts[type] || '未知'
}

const getMenuTypeTag = (type: string) => {
  const types: Record<string, string> = { M: 'warning', C: 'success', F: 'info' }
  return types[type] || ''
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.getMenuTree()
    treeData.value = res.data || []
    
    // 搜索过滤
    if (searchName.value) {
      treeData.value = filterTree(treeData.value, searchName.value)
    }
  } catch (error) {
    console.error('加载数据失败:', error)
    // 使用默认菜单数据
    treeData.value = getDefaultMenuData()
  } finally {
    loading.value = false
  }
}

// 默认菜单数据（后端未实现时使用）
const getDefaultMenuData = () => [
  {
    id: 1,
    menuName: '工作台',
    menuType: 'C',
    path: '/dashboard',
    icon: 'HomeFilled',
    sort: 1,
    visible: 1,
    status: 1,
  },
  {
    id: 2,
    menuName: '系统管理',
    menuType: 'M',
    path: '/system',
    icon: 'Setting',
    sort: 100,
    visible: 1,
    status: 1,
    children: [
      { id: 21, menuName: '用户管理', menuType: 'C', path: '/system/user', component: 'views/system/user/index', icon: 'User', sort: 1, visible: 1, status: 1 },
      { id: 22, menuName: '角色管理', menuType: 'C', path: '/system/role', component: 'views/system/role/index', icon: 'UserFilled', sort: 2, visible: 1, status: 1 },
      { id: 23, menuName: '菜单管理', menuType: 'C', path: '/system/menu', component: 'views/system/menu/index', icon: 'Menu', sort: 3, visible: 1, status: 1 },
      { id: 24, menuName: '字典管理', menuType: 'C', path: '/system/dict', component: 'views/system/dict/index', icon: 'Collection', sort: 4, visible: 1, status: 1 },
      { id: 25, menuName: '操作日志', menuType: 'C', path: '/system/log', component: 'views/system/log/index', icon: 'Document', sort: 5, visible: 1, status: 1 },
    ],
  },
  {
    id: 3,
    menuName: '组织架构',
    menuType: 'M',
    path: '/org',
    icon: 'Grid',
    sort: 90,
    visible: 1,
    status: 1,
    children: [
      { id: 31, menuName: '部门管理', menuType: 'C', path: '/org/dept', component: 'views/org/dept/index', icon: 'OfficeBuilding', sort: 1, visible: 1, status: 1 },
      { id: 32, menuName: '岗位管理', menuType: 'C', path: '/org/position', component: 'views/org/position/index', icon: 'Stamp', sort: 2, visible: 1, status: 1 },
      { id: 33, menuName: '员工档案', menuType: 'C', path: '/org/employee', component: 'views/org/employee/index', icon: 'Avatar', sort: 3, visible: 1, status: 1 },
    ],
  },
]

const filterTree = (tree: any[], keyword: string): any[] => {
  return tree
    .map(node => {
      const matchSelf = node.menuName?.toLowerCase().includes(keyword.toLowerCase())
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
    menuName: '',
    menuType: parent ? 'C' : 'M',
    path: '',
    component: '',
    permission: '',
    icon: '',
    sort: 0,
    visible: 1,
    status: 1,
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, {
    id: row.id,
    parentId: row.parentId || null,
    menuName: row.menuName,
    menuType: row.menuType,
    path: row.path,
    component: row.component,
    permission: row.permission,
    icon: row.icon,
    sort: row.sort,
    visible: row.visible,
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
      await api.updateMenu(form.id, submitData)
      ElMessage.success('更新成功')
    } else {
      await api.createMenu(submitData)
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
    ElMessage.warning('该菜单下有子菜单，请先删除子菜单')
    return
  }

  ElMessageBox.confirm(
    `确定要删除菜单 "${row.menuName}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    await api.deleteMenu(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

onMounted(() => {
  loadData()
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

.menu-name {
  display: flex;
  align-items: center;
  gap: 6px;

  .el-icon {
    color: #409eff;
  }
}

.text-muted {
  color: #909399;
  font-size: 13px;
}
</style>
