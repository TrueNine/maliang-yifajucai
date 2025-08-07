<script setup lang="ts">
import type { AuditTyping } from '@/api'
import { AuditTyping_CONSTANTS } from '@/api'

interface Props {
  modelValue?: AuditTyping
}

type Emits = (e: 'change' | 'update:modelValue', value: AuditTyping) => void

const props = withDefaults(defineProps<Props>(), {
  modelValue: void 0,
})
const emits = defineEmits<Emits>()

const { modelValue: state } = useVModels(props, emits, { passive: true })

function getColor(auditStatus?: AuditTyping) {
  switch (auditStatus) {
    case 'NONE':
    case 'CANCEL':
      return ''
    case 'PASS':
      return 'success'
    case 'FAIL':
    case 'EXPIRED':
      return 'warning'
    case 'REJECT':
      return 'error'
    default:
      return 'primary'
  }
}

function watchChange(key?: AuditTyping) {
  state.value = AuditTyping_CONSTANTS.find((e) => e === key) ?? 'NONE'
  emits('change', state.value)
}

const auditStatusMaps: { k: AuditTyping, v: string }[] = [
  {
    k: 'NONE',
    v: '无',
  },
  {
    k: 'ASSIGNED',
    v: '已分配',
  },
  {
    k: 'PASS',
    v: '通过',
  },
  {
    k: 'CANCEL',
    v: '取消',
  },
  {
    k: 'CANCEL',
    v: '已取消',
  },
  {
    k: 'FAIL',
    v: '未通过',
  },
  {
    k: 'EXPIRED',
    v: '已过期',
  },
  {
    k: 'REJECT',
    v: '拒绝',
  },
]
</script>

<template>
<VBtnToggle v-model="state" :color="getColor(props.modelValue)" @update:modelValue="(e: AuditTyping) => watchChange(e)">
  <template v-for="(it, _) in auditStatusMaps" :key="_">
    <VBtn size="small" variant="text" :value="it.k">
      {{ it.v }}
    </VBtn>
  </template>
</VBtnToggle>
</template>
