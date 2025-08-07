<script setup lang="tsx">
import type { dynamic } from '@compose/types'

interface Props {
  active?: boolean
  width?: string
  placement?: dynamic
}
type Emits = (e: 'update:active', v: boolean) => void
const props = withDefaults(defineProps<Props>(), {
  active: false,
  width: void 0,
  placement: 'bottom',
})
const emits = defineEmits<Emits>()
const { active: _active, width: _width } = useVModels(props, emits, { passive: true })
function switchActive() {
  _active.value = !_active.value
}
defineExpose({ switchActive })
</script>

<template>
<!-- TODO 可使用 ref -->
<ElPopover trigger="click" :visible="_active" :width="_width">
  <template #reference>
    <slot :switchActive="switchActive" :active="switchActive" name="activator" />
  </template>
  <VCard>
    <slot name="title" />
    <slot name="default" />
    <VCardActions>
      <slot name="actions" />
      <VSpacer />
      <VBtn color="error" @click="switchActive">
        关闭
      </VBtn>
    </VCardActions>
  </VCard>
</ElPopover>
</template>
