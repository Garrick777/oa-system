<template>
  <div class="page-container">
    <el-card>
      <!-- 搜索栏 -->
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-input
            v-model="queryParams.username"
            placeholder="操作人"
            style="width: 120px"
            clearable
            @keyup.enter="handleSearch"
          />
          <el-input
            v-model="queryParams.module"
            placeholder="操作模块"
            style="width: 120px"
            clearable
            @keyup.enter="handleSearch"
          />
          <el-select v-model="queryParams.operType" placeholder="操作类型" style="width: 120px" clearable>
            <el-option label="新增" value="INSERT" />
            <el-option label="修改" value="UPDATE" />
            <el-option label="删除" value="DELETE" />
            <el-option label="查询" value="SELECT" />
            <el-option label="导入" value="IMPORT" />
            <el-option label="导出" value="EXPORT" />
            <el-option label="登录" value="LOGIN" />
            <el-option label="登出" value="LOGOUT" />
            <el-option label="其他" value="OTHER" />
          </el-select>
          <el-select v-model="queryParams.status" placeholder="状态" style="width: 100px" clearable>
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-button type="danger" plain @click="handleClear" :disabled="tableData.length === 0">
            <el-icon><Delete /></el-icon>清空日志
          </el-button>
          <el-button type="primary" plain @click="handleExport">
            <el-icon><Download /></el-icon>导出
          </el-button>
        </div>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="log-detail">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="请求方法">
                  <el-tag :type="getMethodType(row.requestMethod)" size="small">
                    {{ row.requestMethod }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="请求URL">{{ row.requestUrl }}</el-descriptions-item>
                <el-descriptions-item label="请求参数" :span="2">
                  <el-scrollbar max-height="100px">
                    <code class="code-block">{{ formatJson(row.requestParams) }}</code>
                  </el-scrollbar>
                </el-descriptions-item>
                <el-descriptions-item label="返回结果" :span="2">
                  <el-scrollbar max-height="100px">
                    <code class="code-block">{{ formatJson(row.responseData) }}</code>
                  </el-scrollbar>
                </el-descriptions-item>
                <el-descriptions-item v-if="row.errorMsg" label="错误信息" :span="2">
                  <span class="error-msg">{{ row.errorMsg }}</span>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="id" label="日志ID" width="80" />
        <el-table-column prop="module" label="操作模块" width="120" />
        <el-table-column prop="operType" label="操作类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getOperTypeTag(row.operType)" size="small">
              {{ getOperTypeText(row.operType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="username" label="操作人" width="100" />
        <el-table-column prop="deptName" label="部门" width="120" />
        <el-table-column prop="ip" label="操作IP" width="130" />
        <el-table-column prop="location" label="操作地点" width="120" show-overflow-tooltip />
        <el-table-column prop="browser" label="浏览器" width="100" show-overflow-tooltip />
        <el-table-column prop="costTime" label="耗时" width="80" align="right">
          <template #default="{ row }">
            <span :class="getCostTimeClass(row.costTime)">{{ row.costTime }}ms</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operTime" label="操作时间" width="170" />
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const dateRange = ref<string[]>([])

const queryParams = reactive({
  current: 1,
  size: 10,
  username: '',
  module: '',
  operType: '',
  status: undefined as number | undefined,
  startTime: '',
  endTime: '',
})

// 监听日期范围变化
watch(dateRange, (val) => {
  if (val && val.length === 2) {
    queryParams.startTime = val[0]
    queryParams.endTime = val[1]
  } else {
    queryParams.startTime = ''
    queryParams.endTime = ''
  }
})

const getOperTypeText = (type: string) => {
  const texts: Record<string, string> = {
    INSERT: '新增',
    UPDATE: '修改',
    DELETE: '删除',
    SELECT: '查询',
    IMPORT: '导入',
    EXPORT: '导出',
    LOGIN: '登录',
    LOGOUT: '登出',
    OTHER: '其他',
  }
  return texts[type] || type
}

const getOperTypeTag = (type: string) => {
  const types: Record<string, string> = {
    INSERT: 'success',
    UPDATE: 'primary',
    DELETE: 'danger',
    SELECT: 'info',
    IMPORT: 'warning',
    EXPORT: 'warning',
    LOGIN: 'success',
    LOGOUT: 'info',
    OTHER: '',
  }
  return types[type] || ''
}

const getMethodType = (method: string) => {
  const types: Record<string, string> = {
    GET: 'info',
    POST: 'success',
    PUT: 'warning',
    DELETE: 'danger',
    PATCH: 'primary',
  }
  return types[method] || ''
}

const getCostTimeClass = (time: number) => {
  if (time < 100) return 'cost-fast'
  if (time < 500) return 'cost-normal'
  return 'cost-slow'
}

const formatJson = (str: string) => {
  if (!str) return '-'
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/system/logs', { params: queryParams })
    tableData.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
    // 使用模拟数据
    tableData.value = getMockData()
    total.value = tableData.value.length
  } finally {
    loading.value = false
  }
}

// 模拟数据
const getMockData = () => [
  {
    id: 1,
    module: '系统管理',
    operType: 'LOGIN',
    description: '用户登录',
    username: 'admin',
    deptName: '技术部',
    ip: '192.168.1.100',
    location: '北京市',
    browser: 'Chrome',
    requestMethod: 'POST',
    requestUrl: '/auth/login',
    requestParams: '{"username":"admin","password":"******"}',
    responseData: '{"code":0,"msg":"success"}',
    costTime: 45,
    status: 1,
    operTime: '2026-01-20 09:15:23',
  },
  {
    id: 2,
    module: '用户管理',
    operType: 'INSERT',
    description: '新增用户：张三',
    username: 'admin',
    deptName: '技术部',
    ip: '192.168.1.100',
    location: '北京市',
    browser: 'Chrome',
    requestMethod: 'POST',
    requestUrl: '/system/users',
    requestParams: '{"username":"zhangsan","realName":"张三","roleId":7}',
    responseData: '{"code":0,"msg":"success","data":{"id":10}}',
    costTime: 123,
    status: 1,
    operTime: '2026-01-20 10:30:45',
  },
  {
    id: 3,
    module: '部门管理',
    operType: 'UPDATE',
    description: '修改部门：技术部',
    username: 'admin',
    deptName: '技术部',
    ip: '192.168.1.100',
    location: '北京市',
    browser: 'Chrome',
    requestMethod: 'PUT',
    requestUrl: '/org/depts/1',
    requestParams: '{"deptName":"技术部","leaderId":5}',
    responseData: '{"code":0,"msg":"success"}',
    costTime: 89,
    status: 1,
    operTime: '2026-01-20 11:20:15',
  },
  {
    id: 4,
    module: '用户管理',
    operType: 'DELETE',
    description: '删除用户失败：用户不存在',
    username: 'admin',
    deptName: '技术部',
    ip: '192.168.1.100',
    location: '北京市',
    browser: 'Chrome',
    requestMethod: 'DELETE',
    requestUrl: '/system/users/999',
    requestParams: '{}',
    responseData: '{"code":500,"msg":"用户不存在"}',
    errorMsg: '用户不存在',
    costTime: 35,
    status: 0,
    operTime: '2026-01-20 14:05:33',
  },
]

const handleSearch = () => {
  queryParams.current = 1
  loadData()
}

const handleReset = () => {
  queryParams.username = ''
  queryParams.module = ''
  queryParams.operType = ''
  queryParams.status = undefined
  dateRange.value = []
  handleSearch()
}

const handleClear = () => {
  ElMessageBox.confirm(
    '确定要清空所有操作日志吗？此操作不可恢复！',
    '警告',
    {
      confirmButtonText: '确定清空',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await request.delete('/system/logs/clear')
      ElMessage.success('日志已清空')
      loadData()
    } catch (error) {
      console.error('清空失败:', error)
    }
  })
}

const handleExport = async () => {
  try {
    ElMessage.info('正在导出...')
    const res = await request.get('/system/logs/export', {
      params: queryParams,
      responseType: 'blob',
    })
    const blob = new Blob([res], { type: 'application/vnd.ms-excel' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `操作日志_${new Date().toLocaleDateString()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    ElMessage.warning('导出功能暂不可用')
  }
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

.table-pagination {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.log-detail {
  padding: 10px 20px;
  background: #f5f7fa;
}

.code-block {
  display: block;
  padding: 8px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}

.error-msg {
  color: #f56c6c;
}

.cost-fast {
  color: #67c23a;
  font-weight: 500;
}

.cost-normal {
  color: #e6a23c;
  font-weight: 500;
}

.cost-slow {
  color: #f56c6c;
  font-weight: 500;
}
</style>
