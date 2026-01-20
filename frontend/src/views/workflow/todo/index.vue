<template>
  <div class="todo-container">
    <div class="page-header">
      <h2>待办任务</h2>
      <p class="subtitle">处理待审批的工作事项</p>
    </div>

    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="流程类型">
          <el-select v-model="searchForm.category" placeholder="全部类型" clearable style="width: 150px">
            <el-option label="请假申请" value="leave" />
            <el-option label="报销申请" value="expense" />
            <el-option label="加班申请" value="overtime" />
            <el-option label="出差申请" value="business_trip" />
            <el-option label="通用审批" value="general" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="搜索标题" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 待办列表 -->
    <el-card class="list-card" shadow="never">
      <el-table :data="todoList" v-loading="loading" stripe>
        <el-table-column prop="processName" label="流程类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getProcessTagType(row.businessType)">{{ row.processName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="申请标题" min-width="200">
          <template #default="{ row }">
            <el-link type="primary" @click="handleView(row)">{{ row.title }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="initiatorName" label="发起人" width="100" />
        <el-table-column prop="taskName" label="当前节点" width="120" />
        <el-table-column prop="createTime" label="到达时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleApprove(row)">
              审批
            </el-button>
            <el-button type="info" size="small" @click="handleView(row)">
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>

    <!-- 审批弹窗 -->
    <el-dialog v-model="approvalDialogVisible" title="审批" width="600px">
      <el-form :model="approvalForm" label-width="100px">
        <el-form-item label="申请标题">
          <span class="form-text">{{ currentTask?.title }}</span>
        </el-form-item>
        <el-form-item label="发起人">
          <span class="form-text">{{ currentTask?.initiatorName }}</span>
        </el-form-item>
        <el-form-item label="当前节点">
          <span class="form-text">{{ currentTask?.taskName }}</span>
        </el-form-item>
        <el-form-item label="审批意见" required>
          <el-input
            v-model="approvalForm.comment"
            type="textarea"
            :rows="4"
            placeholder="请输入审批意见"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="approvalDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="handleReject">驳回</el-button>
        <el-button type="warning" @click="handleReturn">退回</el-button>
        <el-button type="primary" @click="handlePass">通过</el-button>
      </template>
    </el-dialog>

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" title="流程详情" size="600px">
      <div v-if="currentDetail" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="申请标题">{{ currentDetail?.title }}</el-descriptions-item>
          <el-descriptions-item label="流程类型">{{ currentDetail?.processName }}</el-descriptions-item>
          <el-descriptions-item label="发起人">{{ currentDetail?.initiatorName }}</el-descriptions-item>
          <el-descriptions-item label="发起时间">{{ formatDateTime(currentDetail?.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="当前节点">{{ currentDetail?.taskName }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 20px 0 10px;">审批记录</h4>
        <el-timeline>
          <el-timeline-item
            v-for="record in approvalRecords"
            :key="record.id"
            :timestamp="formatDateTime(record.approvalTime)"
            :type="getRecordType(record.action)"
          >
            <p><strong>{{ record.assigneeName }}</strong> {{ getActionText(record.action) }}</p>
            <p v-if="record.comment" class="record-comment">{{ record.comment }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { Search, Refresh } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  getTodoTasks,
  approveTask,
  rejectTask,
  returnTask,
  getApprovalRecords
} from '@/api/workflow';
import dayjs from 'dayjs';

const loading = ref(false);
const todoList = ref<any[]>([]);
const pagination = ref({ page: 1, size: 10, total: 0 });
const searchForm = ref({ category: '', keyword: '' });

const approvalDialogVisible = ref(false);
const detailDrawerVisible = ref(false);
const currentTask = ref<any>(null);
const currentDetail = ref<any>(null);
const approvalRecords = ref<any[]>([]);
const approvalForm = ref({ comment: '' });

const formatDateTime = (time: any) => {
  if (!time) return '-';
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss');
};

const getProcessTagType = (type: string) => {
  const map: Record<string, string> = {
    leave: 'success',
    expense: 'warning',
    overtime: 'info',
    business_trip: 'primary',
    general: ''
  };
  return map[type] || '';
};

const getRecordType = (action: string) => {
  const map: Record<string, string> = {
    submit: 'primary',
    approve: 'success',
    reject: 'danger',
    return: 'warning',
    delegate: 'info',
    withdraw: 'info',
    urge: 'warning'
  };
  return map[action] || 'primary';
};

const getActionText = (action: string) => {
  const map: Record<string, string> = {
    submit: '提交申请',
    approve: '审批通过',
    reject: '驳回申请',
    return: '退回修改',
    delegate: '转办',
    withdraw: '撤回',
    urge: '催办'
  };
  return map[action] || action;
};

const loadData = async () => {
  loading.value = true;
  try {
    const res: any = await getTodoTasks({
      page: pagination.value.page,
      size: pagination.value.size,
      ...searchForm.value
    });
    todoList.value = res.data?.records || [];
    pagination.value.total = res.data?.total || 0;
  } catch (error) {
    console.error('加载待办任务失败', error);
  } finally {
    loading.value = false;
  }
};

const handleSearch = () => {
  pagination.value.page = 1;
  loadData();
};

const handleReset = () => {
  searchForm.value = { category: '', keyword: '' };
  handleSearch();
};

const handleApprove = (row: any) => {
  currentTask.value = row;
  approvalForm.value = { comment: '' };
  approvalDialogVisible.value = true;
};

const handleView = async (row: any) => {
  currentDetail.value = row;
  detailDrawerVisible.value = true;
  try {
    const res: any = await getApprovalRecords(row.processInstanceId);
    approvalRecords.value = res.data || [];
  } catch (error) {
    console.error('加载审批记录失败', error);
  }
};

const handlePass = async () => {
  if (!approvalForm.value.comment) {
    ElMessage.warning('请输入审批意见');
    return;
  }
  try {
    await approveTask(currentTask.value.taskId, { comment: approvalForm.value.comment });
    ElMessage.success('审批通过');
    approvalDialogVisible.value = false;
    loadData();
  } catch (error) {
    console.error('审批失败', error);
  }
};

const handleReject = async () => {
  if (!approvalForm.value.comment) {
    ElMessage.warning('请输入驳回原因');
    return;
  }
  await ElMessageBox.confirm('确定要驳回该申请吗？', '提示', { type: 'warning' });
  try {
    await rejectTask(currentTask.value.taskId, approvalForm.value.comment);
    ElMessage.success('已驳回');
    approvalDialogVisible.value = false;
    loadData();
  } catch (error) {
    console.error('驳回失败', error);
  }
};

const handleReturn = async () => {
  if (!approvalForm.value.comment) {
    ElMessage.warning('请输入退回原因');
    return;
  }
  try {
    await returnTask(currentTask.value.taskId, approvalForm.value.comment);
    ElMessage.success('已退回');
    approvalDialogVisible.value = false;
    loadData();
  } catch (error) {
    console.error('退回失败', error);
  }
};

onMounted(() => {
  loadData();
});
</script>

<style scoped lang="scss">
.todo-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  
  h2 {
    margin: 0 0 8px;
    font-size: 24px;
    color: #303133;
  }
  
  .subtitle {
    margin: 0;
    color: #909399;
    font-size: 14px;
  }
}

.search-card {
  margin-bottom: 20px;
}

.list-card {
  :deep(.el-card__body) {
    padding: 20px;
  }
}

.form-text {
  color: #606266;
}

.detail-content {
  padding: 0 20px;
}

.record-comment {
  margin: 5px 0 0;
  color: #909399;
  font-size: 13px;
}
</style>
