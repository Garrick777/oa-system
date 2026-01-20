import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3000,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:9090',
        changeOrigin: true,
      },
    },
  },
  css: {
    // CSS预处理器配置
    preprocessorOptions: {
      scss: {
        // 可以在这里添加全局SCSS变量
        additionalData: `@use "@/styles/variables.scss" as *;`,
      },
    },
    // PostCSS配置 - 自动添加浏览器前缀
    postcss: {
      plugins: [
        // autoprefixer将在构建时自动处理
      ],
    },
  },
  build: {
    // 构建目标 - 支持的浏览器
    target: ['es2015', 'chrome63', 'firefox67', 'safari11', 'edge79'],
    // CSS代码分割
    cssCodeSplit: true,
    // 输出目录
    outDir: 'dist',
    // 静态资源目录
    assetsDir: 'assets',
    // 小于此阈值的资源内联为base64
    assetsInlineLimit: 4096,
    // 源码映射
    sourcemap: false,
    // 压缩方式
    minify: 'terser',
    terserOptions: {
      compress: {
        // 生产环境移除console
        drop_console: true,
        drop_debugger: true,
      },
    },
    // Rollup选项
    rollupOptions: {
      output: {
        // 分包策略
        manualChunks: {
          // Vue相关
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          // Element Plus
          'element-plus': ['element-plus'],
          // 工具库
          'utils': ['axios', 'lodash-es'],
        },
        // 资源文件命名
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: (assetInfo) => {
          const info = assetInfo.name?.split('.')
          const ext = info?.[info.length - 1]
          if (ext === 'css') {
            return 'assets/css/[name]-[hash].[ext]'
          }
          if (/\.(png|jpe?g|gif|svg|webp|ico)$/.test(assetInfo.name || '')) {
            return 'assets/images/[name]-[hash].[ext]'
          }
          if (/\.(woff2?|eot|ttf|otf)$/.test(assetInfo.name || '')) {
            return 'assets/fonts/[name]-[hash].[ext]'
          }
          return 'assets/[name]-[hash].[ext]'
        },
      },
    },
  },
  // 优化依赖预构建
  optimizeDeps: {
    include: ['vue', 'vue-router', 'pinia', 'axios', 'element-plus'],
  },
})
