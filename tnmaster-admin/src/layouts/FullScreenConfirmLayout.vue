<script setup lang="ts">
import { useFullScreenDialog } from '@/components'

interface Slots {
  default?: () => VNode[]
  title?: () => VNode[]
  actions?: () => VNode[]
  close?: (params: { onOpen: () => void, onClose: () => void }) => VNode[]
  ok?: (params: { onOpen: () => void, onClose: () => void }) => VNode[]
}
const slots = defineSlots<Slots>()
const dialog = useFullScreenDialog()
</script>

<template>
<VCard class="wh-full px-4 py-8">
  <VCardTitle v-if="slots.title">
    <slot name="title" />
  </VCardTitle>
  <VCardText>
    <slot name="default" />
  </VCardText>
  <VCardActions v-if="slots.actions || slots.ok || slots.close">
    <slot name="actions">
      <div class="w-full px-2 py-4 space-y-2">
        <VRow>
          <VCol cols="6">
            <slot name="close" :onOpen="dialog.open" :onClose="dialog.close">
              <VBtn variant="tonal" block @click="dialog.close">
                <template #prepend>
                  <YIco class="i-mdi:keyboard-arrow-left" />
                </template>
                取消
              </VBtn>
            </slot>
          </VCol>
          <VCol cols="6">
            <slot name="ok" :onOpen="dialog.open" :onClose="dialog.close">
              <VBtn variant="tonal" color="primary" @click="dialog.close">
                确定
              </VBtn>
            </slot>
          </VCol>
        </VRow>
      </div>
    </slot>
  </VCardActions>
</VCard>
</template>
