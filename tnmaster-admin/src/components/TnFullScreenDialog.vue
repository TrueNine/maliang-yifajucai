<script setup lang="ts">
import { FullScreenDialogInjectionKey } from '@/components/TnFullScreenDialog'

interface Props {
  modelValue?: boolean
  title?: string
  zIndex?: number
  confirm?: boolean
}

interface Emit {
  'update:modelValue': [value: boolean]
  'ok': []
  'cancel': []
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  confirm: false,
  zIndex: 2,
})
const emit = defineEmits<Emit>()
const _modelValue = useVModel(props, 'modelValue', emit, { passive: true })

function onCancel() {
  emit('cancel')
  _modelValue.value = false
}

function onOk() {
  emit('ok')
  _modelValue.value = false
}

provide(FullScreenDialogInjectionKey, {
  open: onOk,
  close: onCancel,
})
</script>

<template>
<VDialog v-model="_modelValue" :zIndex="props.zIndex" width="auto" height="100%" fullscreen>
  <template #activator="p">
    <slot name="activator" v-bind="p" />
  </template>
  <slot name="default" />
</VDialog>
</template>
