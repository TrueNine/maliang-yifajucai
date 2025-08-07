<script setup lang="ts">
import SnackbarConfig from '@/SnackbarConfig.vue'
import { useConfigStore, useUserStore } from '@/store'

const userStore = useUserStore()
const cfgStore = useConfigStore()

const osDarkMatch = window.matchMedia('(prefers-color-scheme: dark)')
cfgStore.osDark = osDarkMatch.matches
osDarkMatch.onchange = (event) => {
  cfgStore.osDark = event.matches
}
</script>

<template>
<YConfigProvider locale="zh-CN" :dark="cfgStore.dark">
  <YConfigPreAuthorize
    :permissionsProvider="() => userStore.permissions"
    :authedProvider="() => userStore.isLogin"
    :anonymousProvider="() => !userStore.isLogin"
    :rolesProvider="() => userStore.roles"
  >
    <!-- 提示组件 -->
    <TnSnackbar>
      <!-- 通知组件 -->
      <SnackbarConfig />
      <!-- 全屏加载组件 -->
      <FullScreenLoaderComp>
        <RouterView v-slot="{ Component }">
          <component :is="Component" />
        </RouterView>
      </FullScreenLoaderComp>
    </TnSnackbar>
  </YConfigPreAuthorize>
</YConfigProvider>
</template>

<style lang="scss">
:root {
  font-size: 16px;
  overflow-y: auto;
  overflow-x: auto;
}

* {
  font-size: inherit;
}
</style>
