<template>
  <div class="page-container">
    <el-card>
      <!-- 搜索栏 -->
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="queryParams.positionName"
            placeholder="岗位名称"
            style="width: 150px"
            clearable
            @keyup.enter="handleSearch"
          />
          <el-input
            v-model="queryParams.positionCode"
            placeholder="岗位编码"
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
            <el-icon><Plus /></el-icon>新增岗位
          </el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="positionName" label="岗位名称" width="150" />
        <el-table-column prop="positionCode" label="岗位编码" width="120" />
        <el-table-column prop="level" label="职级" width="80">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="岗位名称" prop="positionName">
          <el-input v-model="form.positionName" placeholder="请输入岗位名称" />
        </el-form-item>
        <el-form-item label="岗位编码" prop="positionCode">
          <el-input v-model="form.positionCode" placeholder="请输入岗位编码" />
        </el-form-item>
        <el-form-item label="职级" prop="level">
          <el-input-number v-model="form.level" :min="1" :max="20" style="width: 100%" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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

const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

const queryParams = reactive({
  current: 1,
  size: 10,
  positionName: '',
  positionCode: '',
  status: undefined as number | undefined,
})

const form = reactive({
  id: undefined as number | undefined,
  positionName: '',
  positionCode: '',
  level: 1,
  sort: 0,
  status: 1,
  remark: '',
})

const dialogTitle = computed(() => (form.id ? '编辑岗位' : '新增岗位'))

const rules: FormRules = {
  positionName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  positionCode: [{ required: true, message: '请输入岗位编码', trigger: 'blur' }],
}

const getLevelType = (level: number) => {
  if (level >= 9) return 'danger'
  if (level >= 7) return 'warning'
  if (level >= 5) return 'primary'
  return 'info'
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await api.getPositions(queryParams)
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryParams.current = 1
  loadData()
}

const handleReset = () => {
  queryParams.positionName = ''
  queryParams.positionCode = ''
  queryParams.status = undefined
  handleSearch()
}

const handleAdd = () => {
  Object.assign(form, {
    id: undefined,
    positionName: '',
    positionCode: '',
    level: 1,
    sort: 0,
    status: 1,
    remark: '',
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  Object.assign(form, {
    id: row.id,
    positionName: row.positionName,
    positionCode: row.positionCode,
    level: row.level,
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
      await api.updatePosition(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await api.createPosition(form)
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
    await api.updatePosition(row.id, { status: row.status })
    ElMessage.success('状态更新成功')
  } catch (error) {
    row.status = row.status === 1 ? 0 : 1
  }
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm(`确定要删除岗位 "${row.positionName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await api.deletePosition(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

onMounted(() => {
  loadData()
})
</script>
