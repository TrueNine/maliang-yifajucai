<script setup lang="ts">
import type { ModelValueEmits, ModelValueProps } from '@compose/ui'

const props = withDefaults(defineProps<ModelValueProps<string>>(), {})
const emits = defineEmits<ModelValueEmits<string>>()
const mv = useVModel(props, 'modelValue', emits, { passive: true, defaultValue: '' })
</script>

<template>
<h1>{{ mv }}</h1>
</template>
