<template>
  <div class="page-container">
    <el-row :gutter="20">
      <!-- 左侧：字典类型列表 -->
      <el-col :span="10">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>字典类型</span>
              <el-button type="primary" size="small" @click="handleAddType">
                <el-icon><Plus /></el-icon>新增
              </el-button>
            </div>
          </template>

          <!-- 搜索 -->
          <div class="search-bar">
            <el-input
              v-model="typeSearch"
              placeholder="字典名称/类型"
              clearable
              @keyup.enter="loadTypes"
            >
              <template #append>
                <el-button @click="loadTypes">
                  <el-icon><Search /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>

          <!-- 字典类型表格 -->
          <el-table
            :data="typeList"
            v-loading="typeLoading"
            @row-click="handleTypeClick"
            highlight-current-row
            :row-class-name="getTypeRowClass"
            border
            max-height="500"
          >
            <el-table-column prop="dictName" label="字典名称" min-width="120" />
            <el-table-column prop="dictType" label="字典类型" min-width="140" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="70" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click.stop="handleEditType(row)">
                  编辑
                </el-button>
                <el-button link type="danger" size="small" @click.stop="handleDeleteType(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 右侧：字典数据列表 -->
      <el-col :span="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>
                字典数据
                <el-tag v-if="currentType" type="info" size="small" style="margin-left: 8px;">
                  {{ currentType.dictName }}
                </el-tag>
              </span>
              <el-button
                type="primary"
                size="small"
                :disabled="!currentType"
                @click="handleAddData"
              >
                <el-icon><Plus /></el-icon>新增
              </el-button>
            </div>
          </template>

          <!-- 提示 -->
          <div v-if="!currentType" class="empty-tip">
            <el-empty description="请先在左侧选择一个字典类型" />
          </div>

          <!-- 字典数据表格 -->
          <el-table v-else :data="dataList" v-loading="dataLoading" border>
            <el-table-column prop="dictLabel" label="字典标签" min-width="120" />
            <el-table-column prop="dictValue" label="字典值" width="100" />
            <el-table-column prop="tagType" label="标签样式" width="100" align="center">
              <template #default="{ row }">
                <el-tag v-if="row.tagType" :type="row.tagType" size="small">
                  {{ row.dictLabel }}
                </el-tag>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column prop="sort" label="排序" width="70" align="center" />
            <el-table-column prop="status" label="状态" width="70" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
                  {{ row.status === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
            <el-table-column label="操作" width="120" align="center">
              <template #default="{ row }">
                <el-button link type="primary" size="small" @click="handleEditData(row)">
                  编辑
                </el-button>
                <el-button link type="danger" size="small" @click="handleDeleteData(row)">
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 字典类型弹窗 -->
    <el-dialog
      v-model="typeDialogVisible"
      :title="typeForm.id ? '编辑字典类型' : '新增字典类型'"
      width="500px"
      destroy-on-close
    >
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="90px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input
            v-model="typeForm.dictType"
            :disabled="!!typeForm.id"
            placeholder="请输入字典类型，如：sys_user_sex"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="typeForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="typeForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="typeSubmitLoading" @click="handleTypeSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 字典数据弹窗 -->
    <el-dialog
      v-model="dataDialogVisible"
      :title="dataForm.id ? '编辑字典数据' : '新增字典数据'"
      width="500px"
      destroy-on-close
    >
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="90px">
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="请输入字典标签" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="请输入字典值" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="标签样式">
              <el-select v-model="dataForm.tagType" clearable placeholder="选择样式" style="width: 100%">
                <el-option label="默认(default)" value="" />
                <el-option label="主要(primary)" value="primary">
                  <el-tag type="primary" size="small">primary</el-tag>
                </el-option>
                <el-option label="成功(success)" value="success">
                  <el-tag type="success" size="small">success</el-tag>
                </el-option>
                <el-option label="警告(warning)" value="warning">
                  <el-tag type="warning" size="small">warning</el-tag>
                </el-option>
                <el-option label="危险(danger)" value="danger">
                  <el-tag type="danger" size="small">danger</el-tag>
                </el-option>
                <el-option label="信息(info)" value="info">
                  <el-tag type="info" size="small">info</el-tag>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排序">
              <el-input-number v-model="dataForm.sort" :min="0" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-radio-group v-model="dataForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="dataForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="dataSubmitLoading" @click="handleDataSubmit">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import * as api from '@/api/system'

// 字典类型相关
const typeLoading = ref(false)
const typeSubmitLoading = ref(false)
const typeList = ref<any[]>([])
const typeDialogVisible = ref(false)
const typeFormRef = ref<FormInstance>()
const typeSearch = ref('')
const currentType = ref<any>(null)

const typeForm = reactive({
  id: undefined as number | undefined,
  dictName: '',
  dictType: '',
  status: 1,
  remark: '',
})

const typeRules: FormRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [
    { required: true, message: '请输入字典类型', trigger: 'blur' },
    { pattern: /^[a-z][a-z0-9_]*$/, message: '字典类型必须以小写字母开头，只能包含小写字母、数字和下划线', trigger: 'blur' },
  ],
}

// 字典数据相关
const dataLoading = ref(false)
const dataSubmitLoading = ref(false)
const dataList = ref<any[]>([])
const dataDialogVisible = ref(false)
const dataFormRef = ref<FormInstance>()

const dataForm = reactive({
  id: undefined as number | undefined,
  dictType: '',
  dictLabel: '',
  dictValue: '',
  tagType: '',
  sort: 0,
  status: 1,
  remark: '',
})

const dataRules: FormRules = {
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
}

// 当前选中行样式
const getTypeRowClass = ({ row }: { row: any }) => {
  return currentType.value?.id === row.id ? 'current-row' : ''
}

// 加载字典类型
const loadTypes = async () => {
  typeLoading.value = true
  try {
    const res = await api.getDictTypes({ keyword: typeSearch.value })
    typeList.value = res.records || res.data || []
  } catch (error) {
    console.error('加载字典类型失败:', error)
    // 使用默认数据
    typeList.value = getDefaultTypes()
  } finally {
    typeLoading.value = false
  }
}

// 默认字典类型数据
const getDefaultTypes = () => [
  { id: 1, dictName: '用户性别', dictType: 'sys_user_sex', status: 1, remark: '用户性别列表' },
  { id: 2, dictName: '系统状态', dictType: 'sys_common_status', status: 1, remark: '系统通用状态' },
  { id: 3, dictName: '是否', dictType: 'sys_yes_no', status: 1, remark: '是否选项' },
  { id: 4, dictName: '审批状态', dictType: 'wf_approve_status', status: 1, remark: '工作流审批状态' },
  { id: 5, dictName: '请假类型', dictType: 'biz_leave_type', status: 1, remark: '请假申请类型' },
]

// 加载字典数据
const loadData = async () => {
  if (!currentType.value) return

  dataLoading.value = true
  try {
    const res = await api.getDictDataList({ dictType: currentType.value.dictType })
    dataList.value = res.records || res.data || []
  } catch (error) {
    console.error('加载字典数据失败:', error)
    // 使用默认数据
    dataList.value = getDefaultData(currentType.value.dictType)
  } finally {
    dataLoading.value = false
  }
}

// 默认字典数据
const getDefaultData = (dictType: string) => {
  const mockData: Record<string, any[]> = {
    sys_user_sex: [
      { id: 1, dictLabel: '男', dictValue: '1', tagType: 'primary', sort: 1, status: 1 },
      { id: 2, dictLabel: '女', dictValue: '2', tagType: 'danger', sort: 2, status: 1 },
      { id: 3, dictLabel: '未知', dictValue: '0', tagType: 'info', sort: 3, status: 1 },
    ],
    sys_common_status: [
      { id: 4, dictLabel: '启用', dictValue: '1', tagType: 'success', sort: 1, status: 1 },
      { id: 5, dictLabel: '禁用', dictValue: '0', tagType: 'danger', sort: 2, status: 1 },
    ],
    sys_yes_no: [
      { id: 6, dictLabel: '是', dictValue: '1', tagType: 'success', sort: 1, status: 1 },
      { id: 7, dictLabel: '否', dictValue: '0', tagType: 'danger', sort: 2, status: 1 },
    ],
    wf_approve_status: [
      { id: 8, dictLabel: '待审批', dictValue: '0', tagType: 'warning', sort: 1, status: 1 },
      { id: 9, dictLabel: '审批中', dictValue: '1', tagType: 'primary', sort: 2, status: 1 },
      { id: 10, dictLabel: '已通过', dictValue: '2', tagType: 'success', sort: 3, status: 1 },
      { id: 11, dictLabel: '已驳回', dictValue: '3', tagType: 'danger', sort: 4, status: 1 },
      { id: 12, dictLabel: '已撤销', dictValue: '4', tagType: 'info', sort: 5, status: 1 },
    ],
    biz_leave_type: [
      { id: 13, dictLabel: '年假', dictValue: 'annual', tagType: 'success', sort: 1, status: 1 },
      { id: 14, dictLabel: '事假', dictValue: 'personal', tagType: 'primary', sort: 2, status: 1 },
      { id: 15, dictLabel: '病假', dictValue: 'sick', tagType: 'warning', sort: 3, status: 1 },
      { id: 16, dictLabel: '婚假', dictValue: 'marriage', tagType: 'danger', sort: 4, status: 1 },
      { id: 17, dictLabel: '产假', dictValue: 'maternity', tagType: 'info', sort: 5, status: 1 },
    ],
  }
  return mockData[dictType] || []
}

// 选中字典类型
const handleTypeClick = (row: any) => {
  currentType.value = row
  loadData()
}

// 新增字典类型
const handleAddType = () => {
  Object.assign(typeForm, {
    id: undefined,
    dictName: '',
    dictType: '',
    status: 1,
    remark: '',
  })
  typeDialogVisible.value = true
}

// 编辑字典类型
const handleEditType = (row: any) => {
  Object.assign(typeForm, {
    id: row.id,
    dictName: row.dictName,
    dictType: row.dictType,
    status: row.status,
    remark: row.remark,
  })
  typeDialogVisible.value = true
}

// 提交字典类型
const handleTypeSubmit = async () => {
  const valid = await typeFormRef.value?.validate().catch(() => false)
  if (!valid) return

  typeSubmitLoading.value = true
  try {
    if (typeForm.id) {
      await api.updateDictType(typeForm.id, typeForm)
      ElMessage.success('更新成功')
    } else {
      await api.createDictType(typeForm)
      ElMessage.success('创建成功')
    }
    typeDialogVisible.value = false
    loadTypes()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    typeSubmitLoading.value = false
  }
}

// 删除字典类型
const handleDeleteType = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除字典类型 "${row.dictName}" 吗？删除后，该类型下的所有字典数据将一并删除！`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    await api.deleteDictType(row.id)
    ElMessage.success('删除成功')
    if (currentType.value?.id === row.id) {
      currentType.value = null
      dataList.value = []
    }
    loadTypes()
  })
}

// 新增字典数据
const handleAddData = () => {
  if (!currentType.value) return

  Object.assign(dataForm, {
    id: undefined,
    dictType: currentType.value.dictType,
    dictLabel: '',
    dictValue: '',
    tagType: '',
    sort: 0,
    status: 1,
    remark: '',
  })
  dataDialogVisible.value = true
}

// 编辑字典数据
const handleEditData = (row: any) => {
  Object.assign(dataForm, {
    id: row.id,
    dictType: currentType.value.dictType,
    dictLabel: row.dictLabel,
    dictValue: row.dictValue,
    tagType: row.tagType,
    sort: row.sort,
    status: row.status,
    remark: row.remark,
  })
  dataDialogVisible.value = true
}

// 提交字典数据
const handleDataSubmit = async () => {
  const valid = await dataFormRef.value?.validate().catch(() => false)
  if (!valid) return

  dataSubmitLoading.value = true
  try {
    if (dataForm.id) {
      await api.updateDictData(dataForm.id, dataForm)
      ElMessage.success('更新成功')
    } else {
      await api.createDictData(dataForm)
      ElMessage.success('创建成功')
    }
    dataDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
  } finally {
    dataSubmitLoading.value = false
  }
}

// 删除字典数据
const handleDeleteData = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除字典数据 "${row.dictLabel}" 吗？`,
    '删除确认',
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    await api.deleteDictData(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

onMounted(() => {
  loadTypes()
})
</script>

<style scoped lang="scss">
.page-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-bar {
  margin-bottom: 16px;
}

.empty-tip {
  padding: 40px 0;
}

:deep(.el-table) {
  .current-row {
    background-color: #ecf5ff !important;
  }

  .el-table__row {
    cursor: pointer;
  }
}
</style>
