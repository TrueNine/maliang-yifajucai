<script setup lang="ts">
import { api } from '@/api'

const citizenVerified = ref<boolean>()

async function getCitizenVerified() {
  citizenVerified.value = await api.userInfoV2Api.getCitizenVerified()
}

onMounted(getCitizenVerified)
const router = useRouter()
async function routeToDetailsModify() {
  await router.push('/wxpa/me/certs')
}
</script>

<template>
<VAlert v-if="!citizenVerified" variant="tonal" border="start" type="warning">
  <div class="flex items-center space-x-1">
    <div class="break-all">
      您尚未完成实民认证，这会极大地影响入职流程
    </div>
    <div>
      <VBtn color="primary" variant="tonal" @click="routeToDetailsModify">
        立即完善
      </VBtn>
    </div>
  </div>
</VAlert>
</template>
