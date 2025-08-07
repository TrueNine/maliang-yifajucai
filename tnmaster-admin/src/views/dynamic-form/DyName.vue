<script setup lang="ts">
import type { FormFieldEmits, FormFieldProps } from '@compose/ui'
import { string } from 'yup'

const props = withDefaults(defineProps<FormFieldProps<string>>(), {
  defaultValidateSchema: () => string().optional().nullable().min(2, '姓名过短').max(4, '姓名过长'),
})
const emits = defineEmits<FormFieldEmits<string>>()

const name = useVModel(props, 'modelValue', emits, { passive: true })
</script>

<template>
<TnNameInputComp v-model="name" :label="label" :errorMessages="props.errorMessages" :placeholder="props.placeholder" />
</template>
