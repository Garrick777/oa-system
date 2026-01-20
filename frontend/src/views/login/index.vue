<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="gradient-orb orb-1"></div>
      <div class="gradient-orb orb-2"></div>
      <div class="gradient-orb orb-3"></div>
      <div class="grid-pattern"></div>
    </div>

    <div class="login-wrapper">
      <!-- 左侧品牌区(桌面端) -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="logo-mark">
            <svg viewBox="0 0 64 64" fill="none">
              <rect width="64" height="64" rx="16" fill="url(#brand-gradient)"/>
              <path d="M20 32L28 40L44 24" stroke="white" stroke-width="4" stroke-linecap="round" stroke-linejoin="round"/>
              <defs>
                <linearGradient id="brand-gradient" x1="0" y1="0" x2="64" y2="64">
                  <stop stop-color="#6366f1"/>
                  <stop offset="1" stop-color="#a855f7"/>
                </linearGradient>
              </defs>
            </svg>
          </div>
          <h1 class="brand-title">OA 办公系统</h1>
          <p class="brand-subtitle">高效协作 · 智能办公</p>
          
          <div class="features">
            <div class="feature-item">
              <div class="feature-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>
                </svg>
              </div>
              <div class="feature-text">
                <span class="feature-title">安全可靠</span>
                <span class="feature-desc">企业级数据安全保障</span>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12,6 12,12 16,14"/>
                </svg>
              </div>
              <div class="feature-text">
                <span class="feature-title">高效审批</span>
                <span class="feature-desc">流程自动化处理</span>
              </div>
            </div>
            <div class="feature-item">
              <div class="feature-icon">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
                  <circle cx="9" cy="7" r="4"/>
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87"/>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"/>
                </svg>
              </div>
              <div class="feature-text">
                <span class="feature-title">团队协作</span>
                <span class="feature-desc">打破信息孤岛</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 登录表单区 -->
      <div class="login-section">
        <div class="login-card">
          <!-- 移动端Logo -->
          <div class="mobile-logo">
            <div class="logo-mark-small">
              <svg viewBox="0 0 64 64" fill="none">
                <rect width="64" height="64" rx="16" fill="url(#mobile-gradient)"/>
                <path d="M20 32L28 40L44 24" stroke="white" stroke-width="4" stroke-linecap="round" stroke-linejoin="round"/>
                <defs>
                  <linearGradient id="mobile-gradient" x1="0" y1="0" x2="64" y2="64">
                    <stop stop-color="#6366f1"/>
                    <stop offset="1" stop-color="#a855f7"/>
                  </linearGradient>
                </defs>
              </svg>
            </div>
            <span class="mobile-title">OA 办公</span>
          </div>

          <div class="login-header">
            <h2>欢迎回来</h2>
            <p>请输入您的账号信息登录系统</p>
          </div>

          <el-form
            ref="formRef"
            :model="loginForm"
            :rules="rules"
            class="login-form"
            @submit.prevent="handleLogin"
          >
            <el-form-item prop="username">
              <div class="input-wrapper">
                <div class="input-icon">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                </div>
                <el-input
                  v-model="loginForm.username"
                  placeholder="请输入用户名"
                  size="large"
                  :prefix-icon="null"
                />
              </div>
            </el-form-item>

            <el-form-item prop="password">
              <div class="input-wrapper">
                <div class="input-icon">
                  <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <rect x="3" y="11" width="18" height="11" rx="2" ry="2"/>
                    <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
                  </svg>
                </div>
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  show-password
                  :prefix-icon="null"
                />
              </div>
            </el-form-item>

            <div class="login-options">
              <el-checkbox v-model="rememberMe">记住登录</el-checkbox>
              <a href="#" class="forgot-link">忘记密码？</a>
            </div>

            <el-button
              type="primary"
              class="login-btn"
              :loading="loading"
              native-type="submit"
              size="large"
            >
              <span v-if="!loading">登 录</span>
              <span v-else>登录中...</span>
            </el-button>
          </el-form>

          <div class="login-footer">
            <span class="divider-text">测试账号 (密码均为: 123456)</span>
            <div class="test-accounts">
              <button class="test-btn" @click="fillTestAccount('admin', '123456')">
                <span class="test-icon">👑</span>
                <span>超级管理员</span>
              </button>
              <button class="test-btn" @click="fillTestAccount('leader', '123456')">
                <span class="test-icon">👔</span>
                <span>公司高管</span>
              </button>
              <button class="test-btn" @click="fillTestAccount('hr', '123456')">
                <span class="test-icon">👥</span>
                <span>HR管理员</span>
              </button>
              <button class="test-btn" @click="fillTestAccount('officer', '123456')">
                <span class="test-icon">📋</span>
                <span>行政管理员</span>
              </button>
              <button class="test-btn" @click="fillTestAccount('finance', '123456')">
                <span class="test-icon">💰</span>
                <span>财务管理员</span>
              </button>
              <button class="test-btn" @click="fillTestAccount('manager', '123456')">
                <span class="test-icon">👨‍💻</span>
                <span>部门经理</span>
              </button>
              <button class="test-btn" @click="fillTestAccount('employee', '123456')">
                <span class="test-icon">👤</span>
                <span>普通员工</span>
              </button>
            </div>
          </div>
        </div>

        <p class="copyright">© 2026 OA办公系统 · All Rights Reserved</p>
      </div>
    </div>

    <!-- 首次修改密码弹窗 -->
    <el-dialog
      v-model="showChangePasswordDialog"
      title="首次登录 - 请修改密码"
      width="420px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
    >
      <div class="change-pwd-tips">
        <el-alert
          title="为了您的账号安全，首次登录请修改初始密码"
          type="warning"
          :closable="false"
          show-icon
        />
      </div>
      <el-form class="change-pwd-form" label-width="100px">
        <el-form-item label="新密码">
          <el-input
            v-model="changePasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码（至少6位）"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input
            v-model="changePasswordForm.confirmPassword"
            type="password"
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button
          type="primary"
          :loading="changePasswordLoading"
          @click="handleChangePassword"
          style="width: 100%"
        >
          确认修改
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const rememberMe = ref(true)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const fillTestAccount = (username: string, password: string) => {
  loginForm.username = username
  loginForm.password = password
}

const handleLogin = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const res = await userStore.login({
      username: loginForm.username,
      password: loginForm.password
    })
    ElMessage.success('登录成功')
    
    // 检查是否需要首次修改密码
    if (res.needChangePassword) {
      showChangePasswordDialog.value = true
    } else {
      router.push('/dashboard')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

// 首次修改密码相关
const showChangePasswordDialog = ref(false)
const changePasswordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})
const changePasswordLoading = ref(false)

const handleChangePassword = async () => {
  if (!changePasswordForm.newPassword || changePasswordForm.newPassword.length < 6) {
    ElMessage.error('密码长度不能少于6位')
    return
  }
  if (changePasswordForm.newPassword !== changePasswordForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  
  changePasswordLoading.value = true
  try {
    await userStore.firstChangePassword(changePasswordForm.newPassword)
    ElMessage.success('密码修改成功')
    showChangePasswordDialog.value = false
    router.push('/dashboard')
  } catch (error: any) {
    ElMessage.error(error.message || '密码修改失败')
  } finally {
    changePasswordLoading.value = false
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.login-page {
  min-height: 100vh;
  min-height: -webkit-fill-available;
  background: $bg-dark;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
  font-family: $font-family;
  box-sizing: border-box;

  @media (max-width: 767px) {
    padding: 16px;
    align-items: flex-start;
    padding-top: 60px;
  }
}

// ========================================
// 背景装饰
// ========================================
.bg-decoration {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.gradient-orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(100px);
  opacity: 0.4;
  will-change: transform;

  &.orb-1 {
    width: 600px;
    height: 600px;
    background: linear-gradient(135deg, #6366f1, #8b5cf6);
    top: -200px;
    right: -100px;
    -webkit-animation: float1 20s ease-in-out infinite;
    animation: float1 20s ease-in-out infinite;

    @media (max-width: 767px) {
      width: 300px;
      height: 300px;
      top: -100px;
      right: -50px;
    }
  }

  &.orb-2 {
    width: 500px;
    height: 500px;
    background: linear-gradient(135deg, #a855f7, #ec4899);
    bottom: -150px;
    left: -100px;
    -webkit-animation: float2 25s ease-in-out infinite;
    animation: float2 25s ease-in-out infinite;

    @media (max-width: 767px) {
      width: 250px;
      height: 250px;
      bottom: -100px;
      left: -50px;
    }
  }

  &.orb-3 {
    width: 400px;
    height: 400px;
    background: linear-gradient(135deg, #3b82f6, #06b6d4);
    top: 50%;
    left: 50%;
    -webkit-transform: translate(-50%, -50%);
    transform: translate(-50%, -50%);
    opacity: 0.2;
    -webkit-animation: pulse 15s ease-in-out infinite;
    animation: pulse 15s ease-in-out infinite;

    @media (max-width: 767px) {
      width: 200px;
      height: 200px;
    }
  }
}

@-webkit-keyframes float1 {
  0%, 100% { -webkit-transform: translate(0, 0) scale(1); transform: translate(0, 0) scale(1); }
  50% { -webkit-transform: translate(-50px, 50px) scale(1.1); transform: translate(-50px, 50px) scale(1.1); }
}

@keyframes float1 {
  0%, 100% { -webkit-transform: translate(0, 0) scale(1); transform: translate(0, 0) scale(1); }
  50% { -webkit-transform: translate(-50px, 50px) scale(1.1); transform: translate(-50px, 50px) scale(1.1); }
}

@-webkit-keyframes float2 {
  0%, 100% { -webkit-transform: translate(0, 0) scale(1); transform: translate(0, 0) scale(1); }
  50% { -webkit-transform: translate(30px, -40px) scale(1.15); transform: translate(30px, -40px) scale(1.15); }
}

@keyframes float2 {
  0%, 100% { -webkit-transform: translate(0, 0) scale(1); transform: translate(0, 0) scale(1); }
  50% { -webkit-transform: translate(30px, -40px) scale(1.15); transform: translate(30px, -40px) scale(1.15); }
}

@-webkit-keyframes pulse {
  0%, 100% { opacity: 0.2; -webkit-transform: translate(-50%, -50%) scale(1); transform: translate(-50%, -50%) scale(1); }
  50% { opacity: 0.3; -webkit-transform: translate(-50%, -50%) scale(1.2); transform: translate(-50%, -50%) scale(1.2); }
}

@keyframes pulse {
  0%, 100% { opacity: 0.2; -webkit-transform: translate(-50%, -50%) scale(1); transform: translate(-50%, -50%) scale(1); }
  50% { opacity: 0.3; -webkit-transform: translate(-50%, -50%) scale(1.2); transform: translate(-50%, -50%) scale(1.2); }
}

.grid-pattern {
  position: absolute;
  inset: 0;
  background-image: 
    linear-gradient(rgba(99, 102, 241, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(99, 102, 241, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
}

// ========================================
// 登录容器
// ========================================
.login-wrapper {
  display: flex;
  width: 100%;
  max-width: 1100px;
  min-height: 640px;
  background: rgba(255, 255, 255, 0.95);
  -webkit-backdrop-filter: blur(20px);
  backdrop-filter: blur(20px);
  border-radius: $radius-xl;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.4);
  overflow: hidden;
  position: relative;
  z-index: 1;

  @media (max-width: 1023px) {
    max-width: 480px;
    min-height: auto;
    flex-direction: column;
  }

  @media (max-width: 767px) {
    max-width: 100%;
    border-radius: $radius-lg;
  }
}

// ========================================
// 品牌区
// ========================================
.brand-section {
  width: 460px;
  background: linear-gradient(135deg, #1e1b4b 0%, #0f172a 100%);
  padding: 60px 50px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  flex-shrink: 0;

  @media (max-width: 1023px) {
    display: none;
  }
}

.brand-content {
  color: #fff;
}

.logo-mark {
  width: 64px;
  height: 64px;
  margin-bottom: 28px;

  svg {
    width: 100%;
    height: 100%;
  }
}

.brand-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
  letter-spacing: -0.5px;
}

.brand-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.6);
  margin-bottom: 48px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.feature-icon {
  width: 48px;
  height: 48px;
  background: rgba(99, 102, 241, 0.2);
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $primary-light;
  flex-shrink: 0;
}

.feature-text {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .feature-title {
    font-size: 15px;
    font-weight: 600;
  }

  .feature-desc {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.5);
  }
}

// ========================================
// 登录区
// ========================================
.login-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 50px;
  min-width: 0;

  @media (max-width: 767px) {
    padding: 32px 24px;
  }

  @media (max-width: 480px) {
    padding: 24px 20px;
  }
}

.login-card {
  width: 100%;
  max-width: 380px;

  @media (max-width: 767px) {
    max-width: 100%;
  }
}

.mobile-logo {
  display: none;
  align-items: center;
  gap: 12px;
  margin-bottom: 32px;
  justify-content: center;

  @media (max-width: 1023px) {
    display: flex;
  }

  @media (max-width: 480px) {
    margin-bottom: 24px;
  }

  .logo-mark-small {
    width: 44px;
    height: 44px;

    svg {
      width: 100%;
      height: 100%;
    }
  }

  .mobile-title {
    font-size: 22px;
    font-weight: 700;
    color: $text-primary;
  }
}

.login-header {
  margin-bottom: 32px;

  @media (max-width: 1023px) {
    text-align: center;
  }

  @media (max-width: 480px) {
    margin-bottom: 24px;
  }

  h2 {
    font-size: 28px;
    font-weight: 700;
    color: $text-primary;
    margin-bottom: 8px;
    letter-spacing: -0.5px;

    @media (max-width: 480px) {
      font-size: 24px;
    }
  }

  p {
    font-size: 14px;
    color: $text-secondary;
  }
}

.login-form {
  margin-bottom: 24px;

  .el-form-item {
    margin-bottom: 20px;

    @media (max-width: 480px) {
      margin-bottom: 16px;
    }
  }
}

.input-wrapper {
  position: relative;
  width: 100%;

  .input-icon {
    position: absolute;
    left: 16px;
    top: 50%;
    -webkit-transform: translateY(-50%);
    transform: translateY(-50%);
    color: $text-muted;
    z-index: 1;
    pointer-events: none;
  }

  :deep(.el-input) {
    .el-input__wrapper {
      padding: 0 16px 0 50px;
      height: 52px;
      background: $bg-page;
      border: 2px solid transparent;
      border-radius: $radius-md;
      box-shadow: none;
      -webkit-transition: all 0.2s;
      transition: all 0.2s;

      @media (max-width: 480px) {
        height: 48px;
        padding: 0 14px 0 46px;
      }

      &:hover {
        border-color: rgba(99, 102, 241, 0.3);
      }

      &.is-focus {
        border-color: $primary-color;
        background: #fff;
        box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
      }
    }

    .el-input__inner {
      font-size: 15px;
      color: $text-primary;
      height: 100%;

      &::-webkit-input-placeholder {
        color: $text-muted;
      }

      &::placeholder {
        color: $text-muted;
      }
    }
  }
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 12px;

  :deep(.el-checkbox) {
    .el-checkbox__label {
      font-size: 14px;
      color: $text-secondary;
    }
  }

  .forgot-link {
    font-size: 14px;
    color: $primary-color;
    text-decoration: none;
    -webkit-tap-highlight-color: transparent;

    &:hover {
      text-decoration: underline;
    }
  }
}

.login-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 600;
  background: $primary-gradient;
  border: none;
  border-radius: $radius-md;
  -webkit-transition: all 0.3s;
  transition: all 0.3s;
  -webkit-tap-highlight-color: transparent;

  @media (max-width: 480px) {
    height: 48px;
    font-size: 15px;
  }

  &:hover, &:focus {
    -webkit-transform: translateY(-2px);
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(99, 102, 241, 0.35);
  }

  &:active {
    -webkit-transform: translateY(0);
    transform: translateY(0);
  }

  &.is-loading {
    opacity: 0.8;
  }
}

.login-footer {
  margin-top: 32px;

  @media (max-width: 480px) {
    margin-top: 24px;
  }

  .divider-text {
    display: block;
    text-align: center;
    font-size: 13px;
    color: $text-muted;
    margin-bottom: 16px;
    position: relative;

    &::before,
    &::after {
      content: '';
      position: absolute;
      top: 50%;
      width: calc(50% - 50px);
      height: 1px;
      background: $border-color;
    }

    &::before { left: 0; }
    &::after { right: 0; }
  }
}

.test-accounts {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;

  @media (max-width: 400px) {
    grid-template-columns: 1fr;
  }
}

.test-btn {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  background: $bg-page;
  border: 1px solid $border-color;
  border-radius: $radius-md;
  font-size: 14px;
  color: $text-secondary;
  cursor: pointer;
  -webkit-transition: all 0.2s;
  transition: all 0.2s;
  -webkit-tap-highlight-color: transparent;

  &:hover, &:active {
    border-color: $primary-color;
    color: $primary-color;
    background: rgba(99, 102, 241, 0.05);
  }

  .test-icon {
    font-size: 18px;
  }
}

.copyright {
  margin-top: 32px;
  font-size: 12px;
  color: $text-muted;
  text-align: center;

  @media (max-width: 480px) {
    margin-top: 24px;
  }
}

// 修改密码弹窗
.change-pwd-tips {
  margin-bottom: 24px;
}

.change-pwd-form {
  :deep(.el-form-item) {
    margin-bottom: 20px;
  }
}
</style>
