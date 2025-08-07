<script setup lang="ts">
import type { GenderTyping } from '@/api'

interface Props {
  type?: GenderTyping
}

const { type = void 0 } = defineProps<Props>()

function getColor(type?: GenderTyping) {
  switch (type) {
    case 'MAN':
      return 'blue'
    case 'WOMAN':
      return 'pink'
    case 'UNKNOWN':
      return 'gray'
  }
}

const keys = [
  { k: 'MAN', v: '男' },
  { k: 'WOMAN', v: '女' },
  { k: 'UNKNOWN', v: '未知' },
]
const genderTypeComputed = computed(() => {
  return keys.find((it) => it.k === type)?.v ?? '-'
})
const genderColorComputed = computed(() => getColor(type))
const genderIcon = computed(() => {
  return type === 'MAN' ? 'i-mdi:gender-male' : 'i-mdi:gender-female'
})
</script>

<template>
<VChip :color="genderColorComputed">
  <YIco class="text-6" :class="[genderIcon]" />
  <span class="c-black-500 dark:c-white">
    {{ genderTypeComputed }}
  </span>
</VChip>
</template>
