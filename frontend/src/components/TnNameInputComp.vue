<script setup lang="ts">
import type { FormFieldEmits, FormFieldProps } from '@compose/ui'
import { splitName } from './TnNameInput'

export interface Props extends FormFieldProps<string> {
  firstName?: string
  lastName?: string
}

export interface Emits extends FormFieldEmits<string> {
  'update:firstName': [v: string]
  'update:lastName': [v: string]
  'blur': [v?: Event]
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  firstName: '',
  lastName: '',
  label: '姓名',
  placeholder: '双姓以 空格分开',
})

const emit = defineEmits<Emits>()
const { modelValue: _fullName, firstName: _firstName, lastName: _lastName } = useVModels(props, emit, { passive: true })
if (_firstName.value) {
  _fullName.value += _firstName.value
}
if (_lastName.value) {
  _fullName.value += _lastName.value
}

const compositionLock = ref(false)

function compositionStart() {
  compositionLock.value = true
}

function compositionEnd() {
  compositionLock.value = false
  updateValue()
}

function updateValue() {
  if (!compositionLock.value) {
    const { firstName, lastName } = splitName(_fullName.value)
    _firstName.value = firstName
    _lastName.value = lastName
  }
}

watch(_fullName, updateValue)

function focusClear() {
  if (_fullName.value.includes('*')) {
    _fullName.value = ''
  }
}
function onBlur(e?: Event) {
  emit('blur', e)
}
</script>

<template>
<VTextField
  v-model="_fullName"
  :rules="[]"
  :hint="hint"
  :persistentHint="props.persistentHint"
  clearable
  :errorMessages="props.errorMessages"
  :label="label"
  :placeholder="props.placeholder"
  @focus="focusClear"
  @blur="onBlur"
  @compositionstart="compositionStart"
  @compositionend="compositionEnd"
/>
</template>
