<template>
  <div class="apply-container">
    <div class="page-header">
      <h2>发起申请</h2>
      <p class="subtitle">选择需要发起的审批流程</p>
    </div>

    <!-- 流程列表 -->
    <el-card class="process-card" shadow="never">
      <div class="process-grid">
        <div
          v-for="process in processList"
          :key="process.id"
          class="process-item"
          @click="handleStartProcess(process)"
        >
          <div class="process-icon" :style="{ background: getIconBg(process.category) }">
            <el-icon :size="28" color="#fff">
              <component :is="getIcon(process.category)" />
            </el-icon>
          </div>
          <div class="process-info">
            <h4>{{ process.processName }}</h4>
            <p>{{ process.description || '点击发起申请' }}</p>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 我发起的申请 -->
    <el-card class="my-apply-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>我发起的申请</span>
        </div>
      </template>

      <el-table :data="myApplyList" v-loading="loading" stripe>
        <el-table-column prop="processName" label="流程类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getProcessTagType(row.businessType)">{{ row.processName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="申请标题" min-width="200" />
        <el-table-column prop="currentTaskName" label="当前节点" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发起时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleViewDetail(row)">查看</el-button>
            <el-button
              v-if="row.status === 'running'"
              type="warning"
              size="small"
              @click="handleUrge(row)"
            >
              催办
            </el-button>
            <el-button
              v-if="row.status === 'running'"
              type="danger"
              size="small"
              @click="handleWithdraw(row)"
            >
              撤回
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next"
        @size-change="loadMyApply"
        @current-change="loadMyApply"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  Calendar,
  Money,
  Clock,
  Location,
  Position,
  Ticket,
  Document
} from '@element-plus/icons-vue';
import {
  getAvailableProcesses,
  getMyInitiatedProcesses,
  withdrawProcess,
  urgeProcess
} from '@/api/workflow';
import dayjs from 'dayjs';

const router = useRouter();
const loading = ref(false);
const processList = ref<any[]>([]);
const myApplyList = ref<any[]>([]);
const pagination = ref({ page: 1, size: 10, total: 0 });

const formatDateTime = (time: any) => {
  if (!time) return '-';
  return dayjs(time).format('YYYY-MM-DD HH:mm:ss');
};

const getIcon = (category: string) => {
  const map: Record<string, any> = {
    leave: Calendar,
    expense: Money,
    overtime: Clock,
    business_trip: Location,
    go_out: Position,
    card_replace: Ticket,
    general: Document
  };
  return map[category] || Document;
};

const getIconBg = (category: string) => {
  const map: Record<string, string> = {
    leave: 'linear-gradient(135deg, #67c23a 0%, #95d475 100%)',
    expense: 'linear-gradient(135deg, #e6a23c 0%, #f3d19e 100%)',
    overtime: 'linear-gradient(135deg, #409eff 0%, #79bbff 100%)',
    business_trip: 'linear-gradient(135deg, #909399 0%, #c0c4cc 100%)',
    go_out: 'linear-gradient(135deg, #9b59b6 0%, #c39bd3 100%)',
    card_replace: 'linear-gradient(135deg, #f56c6c 0%, #fab6b6 100%)',
    general: 'linear-gradient(135deg, #606266 0%, #909399 100%)'
  };
  return map[category] || 'linear-gradient(135deg, #606266 0%, #909399 100%)';
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
    draft: 'info',
    running: 'primary',
    completed: 'success',
    rejected: 'danger',
    cancelled: 'info'
  };
  return map[status] || '';
};

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    draft: '草稿',
    running: '审批中',
    completed: '已完成',
    rejected: '已驳回',
    cancelled: '已取消'
  };
  return map[status] || status;
};

const loadProcesses = async () => {
  try {
    const res: any = await getAvailableProcesses();
    processList.value = res.data || [];
  } catch (error) {
    console.error('加载可用流程失败', error);
  }
};

const loadMyApply = async () => {
  loading.value = true;
  try {
    const res: any = await getMyInitiatedProcesses({
      page: pagination.value.page,
      size: pagination.value.size
    });
    myApplyList.value = res.data?.records || [];
    pagination.value.total = res.data?.total || 0;
  } catch (error) {
    console.error('加载我发起的申请失败', error);
  } finally {
    loading.value = false;
  }
};

const handleStartProcess = (process: any) => {
  // 根据流程类型跳转到对应的申请页面
  const routeMap: Record<string, string> = {
    leave: '/workflow/leave/create',
    expense: '/workflow/expense/create',
    overtime: '/workflow/overtime/create',
    business_trip: '/workflow/business-trip/create',
    go_out: '/workflow/go-out/create',
    card_replace: '/workflow/card-replace/create',
    general: '/workflow/general/create'
  };
  const route = routeMap[process.category];
  if (route) {
    router.push(route);
  } else {
    ElMessage.info('该流程页面开发中');
  }
};

const handleViewDetail = (row: any) => {
  // 根据业务类型跳转到详情页
  const routeMap: Record<string, string> = {
    leave: '/workflow/leave',
    expense: '/workflow/expense'
  };
  const route = routeMap[row.businessType];
  if (route) {
    router.push(`${route}/${row.businessId}`);
  }
};

const handleUrge = async (row: any) => {
  try {
    await urgeProcess(row.processInstanceId);
    ElMessage.success('催办成功');
  } catch (error) {
    console.error('催办失败', error);
  }
};

const handleWithdraw = async (row: any) => {
  const { value } = await ElMessageBox.prompt('请输入撤回原因', '撤回申请', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputType: 'textarea',
    inputPlaceholder: '请输入撤回原因'
  });
  if (value) {
    try {
      await withdrawProcess(row.processInstanceId, value);
      ElMessage.success('撤回成功');
      loadMyApply();
    } catch (error) {
      console.error('撤回失败', error);
    }
  }
};

onMounted(() => {
  loadProcesses();
  loadMyApply();
});
</script>

<style scoped lang="scss">
.apply-container {
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

.process-card {
  margin-bottom: 20px;
}

.process-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.process-item {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 8px;
  background: #f5f7fa;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    background: #ecf5ff;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  .process-icon {
    width: 56px;
    height: 56px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;
    flex-shrink: 0;
  }
  
  .process-info {
    flex: 1;
    min-width: 0;
    
    h4 {
      margin: 0 0 4px;
      font-size: 16px;
      color: #303133;
    }
    
    p {
      margin: 0;
      font-size: 13px;
      color: #909399;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

.my-apply-card {
  .card-header {
    font-size: 16px;
    font-weight: 500;
  }
}
</style>
