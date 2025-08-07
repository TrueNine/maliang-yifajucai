<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useDisplay } from 'vuetify'
import logoSrc from '@/assets/logo.png'
import { useConfigStore } from '@/store'

const { mobile } = useDisplay()
const cfgStore = useConfigStore()
const router = useRouter()

const drawer = ref(false)
const themeIcon = computed(() => cfgStore.dark ? 'i-mdi:weather-night' : 'i-mdi:weather-sunny')

const menuItems = [
  { title: '首页', href: '#home' },
  { title: '服务', href: '#services' },
  { title: '关于我们', href: '#about' },
  { title: '管理端', action: () => router.push('/a/dashboard') },
]

function toggleTheme() {
  cfgStore.dark = !cfgStore.dark
}
</script>

<template>
<!-- 导航栏 -->
<VAppBar elevation="1" :height="mobile ? 64 : 80">
  <VContainer class="d-flex align-center">
    <!-- Logo -->
    <div class="d-flex align-center gap-2">
      <img
        :src="logoSrc"
        alt="HR Solutions Logo"
        class="h-12 max-h-full rounded-full object-contain"
      />
      <span class="font-weight-bold">易发聚才</span>
    </div>

    <VSpacer />

    <!-- 桌面端导航菜单 -->
    <template v-if="!mobile">
      <VBtn
        v-for="item in menuItems"
        :key="item.title"
        :href="item.href"
        variant="text"
        class="text-body-1"
        @click="item.action?.() ?? null"
      >
        {{ item.title }}
      </VBtn>
      <VBtn :icon="themeIcon" variant="text" @click="toggleTheme" />
      <VBtn color="primary" variant="flat">
        联系我们
      </VBtn>
    </template>

    <!-- 移动端菜单按钮 -->
    <template v-else>
      <VBtn :icon="themeIcon" variant="text" @click="toggleTheme" />
      <VAppBarNavIcon variant="text" @click="drawer = !drawer" />
    </template>
  </VContainer>
</VAppBar>

<!-- 移动端导航抽屉 -->
<VNavigationDrawer v-if="mobile" v-model="drawer" temporary location="right">
  <VList>
    <VListItem
      v-for="item in menuItems"
      :key="item.title"
      :href="item.href"
      :title="item.title"
      @click="() => {
        drawer = false
        item.action?.()
      }"
    />
    <VDivider class="my-2" />
    <VListItem title="联系我们" href="#contact" @click="drawer = false" />
  </VList>
</VNavigationDrawer>
</template>
