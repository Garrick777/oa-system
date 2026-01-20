<template>
  <div class="page-container">
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="8" :lg="6">
        <el-card class="profile-card">
          <div class="profile-header">
            <div class="avatar">
              {{ userStore.userInfo?.realName?.charAt(0) || 'U' }}
            </div>
            <h3 class="name">{{ userStore.userInfo?.realName || '用户' }}</h3>
            <p class="role">{{ userStore.userInfo?.roleName || '员工' }}</p>
          </div>
          <div class="profile-info">
            <div class="info-item">
              <span class="label">用户名</span>
              <span class="value">{{ userStore.userInfo?.username }}</span>
            </div>
            <div class="info-item">
              <span class="label">部门</span>
              <span class="value">{{ userStore.userInfo?.deptName || '未分配' }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="24" :md="16" :lg="18">
        <el-card>
          <template #header>
            <span>个人信息</span>
          </template>
          
          <el-form label-width="100px" style="max-width: 500px;">
            <el-form-item label="真实姓名">
              <el-input v-model="form.realName" disabled />
            </el-form-item>
            <el-form-item label="手机号码">
              <el-input v-model="form.phone" placeholder="请输入手机号码" />
            </el-form-item>
            <el-form-item label="邮箱地址">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        
        <el-card style="margin-top: 20px;">
          <template #header>
            <span>修改密码</span>
          </template>
          
          <el-form label-width="100px" style="max-width: 500px;">
            <el-form-item label="当前密码">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const form = reactive({
  realName: userStore.userInfo?.realName || '',
  phone: '',
  email: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
</script>

<style scoped lang="scss">
.profile-card {
  text-align: center;
}

.profile-header {
  padding: 30px 20px 20px;
  
  .avatar {
    width: 80px;
    height: 80px;
    margin: 0 auto 16px;
    background: linear-gradient(135deg, #6366f1, #a855f7);
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 32px;
    font-weight: 600;
  }
  
  .name {
    font-size: 20px;
    font-weight: 600;
    color: #1f2937;
    margin-bottom: 4px;
  }
  
  .role {
    font-size: 14px;
    color: #6b7280;
  }
}

.profile-info {
  border-top: 1px solid #f3f4f6;
  padding: 20px;
  
  .info-item {
    display: flex;
    justify-content: space-between;
    padding: 12px 0;
    border-bottom: 1px solid #f3f4f6;
    
    &:last-child {
      border-bottom: none;
    }
    
    .label {
      color: #6b7280;
      font-size: 14px;
    }
    
    .value {
      color: #1f2937;
      font-size: 14px;
      font-weight: 500;
    }
  }
}
</style>
