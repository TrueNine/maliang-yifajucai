<script setup lang="ts">
import { useConfigStore } from '@/store'

const router = useRouter()
const cfgStore = useConfigStore()
cfgStore.setTabBarPage()
function routeTo(path: string) {
  void router.push({
    path,
  })
}
</script>

<template>
<VRow class="p-4" :dense="true">
  <VCol :cols="12">
    <div class="f-c flex space-x-4">
      <VDivider />
      <div class="min-w-16">
        行业服务
      </div>
      <VDivider />
    </div>
  </VCol>

  <VCol :cols="4">
    <VCard @click="routeTo(`/wxpa/service/blacklist`)">
      <div v-ripple role="menuitem" class="f-y-c flex-col bg-green-5 bg-green-5 bg-opacity-20 bg-opacity-20 py-2">
        <div class="i-mdi:shield-warning-outline text-15 c-red" />
        <h3>黑名单查询</h3>
      </div>
    </VCard>
  </VCol>

  <VCol :cols="12">
    <div class="f-c flex space-x-4">
      <VDivider />
      <div class="min-w-16">
        工具服务
      </div>
      <VDivider />
    </div>
  </VCol>

  <VCol :cols="4">
    <VCard @click="routeTo(`/wxpa/service/pack`)">
      <div v-ripple role="menuitem" class="f-y-c flex-col bg-green-5 bg-opacity-20 py-2">
        <div class="i-mdi:zip-box-outline text-15 c-blue" />
        <h3>文件压缩</h3>
      </div>
    </VCard>
  </VCol>
</VRow>
</template>
