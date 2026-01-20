<template>
  <div class="done-container">
    <div class="page-header">
      <h2>已办任务</h2>
      <p class="subtitle">查看已处理的审批记录</p>
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

    <!-- 已办列表 -->
    <el-card class="list-card" shadow="never">
      <el-table :data="doneList" v-loading="loading" stripe>
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
        <el-table-column prop="taskName" label="处理节点" width="120" />
        <el-table-column prop="status" label="流程状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="处理时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.endTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">
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

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailDrawerVisible" title="流程详情" size="600px">
      <div v-if="currentDetail" class="detail-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="申请标题">{{ currentDetail?.title }}</el-descriptions-item>
          <el-descriptions-item label="流程类型">{{ currentDetail?.processName }}</el-descriptions-item>
          <el-descriptions-item label="发起人">{{ currentDetail?.initiatorName }}</el-descriptions-item>
          <el-descriptions-item label="发起时间">{{ formatDateTime(currentDetail?.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="处理节点">{{ currentDetail?.taskName }}</el-descriptions-item>
          <el-descriptions-item label="处理时间">{{ formatDateTime(currentDetail?.endTime) }}</el-descriptions-item>
          <el-descriptions-item label="流程状态">
            <el-tag :type="getStatusType(currentDetail?.status)">{{ getStatusText(currentDetail?.status) }}</el-tag>
          </el-descriptions-item>
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
import { getDoneTasks, getApprovalRecords } from '@/api/workflow';
import dayjs from 'dayjs';

const loading = ref(false);
const doneList = ref<any[]>([]);
const pagination = ref({ page: 1, size: 10, total: 0 });
const searchForm = ref({ category: '', keyword: '' });

const detailDrawerVisible = ref(false);
const currentDetail = ref<any>(null);
const approvalRecords = ref<any[]>([]);

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

const getStatusType = (status: string) => {
  const map: Record<string, string> = {
    running: 'primary',
    completed: 'success',
    rejected: 'danger',
    cancelled: 'info'
  };
  return map[status] || '';
};

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    running: '审批中',
    completed: '已完成',
    rejected: '已驳回',
    cancelled: '已取消'
  };
  return map[status] || status;
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
    const res: any = await getDoneTasks({
      page: pagination.value.page,
      size: pagination.value.size,
      ...searchForm.value
    });
    doneList.value = res.data?.records || [];
    pagination.value.total = res.data?.total || 0;
  } catch (error) {
    console.error('加载已办任务失败', error);
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

onMounted(() => {
  loadData();
});
</script>

<style scoped lang="scss">
.done-container {
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

.detail-content {
  padding: 0 20px;
}

.record-comment {
  margin: 5px 0 0;
  color: #909399;
  font-size: 13px;
}
</style>
