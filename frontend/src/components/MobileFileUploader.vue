<script setup lang="ts">
import type { FormFieldEmits, FormFieldProps } from '@compose/ui'
import type { VarFile } from '@varlet/ui'

interface Props extends FormFieldProps<(File | undefined)[]> {
  accept?: string
  preview?: boolean
  multiple?: boolean
  maxSize?: number
  maxLength?: number
}
interface Emits extends FormFieldEmits<(File | undefined)[]> {
}

const props = withDefaults(
  defineProps<Props>(),
  {
    preview: true,
    accept: void 0,
    multiple: true,
    maxSize: 1024 * 1024 * 2,
    modelValue: () => ([]),
    label: '',
    placeholder: '',
  },
)
const emit = defineEmits<Emits>()

const { modelValue: _modelValue } = useVModels(props, emit, { passive: true })

const varFiles = ref<VarFile[]>([])
watch(varFiles, (value) => {
  _modelValue.value = value.map((v) => v.file)
})
</script>

<template>
<VarUploader v-model="varFiles" :previewed="props.preview" :maxlength="props.maxLength" :multiple="props.multiple" :accept="props.accept" />
</template>
