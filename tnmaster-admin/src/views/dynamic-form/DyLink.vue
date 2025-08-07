<script setup lang="ts">
import type { FormFieldEmits, FormFieldProps } from '@compose/ui'

const props = withDefaults(defineProps<FormFieldProps<string>>(), {})
const emits = defineEmits<FormFieldEmits<string>>()
const _modelValue = useVModel(props, 'modelValue', emits, { passive: true })
const _label = useVModel(props, 'label', emits, { passive: true })
</script>

<template>
<VarLink textColor="primary" target="_blank" :href="_modelValue">
  {{ _label }}
</VarLink>
</template>
