<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>待我审批</span>
          <el-tag type="danger">0</el-tag>
        </div>
      </template>
      
      <el-empty description="暂无待审批事项">
        <template #image>
          <svg width="120" height="120" viewBox="0 0 24 24" fill="none" stroke="#c0c4cc" stroke-width="1">
            <circle cx="12" cy="12" r="10"/>
            <polyline points="12 6 12 12 16 14"/>
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
