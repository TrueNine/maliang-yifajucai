<script setup lang="tsx">
import ClientMenuView from '@/pages/a/sys/menu/ClientMenuView.vue'

enum Window {
  CLIENT_MENU,
  SERVER_MENU,
}

const currentWindow = ref(Window.CLIENT_MENU)
</script>

<template>
<VCard>
  <VCardTitle> 菜单管理 </VCardTitle>
  <VTabs v-model="currentWindow">
    <VTab :value="Window.CLIENT_MENU">
      当前客户端菜单
    </VTab>
    <VTab :disabled="true" :value="Window.SERVER_MENU">
      服务器菜单
    </VTab>
  </VTabs>
  <VWindow v-model="currentWindow">
    <VWindowItem :value="Window.CLIENT_MENU">
      <ClientMenuView />
    </VWindowItem>
    <VWindowItem :value="Window.SERVER_MENU" />
  </VWindow>
</VCard>
</template>
