<script setup lang="tsx">
import type { asyncable, late, latenil } from '@compose/types'
import type { FormFieldEmits, FormFieldProps } from '@compose/ui'

import type { AddressSelectItem } from '@/components/address/AddressSelect2'
import { api } from '@/api'
import AddressSelect2Comp from '@/components/address/AddressSelect2Comp.vue'

export interface Props extends FormFieldProps<AddressSelectItem[]> {
  childrenSelect?: (v: string) => asyncable<latenil<AddressSelectItem[]>>
  init?: () => asyncable<latenil<AddressSelectItem[]>>
  adCode?: string
  level?: number
  deepLevel?: number
  fullPath?: string
  disabled?: boolean
}

export interface Emits extends FormFieldEmits<AddressSelectItem[]> {
  'update:adCode': [value: string]
  'update:fullPath': [value: string]
  'update:level': [value: number]
  'update:disabled': [value: boolean]
}

const props = withDefaults(defineProps<Props>(), {
  childrenSelect: void 0,
  init: void 0,
  adCode: void 0,
  level: void 0,
  deepLevel: void 0,
  fullPath: void 0,
})
const emit = defineEmits<Emits>()
const _adCode = useVModel(props, 'adCode', emit, { passive: true })

async function findProvince() {
  return await api.addressV2Api.getAllProvinces()
}

async function findByCode(code: string) {
  const a = await api.addressV2Api.getDirectChildrenByCode({ code })
  return a ?? []
}

const _fullPath = computed({
  get: () => props.fullPath,
  set: (v: late<string>) => emit('update:fullPath', v ?? ''),
})
</script>

<template>
<AddressSelect2Comp
  v-bind="$attrs"
  v-model:fullPath="_fullPath"
  v-model:adCode="_adCode"
  :errorMessages="errorMessages"
  :hint="hint"
  :persistentHint="persistentHint"
  :label="label"
  :placeholder="placeholder"
  :childrenSelect="findByCode"
  :init="findProvince"
/>
</template>
