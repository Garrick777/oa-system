module.exports = {
  plugins: {
    autoprefixer: {
      // 自动添加浏览器前缀
      // 支持的浏览器列表
      overrideBrowserslist: [
        '> 1%',              // 全球使用率 > 1%
        'last 2 versions',   // 每个浏览器最后2个版本
        'not dead',          // 排除已停止维护的浏览器
        'Chrome >= 63',      // Chrome 63+
        'Firefox >= 67',     // Firefox 67+
        'Safari >= 11',      // Safari 11+
        'Edge >= 79',        // Edge 79+
        'iOS >= 11',         // iOS Safari 11+
        'Android >= 5',      // Android 5+
      ],
      // 启用flexbox前缀
      flexbox: true,
      // 启用grid前缀（仅限IE）
      grid: 'autoplace',
    },
  },
}
