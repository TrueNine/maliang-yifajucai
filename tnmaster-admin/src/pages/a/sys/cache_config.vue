<script setup lang="ts">
import type { Pq } from '@compose/types'
import type { ResponseOf } from '@/api'
import { Pw } from '@compose/shared'

import { api } from '@/api'
import { useSnackbar } from '@/components/TnSnackbar'

const pq = ref<Pq>({ ...Pw.DEFAULT_MAX })
const caches = ref<ResponseOf<typeof api.serverCacheConfigV2Api.getAllCacheConfigData>>()

async function init() {
  caches.value = await api.serverCacheConfigV2Api.getAllCacheConfigData({ pq: pq.value })
}

onMounted(init)
const chinaFirstName = ref<string>('')
const popup = useSnackbar()

function addFirstName() {
  void popup?.loading(async () => {
    await api.serverCacheConfigV2Api.patchChinaFirstName({ name: chinaFirstName.value })
  })
}

function deleteFirstName() {
  void popup?.loading(async () => {
    await api.serverCacheConfigV2Api.deleteChinaFirstName({ name: chinaFirstName.value })
  })
}
</script>

<template>
<VTextField v-model="chinaFirstName" label="姓" />
<div class="space-x-1">
  <VBtn color="primary" @click="addFirstName">
    添加百家姓
  </VBtn>
  <YPreAuthorize :hasAnyPermissions="[`ROOT`]">
    <VBtn color="error" @click="deleteFirstName">
      删除百家姓
    </VBtn>
  </YPreAuthorize>
</div>

<div py8 />
<VRow>
  <VCol :cols="2">
    配置 Key
  </VCol>
  <VCol :cols="10">
    配置 Value
  </VCol>
  <template v-if="caches && caches.d">
    <template v-for="(c, _) in caches.d" :key="_">
      <VCol :cols="2">
        {{ c?.k }}
      </VCol>
      <VCol :cols="10">
        {{ c?.v }}
      </VCol>
    </template>
  </template>
</VRow>
</template>
