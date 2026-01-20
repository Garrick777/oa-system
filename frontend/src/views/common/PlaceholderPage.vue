<template>
  <div class="placeholder-page">
    <div class="placeholder-content">
      <div class="placeholder-icon">
        <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
        </svg>
      </div>
      <h2 class="placeholder-title">{{ $route.meta.title || '功能开发中' }}</h2>
      <p class="placeholder-desc">该功能模块正在开发中，敬请期待...</p>
      <div class="placeholder-info">
        <span class="info-item">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
          </svg>
          当前路径: {{ $route.path }}
        </span>
      </div>
      <el-button type="primary" @click="goBack" class="back-btn">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="19" y1="12" x2="5" y2="12"/>
          <polyline points="12 19 5 12 12 5"/>
        </svg>
        返回上一页
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/dashboard')
  }
}
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.placeholder-page {
  min-height: calc(100vh - 180px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.placeholder-content {
  text-align: center;
  max-width: 400px;
}

.placeholder-icon {
  width: 120px;
  height: 120px;
  margin: 0 auto 24px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.1), rgba(168, 85, 247, 0.1));
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $primary-color;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.05);
    opacity: 0.8;
  }
}

.placeholder-title {
  font-size: 24px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 12px;
}

.placeholder-desc {
  font-size: 14px;
  color: $text-secondary;
  margin-bottom: 24px;
}

.placeholder-info {
  display: flex;
  justify-content: center;
  margin-bottom: 32px;

  .info-item {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: $text-muted;
    background: $bg-page;
    padding: 8px 16px;
    border-radius: 20px;

    svg {
      flex-shrink: 0;
    }
  }
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  height: 44px;
  padding: 0 24px;
  font-size: 15px;
  border-radius: 22px;
  background: $primary-gradient;
  border: none;

  svg {
    flex-shrink: 0;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 20px rgba(99, 102, 241, 0.35);
  }
}
</style>
