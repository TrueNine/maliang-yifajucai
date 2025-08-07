<script setup lang="ts">
export interface Props {
  avatarUrl?: string
  name?: string
}

const { avatarUrl = void 0, name = void 0 } = defineProps<Props>()
</script>

<template>
<VCard>
  <VRow noGutters dense>
    <VCol :cols="12" :sm="4" :md="2">
      <div class="f-c flex-col py-8">
        <VAvatar class="text-8" :image="avatarUrl" size="80" color="primary" :text="name?.slice(0, 2)" />
        <h2>
          <slot name="name" />
        </h2>
      </div>
    </VCol>

    <VCol :cols="12" :sm="8" :md="10">
      <slot name="info" />
      <slot name="certs" />
    </VCol>
  </VRow>
</VCard>
</template>
