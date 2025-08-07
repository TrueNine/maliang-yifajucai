<script setup lang="ts">
import type { RefId } from '@compose/types'
import type { api, ResponseOf } from '@/api'
import SpecFindView from './all/SpecFindView.vue'
import UserListView from './all/UserListView.vue'

const data = ref<ResponseOf<typeof api.userInfoV2Api.getUserInfosAsAdmin>>()
const isLoading = ref(true)
const router = useRouter()

async function routeToDetails(infoId: RefId) {
  await router.push({
    path: '/a/user_info/details',
    query: { infoId },
  })
}

async function routeToModify(infoId: RefId) {
  await router.push({
    path: '/a/user_info/add',
    query: { iId: infoId },
  })
}

// 监听数据变化
watch(() => data.value, (newVal) => {
  if (newVal !== void 0) {
    setTimeout(() => {
      isLoading.value = false
    }, 100)
  }
})
</script>

<template>
<div class="transition-all duration-100">
  <SpecFindView v-model:results="data" />
  <!-- 显示当前查询总数 -->
  <VCard v-show="!isLoading" class="transition-opacity duration-500" :class="{ 'opacity-0': isLoading }">
    <div class="f-c p-1">
      <h1>{{ data?.t }}</h1>
    </div>
  </VCard>

  <!-- 加载状态 -->
  <VCard v-if="isLoading" class="ma-4">
    <VCardText>
      <div class="flex flex-col items-center justify-center py-8">
        <VProgressCircular indeterminate color="primary" />
        <div class="text-subtitle-1 mt-4">
          加载中...
        </div>
      </div>
    </VCardText>
  </VCard>

  <UserListView
    :isLoading="isLoading"
    :userData="data?.d"
    @details="routeToDetails"
    @modify="routeToModify"
  />
</div>
</template>

<style lang="scss" scoped>
.fade-transform {
  &-enter-active,
  &-leave-active {
    transition: all 0.5s ease;
  }

  &-enter-from,
  &-leave-to {
    opacity: 0;
    transform: translateY(20px);
  }

  &-leave-to {
    transform: translateY(-20px);
  }
}
</style>
