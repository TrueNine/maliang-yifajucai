<script setup lang="ts">
import { useConfigStore } from '@/store'

const cfgStore = useConfigStore()
cfgStore.setTabBarPage()
</script>

<template>
<div class="wh-full">
  <div class="min-h-60vh">
    <VAppBar color="primary">
      <VSpacer />
      <VAppBarTitle> 易发聚才</VAppBarTitle>
      <VSpacer />
      <VAppBarNavIcon>
        <YIco class="i-mdi:search text-8" />
      </VAppBarNavIcon>
    </VAppBar>

    <VMain>
      <div class="h-full flex items-center justify-center">
        <VEmptyState>
          <template #media>
            <YIco class="i-mdi:flask-empty-minus text-6" />
          </template>
          暂无职位
        </VEmptyState>
      </div>
    </VMain>
  </div>
</div>
</template>
