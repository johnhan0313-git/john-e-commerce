/// <reference types="vitest" />
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
      '@john/fe-shared': resolve(__dirname, '../../packages/fe-shared/src'),
    }
  },
  server: {
    host: true,
    port: 3023,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8020',
        changeOrigin: true
      }
    }
  },
  test: {
    environment: 'happy-dom',
    globals: true
  }
})
