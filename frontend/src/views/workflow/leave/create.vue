<template>
  <div class="leave-create-container">
    <div class="page-header">
      <el-button @click="$router.back()" class="back-btn">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2>请假申请</h2>
    </div>

    <el-card class="form-card" shadow="never">
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        style="max-width: 800px"
      >
        <el-form-item label="请假类型" prop="leaveType">
          <el-select v-model="formData.leaveType" placeholder="请选择请假类型" style="width: 100%">
            <el-option label="年假" value="annual" />
            <el-option label="病假" value="sick" />
            <el-option label="事假" value="personal" />
            <el-option label="婚假" value="marriage" />
            <el-option label="产假" value="maternity" />
            <el-option label="陪产假" value="paternity" />
            <el-option label="丧假" value="bereavement" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>

        <el-form-item label="请假时间" prop="timeRange">
          <el-date-picker
            v-model="formData.timeRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%"
            @change="handleTimeChange"
          />
        </el-form-item>

        <el-form-item label="请假时长">
          <el-input v-model="formData.durationText" disabled style="width: 200px" />
        </el-form-item>

        <el-form-item label="请假事由" prop="reason">
          <el-input
            v-model="formData.reason"
            type="textarea"
            :rows="4"
            placeholder="请详细说明请假原因"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="工作交接人" prop="handoverUserId">
          <el-select
            v-model="formData.handoverUserId"
            placeholder="请选择工作交接人"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.realName"
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="交接说明">
          <el-input
            v-model="formData.handoverRemark"
            type="textarea"
            :rows="2"
            placeholder="请说明工作交接事项（可选）"
          />
        </el-form-item>

        <el-form-item label="附件">
          <el-upload
            class="upload-area"
            drag
            action="#"
            :auto-upload="false"
            :file-list="fileList"
            @change="handleFileChange"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">
              拖拽文件到此处或 <em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持上传jpg/png/pdf文件，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button type="default" @click="handleSaveDraft" :loading="saving">
            保存草稿
          </el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            提交申请
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 假期余额 -->
    <el-card class="balance-card" shadow="never">
      <template #header>
        <span>假期余额</span>
      </template>
      <div class="balance-grid">
        <div class="balance-item">
          <span class="label">年假</span>
          <span class="value">{{ leaveBalance.annual?.remaining || 0 }} 天</span>
        </div>
        <div class="balance-item">
          <span class="label">病假</span>
          <span class="value">{{ leaveBalance.sick?.remaining || 0 }} 天</span>
        </div>
        <div class="balance-item">
          <span class="label">事假</span>
          <span class="value">{{ leaveBalance.personal?.remaining || 0 }} 天</span>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ArrowLeft, UploadFilled } from '@element-plus/icons-vue';
import { ElMessage } from 'element-plus';
import type { FormInstance, FormRules, UploadFile } from 'element-plus';
import {
  createLeaveApplication,
  submitLeaveApplication,
  getLeaveBalance,
  calculateLeaveDuration
} from '@/api/workflow';
import { getSimpleUserList } from '@/api/system';

const router = useRouter();
const formRef = ref<FormInstance>();
const saving = ref(false);
const submitting = ref(false);
const userList = ref<any[]>([]);
const fileList = ref<UploadFile[]>([]);
const leaveBalance = ref<any>({});

const formData = reactive({
  leaveType: '',
  timeRange: null as string[] | null,
  startTime: '',
  endTime: '',
  duration: 0,
  durationText: '',
  reason: '',
  handoverUserId: null as number | null,
  handoverRemark: '',
  attachments: ''
});

const formRules: FormRules = {
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  timeRange: [{ required: true, message: '请选择请假时间', trigger: 'change' }],
  reason: [{ required: true, message: '请输入请假事由', trigger: 'blur' }]
};

const handleTimeChange = async (value: string[] | null) => {
  if (value && value.length === 2) {
    formData.startTime = value[0];
    formData.endTime = value[1];
    try {
      const res: any = await calculateLeaveDuration(value[0], value[1]);
      formData.duration = res.data || 0;
      const hours = formData.duration;
      if (hours >= 8) {
        const days = Math.floor(hours / 8);
        const remainHours = hours % 8;
        formData.durationText = remainHours > 0 ? `${days}天${remainHours}小时` : `${days}天`;
      } else {
        formData.durationText = `${hours}小时`;
      }
    } catch (error) {
      console.error('计算请假时长失败', error);
    }
  } else {
    formData.startTime = '';
    formData.endTime = '';
    formData.duration = 0;
    formData.durationText = '';
  }
};

const handleFileChange = (file: UploadFile) => {
  // 处理文件变化
};

const loadUserList = async () => {
  try {
    const res: any = await getSimpleUserList({ page: 1, size: 100 });
    // 响应拦截器直接返回response.data，所以直接取records
    userList.value = res.records || res.data?.records || [];
  } catch (error) {
    console.error('加载用户列表失败', error);
  }
};

const loadLeaveBalance = async () => {
  try {
    const res: any = await getLeaveBalance();
    leaveBalance.value = res.data || {};
  } catch (error) {
    console.error('加载假期余额失败', error);
  }
};

const handleSaveDraft = async () => {
  saving.value = true;
  try {
    const handoverUser = userList.value.find(u => u.id === formData.handoverUserId);
    const data = {
      leaveType: formData.leaveType,
      startTime: formData.startTime,
      endTime: formData.endTime,
      duration: formData.duration,
      reason: formData.reason,
      handoverUserId: formData.handoverUserId,
      handoverUserName: handoverUser?.realName,
      handoverRemark: formData.handoverRemark
    };
    await createLeaveApplication(data);
    ElMessage.success('草稿已保存');
  } catch (error) {
    console.error('保存草稿失败', error);
  } finally {
    saving.value = false;
  }
};

const handleSubmit = async () => {
  const valid = await formRef.value?.validate();
  if (!valid) return;

  submitting.value = true;
  try {
    const handoverUser = userList.value.find(u => u.id === formData.handoverUserId);
    const data = {
      leaveType: formData.leaveType,
      startTime: formData.startTime,
      endTime: formData.endTime,
      duration: formData.duration,
      reason: formData.reason,
      handoverUserId: formData.handoverUserId,
      handoverUserName: handoverUser?.realName,
      handoverRemark: formData.handoverRemark
    };
    
    // 创建申请
    const res: any = await createLeaveApplication(data);
    const applicationId = res.data?.id;
    
    // 提交申请
    await submitLeaveApplication(applicationId);
    
    ElMessage.success('申请已提交');
    router.push('/workflow/apply');
  } catch (error) {
    console.error('提交申请失败', error);
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  loadUserList();
  loadLeaveBalance();
});
</script>

<style scoped lang="scss">
.leave-create-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  
  .back-btn {
    margin-right: 16px;
  }
  
  h2 {
    margin: 0;
    font-size: 24px;
    color: #303133;
  }
}

.form-card {
  margin-bottom: 20px;
}

.balance-card {
  max-width: 400px;
  
  .balance-grid {
    display: flex;
    gap: 30px;
  }
  
  .balance-item {
    text-align: center;
    
    .label {
      display: block;
      color: #909399;
      font-size: 14px;
      margin-bottom: 4px;
    }
    
    .value {
      font-size: 20px;
      font-weight: 600;
      color: #409eff;
    }
  }
}

.upload-area {
  width: 100%;
  
  :deep(.el-upload-dragger) {
    width: 100%;
  }
}
</style>
