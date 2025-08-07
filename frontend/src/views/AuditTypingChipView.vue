<script setup lang="ts">
import type { AuditTyping } from '@/api'

interface Props {
  type?: AuditTyping
}
const props = withDefaults(defineProps<Props>(), {
  type: 'NONE',
})
const typeDesc = computed(() => {
  let result: {
    color?: string
    desc?: string
  } = {}
  switch (props.type) {
    case 'FAIL':
      result = {
        color: 'red',
        desc: '失败',
      }
      break
    case 'REJECT':
      result = {
        color: 'red',
        desc: '拒绝',
      }
      break
    case 'PASS':
      result = {
        color: 'green',
        desc: '通过',
      }
      break
    case 'NONE':
    default:
      result = {
        color: 'info',
        desc: '待审核',
      }
      break
  }
  return result
})
</script>

<template>
<VChip v-bind="$attrs" :color="typeDesc.color" :text="typeDesc.desc" />
</template>
