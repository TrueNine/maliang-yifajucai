<script setup lang="ts">
import { maybeArray } from '@compose/shared'

const route = useRoute()
const router = useRouter()

const code = ref(maybeArray(route.query.code)[0])
const msg = ref(maybeArray(route.query.msg)[0])
</script>

<template>
<section class="h-screen f-c flex-col content-center justify-center text-6">
  <nav>发生错误：{{ code }}</nav>
  <main>
    {{ msg }}
  </main>
  <main class="space-x-2">
    <VBtn color="primary" variant="outlined" @click="router.back()">
      <div class="i-mdi:keyboard-arrow-left" />
      <div>返回</div>
    </VBtn>
    <VBtn color="success" variant="outlined">
      <div class="i-mdi:home-outline" />
      <div>首页</div>
    </VBtn>
    <VBtn color="success" variant="outlined">
      <div class="i-mdi:code" />
      <div>登录</div>
    </VBtn>
  </main>
</section>
</template>
