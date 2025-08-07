<script setup lang="ts">
import { useConfigStore } from '@/store'

const cfgStore = useConfigStore()
const viewport = document.head.querySelector('meta[name="viewport"]')
if (viewport) {
  document.head.removeChild(viewport)
}
const meta = document.createElement('meta')
meta.name = 'viewport'
meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'
document.head.appendChild(meta)

onUnmounted(() => {
  document.head.removeChild(meta)
  const m = document.createElement('meta')
  m.name = 'viewport'
  m.content = 'width=device-width, initial-scale=1.0'
  document.head.appendChild(m)
})

const router = useRouter()
const title = ref<string>(router.currentRoute.value.meta.title as string)

router.beforeEach((to, _, next) => {
  title.value = to.meta.title as string
  next()
})
</script>

<template>
<YVSystemBar :showAppBar="false">
  <RouterView v-slot="{ Component }">
    <component :is="Component" />
  </RouterView>

  <template v-if="cfgStore.bottomNavigationShow">
    <!-- 底部菜单栏 -->
    <VBottomNavigation
      v-model="cfgStore.activeBottomNavigation"
      role="menu"
      aria-roledescription="底部导航栏"
      :disabled="!cfgStore.bottomNavigationShow"
      :active="cfgStore.bottomNavigationShow"
      :elevation="5"
      :mandatory="true"
      :grow="true"
      mode="shift"
      height="100"
    >
      <VBtn role="menuitem" :disabled="!cfgStore.bottomNavigationShow" value="home" @click="router.push(`/wxpa`)">
        <YIco class="i-mdi:home text-12" />
        <span class="text-5">主页</span>
      </VBtn>

      <VBtn role="menuitem" :disabled="!cfgStore.bottomNavigationShow" value="service" @click="router.push(`/wxpa/service`)">
        <YIco class="i-mdi:grid text-12" />
        <span class="text-5">服务</span>
      </VBtn>

      <VBtn role="menuitem" :disabled="!cfgStore.bottomNavigationShow" value="info" @click="router.push(`/wxpa/me`)">
        <YIco class="i-mdi:account text-12" />
        <span class="text-5">个人信息</span>
      </VBtn>
    </VBottomNavigation>
  </template>
</YVSystemBar>
</template>
