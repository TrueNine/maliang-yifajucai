<script setup lang="ts">
import type { DisTyping } from '@/api'

interface Props {
  type?: DisTyping
  level?: number
}

const { type = void 0, level = void 0 } = defineProps<Props>()

function getColor(type?: DisTyping) {
  switch (type) {
    case 'EYE':
      return '#ff0000'
    case 'EAR':
      return '#ffff00'
    case 'MOUTH':
      return '#0000ff'
    case 'BODY':
      return '#00ff00'
    case 'IQ':
      return '#800080'
    case 'NERVE':
      return '#ffa500'
    case 'MULTIPLE':
      return '#a52a2a'
    case void 0:
      return 'gray'
  }
}

const keys = [
  { k: 'EYE', v: '视力' },
  { k: 'EAR', v: '听力' },
  { k: 'MOUTH', v: '言语' },
  { k: 'BODY', v: '肢体' },
  { k: 'IQ', v: '智力' },
  { k: 'NERVE', v: '精神' },
  { k: 'MULTIPLE', v: '多重' },
]
const disTypeNameComputed = computed(() => {
  return keys.find((it) => it.k === type)?.v ?? '正常'
})
const disColorComputed = computed(() => getColor(type))
</script>

<template>
<VChip :color="disColorComputed">
  <span class="c-black-500 dark:c-white">
    {{ disTypeNameComputed }}{{ type ? level : '' }}
  </span>
</VChip>
</template>
