<script setup lang="ts">
import type { i32, latenil } from '@compose/types'
import type { IComponentAddr } from '@compose/ui'

import { api } from '@/api'

export interface Props {
  code?: string
  level?: number
  selectedLevel?: i32
  fullPath?: string
}

export interface Emits {
  (e: 'update:code' | 'update:fullPath', codeOrFullPath?: string): void

  (e: 'update:selectedLevel' | 'update:level', selectedLevel?: i32): void
}

const props = withDefaults(defineProps<Props>(), {
  code: void 0,
  level: void 0,
  selectedLevel: void 0,
  fullPath: void 0,
})
const emits = defineEmits<Emits>()
const { code: _code, fullPath: _fullPath, selectedLevel: _selectedLevel } = useVModels(props, emits, { passive: true })

async function findProvince() {
  return await api.addressV2Api.getAllProvinces()
}

async function findByCode(code: latenil<IComponentAddr>) {
  if (!code) {
    return void 0
  }
  return await api.addressV2Api.getDirectChildrenByCode({ code: code.code })
}
</script>

<template>
<YVAddressSelect
  v-model:selectedLevel="_selectedLevel"
  v-model:fullPath="_fullPath"
  v-model:adCode="_code"
  :findProvinces="findProvince"
  :findCities="findByCode"
  :findDistricts="findByCode"
/>
</template>
