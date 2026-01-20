<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>已审批</span>
        </div>
      </template>
      
      <el-empty description="暂无审批记录">
        <template #image>
          <svg width="120" height="120" viewBox="0 0 24 24" fill="none" stroke="#c0c4cc" stroke-width="1">
            <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14"/>
            <polyline points="22 4 12 14.01 9 11.01"/>
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
