<script setup lang="ts">
import type { timestamp } from '@compose/types'
import type { FormFieldProps } from '@compose/ui'
import type { enums } from '@/api'
import { IdcardUtils } from '@compose/shared'
import { DisTyping_CONSTANTS } from '@/api'

export interface Props extends FormFieldProps<string> {
  gender?: enums.GenderTyping
  disLevel?: 1 | 2 | 3 | 4
  disType?: enums.DisTyping
  disCode?: string
  idcardCode?: string
  adCode?: string
  birthday?: timestamp | string
}

export interface Emits {
  'blur': [ev?: Event]
  'update:persistentHint': [persistentHint?: boolean]
  'update:hint': [hint?: string]
  'update:placeholder': [placeholder?: string]
  'update:errorMessages': [messages?: string[]]
  'update:modelValue': [value: string]
  'update:gender': [gender: enums.GenderTyping]
  'update:idcardCode': [code?: string]
  'update:disCode': [code?: string]
  'update:label': [code: string]
  'update:adCode': [code: string]
  'update:birthday': [timestamp]
  'update:disLevel': [number]
  'update:disType': [enums.DisTyping]
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  birthday: void 0,
  adCode: void 0,
  idcardCode: void 0,
  disCode: void 0,
  disType: void 0,
  disLevel: void 0,
  gender: void 0,
})
const emit = defineEmits<Emits>()

const {
  label: _label,
  errorMessages: _errorMessages,
  modelValue: _code,
  disCode: _disCode,
  idcardCode: _idcardCode,
  gender: _gender,
  adCode: _adCode,
  birthday: _birthday,
  disType: _disType,
  disLevel: _disLevel,
} = useVModels(props, emit, { passive: true })

function emitCodeObject(certCode?: string) {
  _idcardCode.value = certCode?.slice(0, 18)
  const info = certCode ? IdcardUtils.getInfo(certCode) : void 0
  _adCode.value = info?.adCode
  _birthday.value = info?.birthday?.toString()
  _gender.value = certCode?.length && certCode.length >= 18 ? (Number(certCode[16]) % 2 === 0 ? 'WOMAN' : 'MAN') : void 0
  if (certCode && certCode.length >= 20) {
    _disCode.value = certCode
    _disType.value = DisTyping_CONSTANTS[Number(certCode[18]) - 1]
    _disLevel.value = Number(certCode[19]) as unknown as 1
  } else {
    _disCode.value = void 0
    _disType.value = void 0
    _disLevel.value = void 0
  }
}

watch(_code, emitCodeObject)
</script>

<template>
<VTextField
  v-model="_code"
  :errorMessages="_errorMessages" :label="_label"
  :placeholder="props.placeholder"
  :hint="props.hint"
  :persistentHint="props.persistentHint"
  @blur="() => emit(`blur`)"
/>
</template>
