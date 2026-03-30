import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const isCapacitorBuild = env.VITE_APP_RUNTIME === 'capacitor'

  return {
    base: isCapacitorBuild ? './' : '/',
    plugins: [
      vue(),
      // vueDevTools(),
    ],
    build: {
      rollupOptions: {
        output: {
          manualChunks(id) {
            if (!id.includes('node_modules')) {
              return
            }
            if (
              id.includes('/node_modules/vue/') ||
              id.includes('/node_modules/pinia/') ||
              id.includes('/node_modules/vue-router/')
            ) {
              return 'vendor-vue'
            }
            if (
              id.includes('/node_modules/ant-design-vue/') ||
              id.includes('/node_modules/@ant-design/')
            ) {
              return 'vendor-ant'
            }
            if (
              id.includes('/node_modules/echarts/') ||
              id.includes('/node_modules/vue-echarts/')
            ) {
              return 'vendor-echarts'
            }
            if (
              id.includes('/node_modules/three/') ||
              id.includes('/node_modules/three-stdlib/') ||
              id.includes('/node_modules/@pixiv/three-vrm/') ||
              id.includes('/node_modules/mmd-parser/')
            ) {
              return 'vendor-three'
            }
            if (
              id.includes('/node_modules/markdown-it/') ||
              id.includes('/node_modules/dompurify/')
            ) {
              return 'vendor-doc-text-preview'
            }
            if (id.includes('/node_modules/mammoth/')) {
              return 'vendor-docx-preview'
            }
            if (id.includes('/node_modules/rtf.js/')) {
              return 'vendor-rtf-preview'
            }
            if (
              id.includes('/node_modules/@tensorflow/') ||
              id.includes('/node_modules/@tensorflow-models/')
            ) {
              return 'vendor-tfjs'
            }
          },
        },
      },
    },
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      },
    },
  }
})
