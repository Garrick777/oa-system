<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的申请</span>
          <el-button type="primary" size="small">
            <el-icon><Plus /></el-icon>发起申请
          </el-button>
        </div>
      </template>
      
      <el-empty description="暂无申请记录">
        <template #image>
          <svg width="120" height="120" viewBox="0 0 24 24" fill="none" stroke="#c0c4cc" stroke-width="1">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="12" y1="18" x2="12" y2="12"/>
            <line x1="9" y1="15" x2="15" y2="15"/>
          </svg>
        </template>
      </el-empty>
      
      <div class="coming-soon">
        <el-tag type="info" size="large">功能开发中...</el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue'
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.coming-soon {
  text-align: center;
  padding: 20px 0;
}
</style>
