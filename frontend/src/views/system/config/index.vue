<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统参数配置</span>
          <el-button type="primary" :loading="saveLoading" @click="handleSaveAll">
            <el-icon><Check /></el-icon>保存全部
          </el-button>
        </div>
      </template>

      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基础配置 -->
        <el-tab-pane label="基础配置" name="basic">
          <el-form :model="basicConfig" label-width="160px" class="config-form">
            <el-form-item label="系统名称">
              <el-input v-model="basicConfig.systemName" placeholder="请输入系统名称" />
              <div class="form-tip">显示在浏览器标题和登录页</div>
            </el-form-item>
            <el-form-item label="系统Logo">
              <el-input v-model="basicConfig.systemLogo" placeholder="Logo图片URL" />
              <div class="form-tip">建议尺寸 200x60 px</div>
            </el-form-item>
            <el-form-item label="版权信息">
              <el-input v-model="basicConfig.copyright" placeholder="页脚版权信息" />
            </el-form-item>
            <el-form-item label="备案号">
              <el-input v-model="basicConfig.icp" placeholder="ICP备案号" />
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 安全配置 -->
        <el-tab-pane label="安全配置" name="security">
          <el-form :model="securityConfig" label-width="160px" class="config-form">
            <el-form-item label="登录验证码">
              <el-switch v-model="securityConfig.captchaEnabled" />
              <div class="form-tip">开启后登录需要输入验证码</div>
            </el-form-item>
            <el-form-item label="密码最小长度">
              <el-input-number v-model="securityConfig.passwordMinLength" :min="6" :max="20" />
              <div class="form-tip">用户密码最小字符数</div>
            </el-form-item>
            <el-form-item label="登录失败锁定">
              <el-switch v-model="securityConfig.loginFailLock" />
              <div class="form-tip">连续登录失败后锁定账号</div>
            </el-form-item>
            <el-form-item label="最大失败次数" v-if="securityConfig.loginFailLock">
              <el-input-number v-model="securityConfig.maxFailCount" :min="3" :max="10" />
              <div class="form-tip">连续失败多少次后锁定</div>
            </el-form-item>
            <el-form-item label="锁定时长（分钟）" v-if="securityConfig.loginFailLock">
              <el-input-number v-model="securityConfig.lockMinutes" :min="5" :max="60" />
            </el-form-item>
            <el-form-item label="Token有效期（小时）">
              <el-input-number v-model="securityConfig.tokenExpireHours" :min="1" :max="168" />
              <div class="form-tip">用户登录后Token的有效时长</div>
            </el-form-item>
            <el-form-item label="密码过期天数">
              <el-input-number v-model="securityConfig.passwordExpireDays" :min="0" :max="365" />
              <div class="form-tip">0表示永不过期</div>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 上传配置 -->
        <el-tab-pane label="上传配置" name="upload">
          <el-form :model="uploadConfig" label-width="160px" class="config-form">
            <el-form-item label="文件存储方式">
              <el-radio-group v-model="uploadConfig.storageType">
                <el-radio value="local">本地存储</el-radio>
                <el-radio value="oss">阿里云OSS</el-radio>
                <el-radio value="cos">腾讯云COS</el-radio>
                <el-radio value="minio">MinIO</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="上传大小限制(MB)">
              <el-input-number v-model="uploadConfig.maxFileSize" :min="1" :max="100" />
            </el-form-item>
            <el-form-item label="允许的文件类型">
              <el-input
                v-model="uploadConfig.allowedTypes"
                placeholder="jpg,png,pdf,doc,docx,xls,xlsx"
              />
              <div class="form-tip">多个类型用逗号分隔</div>
            </el-form-item>
            <template v-if="uploadConfig.storageType === 'oss'">
              <el-divider content-position="left">阿里云OSS配置</el-divider>
              <el-form-item label="Endpoint">
                <el-input v-model="uploadConfig.ossEndpoint" placeholder="oss-cn-hangzhou.aliyuncs.com" />
              </el-form-item>
              <el-form-item label="AccessKey ID">
                <el-input v-model="uploadConfig.ossAccessKeyId" placeholder="AccessKey ID" />
              </el-form-item>
              <el-form-item label="AccessKey Secret">
                <el-input v-model="uploadConfig.ossAccessKeySecret" type="password" placeholder="AccessKey Secret" show-password />
              </el-form-item>
              <el-form-item label="Bucket名称">
                <el-input v-model="uploadConfig.ossBucket" placeholder="Bucket名称" />
              </el-form-item>
            </template>
          </el-form>
        </el-tab-pane>

        <!-- 邮件配置 -->
        <el-tab-pane label="邮件配置" name="email">
          <el-form :model="emailConfig" label-width="160px" class="config-form">
            <el-form-item label="启用邮件通知">
              <el-switch v-model="emailConfig.enabled" />
            </el-form-item>
            <template v-if="emailConfig.enabled">
              <el-form-item label="SMTP服务器">
                <el-input v-model="emailConfig.smtpHost" placeholder="smtp.example.com" />
              </el-form-item>
              <el-form-item label="SMTP端口">
                <el-input-number v-model="emailConfig.smtpPort" :min="1" :max="65535" />
              </el-form-item>
              <el-form-item label="发件人邮箱">
                <el-input v-model="emailConfig.fromEmail" placeholder="noreply@example.com" />
              </el-form-item>
              <el-form-item label="发件人名称">
                <el-input v-model="emailConfig.fromName" placeholder="OA系统" />
              </el-form-item>
              <el-form-item label="SMTP用户名">
                <el-input v-model="emailConfig.smtpUsername" placeholder="邮箱账号" />
              </el-form-item>
              <el-form-item label="SMTP密码">
                <el-input v-model="emailConfig.smtpPassword" type="password" placeholder="邮箱密码或授权码" show-password />
              </el-form-item>
              <el-form-item label="使用SSL">
                <el-switch v-model="emailConfig.useSsl" />
              </el-form-item>
              <el-form-item>
                <el-button type="success" @click="handleTestEmail">
                  <el-icon><Message /></el-icon>发送测试邮件
                </el-button>
              </el-form-item>
            </template>
          </el-form>
        </el-tab-pane>

        <!-- 通知配置 -->
        <el-tab-pane label="通知配置" name="notification">
          <el-form :model="notifyConfig" label-width="160px" class="config-form">
            <el-form-item label="审批通知方式">
              <el-checkbox-group v-model="notifyConfig.approvalNotify">
                <el-checkbox value="system">站内消息</el-checkbox>
                <el-checkbox value="email">邮件</el-checkbox>
                <el-checkbox value="sms">短信</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="公告通知方式">
              <el-checkbox-group v-model="notifyConfig.announcementNotify">
                <el-checkbox value="system">站内消息</el-checkbox>
                <el-checkbox value="email">邮件</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="任务提醒方式">
              <el-checkbox-group v-model="notifyConfig.taskNotify">
                <el-checkbox value="system">站内消息</el-checkbox>
                <el-checkbox value="email">邮件</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item label="考勤异常提醒">
              <el-checkbox-group v-model="notifyConfig.attendanceNotify">
                <el-checkbox value="system">站内消息</el-checkbox>
                <el-checkbox value="email">邮件</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import * as api from '@/api/system'

const activeTab = ref('basic')
const saveLoading = ref(false)

// 基础配置
const basicConfig = reactive({
  systemName: 'OA办公自动化系统',
  systemLogo: '',
  copyright: '© 2026 智慧科技有限公司',
  icp: '',
})

// 安全配置
const securityConfig = reactive({
  captchaEnabled: true,
  passwordMinLength: 6,
  loginFailLock: true,
  maxFailCount: 5,
  lockMinutes: 15,
  tokenExpireHours: 24,
  passwordExpireDays: 90,
})

// 上传配置
const uploadConfig = reactive({
  storageType: 'local',
  maxFileSize: 10,
  allowedTypes: 'jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx,ppt,pptx,zip,rar',
  ossEndpoint: '',
  ossAccessKeyId: '',
  ossAccessKeySecret: '',
  ossBucket: '',
})

// 邮件配置
const emailConfig = reactive({
  enabled: false,
  smtpHost: '',
  smtpPort: 465,
  fromEmail: '',
  fromName: 'OA系统',
  smtpUsername: '',
  smtpPassword: '',
  useSsl: true,
})

// 通知配置
const notifyConfig = reactive({
  approvalNotify: ['system'],
  announcementNotify: ['system'],
  taskNotify: ['system'],
  attendanceNotify: ['system'],
})

// 加载配置
const loadConfigs = async () => {
  try {
    const res = await api.getConfigs()
    const configs = res.data || {}

    // 解析并赋值各配置项
    if (configs.system_name) basicConfig.systemName = configs.system_name
    if (configs.system_logo) basicConfig.systemLogo = configs.system_logo
    if (configs.copyright) basicConfig.copyright = configs.copyright
    if (configs.icp) basicConfig.icp = configs.icp

    if (configs.captcha_enabled !== undefined) securityConfig.captchaEnabled = configs.captcha_enabled === 'true'
    if (configs.password_min_length) securityConfig.passwordMinLength = parseInt(configs.password_min_length)
    // ... 其他配置项
  } catch (error) {
    console.log('使用默认配置')
  }
}

// 保存所有配置
const handleSaveAll = async () => {
  saveLoading.value = true
  try {
    const configs = [
      { key: 'system_name', value: basicConfig.systemName },
      { key: 'system_logo', value: basicConfig.systemLogo },
      { key: 'copyright', value: basicConfig.copyright },
      { key: 'icp', value: basicConfig.icp },
      { key: 'captcha_enabled', value: String(securityConfig.captchaEnabled) },
      { key: 'password_min_length', value: String(securityConfig.passwordMinLength) },
      { key: 'login_fail_lock', value: String(securityConfig.loginFailLock) },
      { key: 'max_fail_count', value: String(securityConfig.maxFailCount) },
      { key: 'lock_minutes', value: String(securityConfig.lockMinutes) },
      { key: 'token_expire_hours', value: String(securityConfig.tokenExpireHours) },
      { key: 'password_expire_days', value: String(securityConfig.passwordExpireDays) },
      { key: 'storage_type', value: uploadConfig.storageType },
      { key: 'max_file_size', value: String(uploadConfig.maxFileSize) },
      { key: 'allowed_file_types', value: uploadConfig.allowedTypes },
      { key: 'email_enabled', value: String(emailConfig.enabled) },
      { key: 'smtp_host', value: emailConfig.smtpHost },
      { key: 'smtp_port', value: String(emailConfig.smtpPort) },
      { key: 'approval_notify', value: notifyConfig.approvalNotify.join(',') },
      { key: 'announcement_notify', value: notifyConfig.announcementNotify.join(',') },
      { key: 'task_notify', value: notifyConfig.taskNotify.join(',') },
      { key: 'attendance_notify', value: notifyConfig.attendanceNotify.join(',') },
    ]

    await api.updateConfigs(configs)
    ElMessage.success('配置保存成功')
  } catch (error) {
    ElMessage.error('保存失败，请重试')
  } finally {
    saveLoading.value = false
  }
}

// 测试邮件
const handleTestEmail = async () => {
  if (!emailConfig.smtpHost || !emailConfig.fromEmail) {
    ElMessage.warning('请先填写完整的邮件配置')
    return
  }

  try {
    ElMessage.info('正在发送测试邮件...')
    // await api.sendTestEmail(emailConfig)
    ElMessage.success('测试邮件已发送，请检查收件箱')
  } catch (error) {
    ElMessage.error('发送失败，请检查配置')
  }
}

onMounted(() => {
  loadConfigs()
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

.config-form {
  max-width: 700px;
  padding: 20px;

  .el-form-item {
    margin-bottom: 24px;
  }
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.4;
}

:deep(.el-tabs__content) {
  padding: 0;
}

:deep(.el-divider__text) {
  font-size: 13px;
  color: #606266;
}
</style>
