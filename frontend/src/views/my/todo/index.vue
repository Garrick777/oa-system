<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>待办事项</span>
        </div>
      </template>
      
      <el-empty description="暂无待办事项">
        <template #image>
          <svg width="120" height="120" viewBox="0 0 24 24" fill="none" stroke="#c0c4cc" stroke-width="1">
            <path d="M9 11l3 3L22 4"/>
            <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
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
.coming-soon {
  text-align: center;
  padding: 20px 0;
}
</style>
