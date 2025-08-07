<script setup lang="ts">
import type { RefId } from '@compose/types'
import type { api, ResponseOf } from '@/api'
import UserInfoCard from './UserInfoCard.vue'

interface Props {
  userData?: ResponseOf<typeof api.userInfoV2Api.getUserInfosAsAdmin>['d']
  isLoading: boolean
}

interface Emits {
  details: [id: RefId]
  modify: [id: RefId]
}

defineProps<Props>()
const emit = defineEmits<Emits>()

function handleDetails(infoId: RefId) {
  emit('details', infoId)
}

function handleModify(infoId: RefId) {
  emit('modify', infoId)
}
</script>

<template>
<Transition name="fade-transform" mode="out-in">
  <VRow v-if="!isLoading && userData?.length" class="p-4 transition-all duration-300 ease-in-out" dense>
    <VCol
      v-for="(it, i) in userData"
      :key="i"
      class="transition-all duration-100 ease-in-out"
      :cols="12"
      :sm="6"
      :md="4"
      :lg="3"
      :xl="2"
    >
      <UserInfoCard
        :userData="it"
        @details="handleDetails"
        @modify="handleModify"
      />
    </VCol>
  </VRow>

  <!-- Empty State -->
  <VRow v-else-if="!isLoading" class="fill-height p-4" align="center" justify="center">
    <VCol cols="12" md="6" lg="4">
      <VSheet rounded="lg" class="pa-8 text-center">
        <VIcon icon="mdi-account-search-outline" size="80" class="mb-4 text-disabled" />
        <div class="text-h6 text-medium-emphasis">
          暂无用户数据
        </div>
        <!-- Optional: Add a button to add users -->
        <!-- <VBtn color="primary" class="mt-4">添加用户</VBtn> -->
      </VSheet>
    </VCol>
  </VRow>

  <!-- Loading State Placeholder (Optional but good UX) -->
  <VRow v-else class="p-4">
    <VCol
      v-for="n in 6" :key="n"
      :cols="12" :sm="6" :md="4" :lg="3" :xl="2"
    >
      <VSkeletonLoader type="card" />
    </VCol>
  </VRow>
</Transition>
</template>

<style lang="scss" scoped>
// Keep the existing transition or use Vuetify's like v-fade-transition
.fade-transform {
  &-enter-active,
  &-leave-active {
    transition: all 0.5s ease; // Keep original duration or adjust
  }

  &-enter-from,
  &-leave-to {
    opacity: 0;
    transform: translateY(20px);
  }

  &-leave-to {
    transform: translateY(-20px); // Maintain original direction
  }
}
</style>
