import type { ComponentResolver } from 'unplugin-vue-components'
import type { ConfigEnv, UserConfig } from 'vite'
import { fileURLToPath, URL } from 'node:url'
import { MetaUiWebResolver, Vuetify3LabsLabResolver } from '@compose/ui/unplugin'
import { quasar, transformAssetUrls as QuasarTransformAssetUrls } from '@quasar/vite-plugin'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import unocss from 'unocss/vite'
import AutoImport from 'unplugin-auto-import/vite'
import { ElementPlusResolver, NaiveUiResolver, QuasarResolver, VarletUIResolver } from 'unplugin-vue-components/resolvers'
import Components from 'unplugin-vue-components/vite'
import { VueRouterAutoImports } from 'unplugin-vue-router'
import vueRouterUnplugin from 'unplugin-vue-router/vite'
import { defineConfig } from 'vite'
import viteCompression from 'vite-plugin-compression'
import devTools from 'vite-plugin-vue-devtools'
import { extendRoute } from './src/router/extendRoute'

async function configVite(env: ConfigEnv): Promise<UserConfig> {
  const isProd = env.mode === 'production'
  const isTest = env.mode === 'test'
  return {
    plugins: [
      isTest ? void 0 : devTools(),
      vueRouterUnplugin({
        routesFolder: [
          {
            src: 'src/pages',
            path: '',
            exclude: (excluded: string[]) => [...excluded, '**/*View.vue', '**/Page.vue', '**/[A-Z]*.vue'],
            filePatterns: (filePatterns: string[]) => filePatterns,
            extensions: (extensions: string[]) => extensions,
          },
        ],
        extensions: ['.vue'],
        filePatterns: ['**/*'],
        exclude: [],
        dts: './typed-router.d.ts',
        routeBlockLang: 'json5',
        importMode: 'async',
        pathParser: {
          dotNesting: true,
        },
        extendRoute,
      }),
      vue({
        template: {
          transformAssetUrls: {
            ...QuasarTransformAssetUrls,
          },
        },
      }),
      vueJsx(),
      unocss(),
      quasar({
        autoImportComponentCase: 'pascal',
        sassVariables: false,
      }),
      AutoImport({
        imports: [
          'vue',
          {
            vuetify: ['useDisplay'],
          },
          {
            'naive-ui': ['useDialog', 'useMessage', 'useNotification', 'useLoadingBar'],
          },
          { zod: ['z'] },
          'pinia',
          { '@/api': ['api'] },
          { '@vueuse/integrations/useQRCode': ['useQRCode'] },
          { vue: ['useTemplateRef'] },
          VueRouterAutoImports,
          'vue-i18n',
          '@vueuse/core',
        ],
        dts: 'imports-auto.d.ts',
        eslintrc: {
          enabled: true,
          filepath: './imports-eslint.json',
          globalsPropValue: true,
        },
      }),
      Components({
        dts: 'imports-comp.d.ts',
        deep: true,
        dirs: ['src/components', 'src/views', 'src/layouts'],
        resolvers: [
          ElementPlusResolver(),
          QuasarResolver(),
          NaiveUiResolver(),
          Vuetify3LabsLabResolver() as unknown as ComponentResolver,
          MetaUiWebResolver() as unknown as ComponentResolver,
          VarletUIResolver(),
        ],
      }),
      viteCompression({
        filter: /.(js|mjs|cjs|json|css|html|svg|map)$/i,
        verbose: true,
        disable: false,
        deleteOriginFile: false,
        threshold: 10240,
        algorithm: 'gzip',
        ext: 'gz',
      }),
    ],
    envDir: './src/config/env',
    build: {
      sourcemap: !isProd,
      minify: 'terser',
      terserOptions: {
        compress: {
          drop_console: isProd,
          drop_debugger: isProd,
        },
      },
      rollupOptions: {
        output: {
          entryFileNames: '[hash].js',
          assetFileNames: '[hash].[ext]',
          chunkFileNames: () => `[hash].js`,
        },
      },
    },
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    server: {
      port: isTest ? 80 : 3000,
      host: '0.0.0.0',
      allowedHosts: ['lvh.me', 'localhost', '127.0.0.1'],
      fs: {
        strict: false,
      },
    },
  }
}

export default defineConfig(configVite)
