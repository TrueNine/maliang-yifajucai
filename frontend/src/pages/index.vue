<script setup lang="ts">
import { useGoTo } from 'vuetify'

import AboutView from './index/AboutView.vue'
import ContactView from './index/ContactView.vue'
import FooterView from './index/FooterView.vue'
import HeaderView from './index/HeaderView.vue'
import HeroView from './index/HeroView.vue'
import ServicesView from './index/ServicesView.vue'

// 回到顶部功能
const { y } = useWindowScroll()
const goTo = useGoTo()
const showBackToTop = computed(() => y.value > 200)

function scrollToTop() {
  void goTo(0, { duration: 300 })
}
</script>

<template>
<VApp>
  <VLayout>
    <!-- 头部导航 -->
    <HeaderView />

    <VMain>
      <!-- 主要内容 -->
      <HeroView />
      <ServicesView />
      <AboutView />
      <ContactView />
      <!-- 页脚 -->
      <FooterView />
    </VMain>

    <!-- 回到顶部按钮 -->
    <VBtn
      v-show="showBackToTop"
      icon="i-mdi:arrow-up"
      color="primary"
      position="fixed"
      location="bottom end"
      class="ma-4"
      elevation="8"
      @click="scrollToTop"
    />
  </VLayout>
</VApp>
</template>
