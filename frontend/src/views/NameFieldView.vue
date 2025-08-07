<script setup lang="ts">
import type { FormFieldProps } from '@compose/ui'

interface Props extends FormFieldProps<string> {
  firstName?: string
  lastName?: string
}

type Emits = (e: 'update:firstName' | 'update:lastName', v: string) => void

const props = withDefaults(defineProps<Props>(), {
  firstName: void 0,
  lastName: void 0,
})
const emits = defineEmits<Emits>()

const { modelValue: _fullName, firstName: _firstName, lastName: _lastName } = useVModels(props, emits, { passive: true })
</script>

<template>
<TnNameInputComp
  v-bind="$attrs"
  v-model="_fullName"
  v-model:firstName="_firstName"
  v-model:lastName="_lastName"
  :hint="hint"
  :persistentHint="persistentHint"
  :errorMessages="errorMessages"
/>
</template>
