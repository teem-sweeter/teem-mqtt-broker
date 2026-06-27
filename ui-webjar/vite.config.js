import { defineConfig, loadEnv } from 'vite';
import vue from '@vitejs/plugin-vue';
import vueDevTools from 'vite-plugin-vue-devtools';
import { fileURLToPath, URL } from 'node:url';

// https://vitejs.dev/config/
export default defineConfig(({ mode }) => {
  // 加载环境变量
  const env = loadEnv(mode, process.cwd(), '');

  return {
    plugins: [
      vue(),
      // 仅在开发环境中启用vueDevTools
      mode === 'development' ? vueDevTools() : null,
    ].filter(Boolean),
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    // 构建配置
    base: './', // 相对路径，适合打包部署
    build: {
      // 默认值，适合现代浏览器
      target: 'es2015',
      // 输出目录，默认 dist
      outDir: 'dist',
      // 静态资源存放目录
      assetsDir: 'assets',
      // 减小内联资源阈值，避免大文件内联
      assetsInlineLimit: 4096,
      sourcemap: false,  // 不生成 sourcemap
      emptyOutDir: true, // 清空输出目录
      // 启用压缩
      minify: 'terser',
      terserOptions: {
        compress: {
          // 删除console
          drop_console: true,
          // 删除debugger
          drop_debugger: true,
          // 删除指定函数
         // pure_funcs: ['console.log']
        }
      },
      rollupOptions: {
        output: {
          chunkFileNames: 'assets/js/[name]-[hash].js',
          entryFileNames: 'assets/js/[name]-[hash].js',
          assetFileNames: 'assets/[ext]/[name]-[hash].[ext]',
          // 分包策略，减少初始加载体积
          manualChunks(id) {
            if (id.includes('node_modules/vue') || id.includes('node_modules/vue-router') || id.includes('node_modules/pinia')) {
              return 'vue-vendor';
            }
            if (id.includes('node_modules/element-plus')) {
              return 'ui-vendor';
            }
            if (id.includes('node_modules/axios')) {
              return 'util-vendor';
            }
          }
        }
      }
    },
    server: {
      proxy: {
        // 从环境变量读取后端地址，支持不同环境配置
        // 默认为本地开发环境地址
        '/actuator': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080',
          changeOrigin: true,
        },
        '/v1': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080',
          changeOrigin: true,
        },
        '/api': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080',
          changeOrigin: true,
        },
        '/ws': {
          target: env.VITE_API_BASE_URL || 'http://localhost:8080',
          changeOrigin: true,
          ws: true
        }
      }
    }
  };
});
