<script setup lang="ts">
import type { dynamic } from '@compose/types'
import type { ModelValueEmits, ModelValueProps } from '@compose/ui'

const props = withDefaults(defineProps<ModelValueProps<dynamic>>(), {})
const emits = defineEmits<ModelValueEmits<dynamic>>()
const _mv = useVModel(props, 'modelValue', emits, { passive: true })
const n = computed({
  get: () => _mv.value.name,
  set: (v) => (_mv.value.name = v),
})
const d = computed({
  get: () => _mv.value.description,
  set: (v) => (_mv.value.description = v),
})
</script>

<template>
<!-- 表单描述 -->
<VExpansionPanels color="primary">
  <VExpansionPanel>
    <VExpansionPanelTitle>{{ n || '表单名称' }}</VExpansionPanelTitle>
    <VExpansionPanelText>
      <VTextField v-model="n" label="表单名称" />
      <VTextarea v-model="d" label="表备注" />
    </VExpansionPanelText>
  </VExpansionPanel>
</VExpansionPanels>
</template>
