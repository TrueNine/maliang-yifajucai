<script setup lang="ts">
import { disNames, getColor as externalGetColor } from '@/components/dis/DisCommon'

export interface Props {
  rules?: Uint8Array | number[] | string
}

export type Emits = object

type ServerByteArray = Uint8Array | number[] | string

const props = withDefaults(defineProps<Props>(), {
  rules: () => [],
})

function u8Converter(u8: ServerByteArray) {
  if (u8 instanceof Uint8Array) {
    return u8
  } else if (typeof u8 === 'string') {
    return Uint8Array.from(atob(u8), (c) => c.charCodeAt(0))
  } else if (Array.isArray(u8)) {
    return Uint8Array.from(u8)
  } else {
    return new Uint8Array(28).map(() => 0)
  }
}

const ruleBools = ref<boolean[]>([])
watch(
  () => props.rules,
  (v) => {
    const rs = u8Converter(v)
    ruleBools.value = [...rs].map((e) => !!e)
  },
  { deep: true, immediate: true },
)

function getNames(ab: boolean[]) {
  const sliced = ab.slice(0, 4)
  if (sliced.every(Boolean)) {
    return '全接受'
  } else {
    return ab
      .map((e, i) => ({ [i]: e }))
      .filter((e, i) => e[i])
      .map((e) => {
        return Number(Object.keys(e)) + 1
      })
      .flatMap((e) => e)
      .join('、')
  }
}

const getColor = (num: number) => externalGetColor(num)

function getFns(ab: boolean[]) {
  if (ab.every((e) => !e)) {
    return {
      0: { v: '', color: 'gray' },
    }
  }
  const sliced = ab.slice(0, 28)
  const abs: Record<number, { v: string, color: string }> = {}
  for (let i = 0; i < sliced.length; i++) {
    const j = i + 1
    if (j % 4 === 0 && sliced.slice(j - 4, j).some(Boolean)) {
      const sep = sliced.slice(j - 4, j)
      const level = j / 4
      abs[level] = { v: getNames(sep), color: getColor(level - 1) }
    }
  }
  return abs
}

const matchRules = computed(() => {
  const d = getFns(ruleBools.value)
  return Object.entries(d)
    .map(([k, ac]) => {
      return { [disNames[Number(k)]]: ac }
    })
    .reduce((acc, cur) => {
      return { ...acc, ...cur }
    }, {}) as Record<string, { v: string, color: string }>
})
</script>

<template>
<VChip v-for="(v, k) in matchRules" :key="k" size="small" :color="v.color">
  {{ k }} {{ v.v }}
</VChip>
</template>
