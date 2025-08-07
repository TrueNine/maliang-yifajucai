<script setup lang="ts">
import { range } from '@compose/shared'

import { getColor } from '@/components/dis/DisCommon'

/**
 * 服务器传递的可能 u8 字节数组序列
 * - Uint8Array 不太可能，但组件可以接受
 * - number[] 直接的每个字节的数组表示方法
 * - string[] 类似 number[] 但为 string[] 表示
 * - string 为直接的 base64 字符串，需要解码
 */
type ServerByteArray = Uint8Array | number[] | string

const props = withDefaults(defineProps<Props>(), {
  title: '选择残障类别',
  rules: () => Array.from({ length: 28 }).map(() => !1),
  modelValue: () => new Uint8Array(28).map(() => 0),
})

const emits = defineEmits<Emits>()

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

export interface Props {
  title?: string
  rules?: boolean[]
  modelValue?: ServerByteArray
}

export interface Emits {
  (e: 'update:modelValue', checked: Uint8Array): void

  (e: 'update:rules', checked: boolean[]): void
}

const _fixedRules = ref({
  1: !1,
  2: !1,
  3: !1,
  4: !1,
  5: !1,
  6: !1,
  7: !1,
})
const _disTypeNames = ['视力', '听力', '言语', '肢体', '智力', '精神', '多重']

const pubRules = useVModel(props, 'rules', emits, { passive: true })
const _modelValue = useVModel(props, 'modelValue', emits, { passive: true })
if (_modelValue.value.length !== 28) {
  _modelValue.value = new Uint8Array(28).map(() => 0)
}
pubRules.value = [...u8Converter(_modelValue.value)].map((e) => !!e)
const _u8 = computed({
  set: (v) => {
    pubRules.value = [...v].map((e) => !!e)
    _modelValue.value = new Uint8Array(v.map((e) => (e ? 1 : 0)))
  },
  get: () => {
    return u8Converter(_modelValue.value)
  },
})

watch(
  pubRules,
  (v) => {
    _modelValue.value = new Uint8Array(v.map((e) => (e ? 1 : 0)))
  },
  { deep: true },
)
function updateValue(v: boolean, key: number) {
  const idx = key * 4
  const newArr = [...pubRules.value]
  for (let i = idx; i < idx + 4; i++) {
    newArr[i] = v
  }
  pubRules.value = newArr
}

function selectLevel(v: boolean, key: number) {
  const newArr = [...pubRules.value]
  newArr.forEach((_, i) => {
    if (i % 4 === key - 1) {
      newArr[i] = v
    }
  })
  pubRules.value = newArr
}

function selectAll(b: boolean) {
  pubRules.value = pubRules.value.map(() => b)
}

const _a = ref<Record<string, boolean>>({
  1: !1,
  2: !1,
  3: !1,
  4: !1,
})

const _allSelect = ref(false)
</script>

<template>
<VExpansionPanels>
  <VExpansionPanel :title="props.title">
    <template #title>
      <header class="f-y-c flex space-x-2">
        <div>{{ props.title }}</div>
        <div>
          <DisChipComp :rules="_u8" />
        </div>
      </header>
    </template>

    <template #text>
      <VRow noGutters dense>
        <VCol :cols="4">
          <VCheckbox v-model="_allSelect" label="全选" @update:modelValue="v => selectAll(v!)" />
        </VCol>
        <VCol v-for="idx in range(1, 4)" :key="idx" cols="2">
          <VCheckbox v-model="_a[idx.toString()]" :label="`${idx}`" @update:modelValue="v => selectLevel(v!, idx)" />
        </VCol>
        <VCol cols="12">
          <VDivider />
        </VCol>

        <template v-for="(_, key) in _fixedRules" :key="key">
          <!-- 残障类别 -->
          <VCol cols="4">
            <VCheckbox
              v-model="_fixedRules[key]"
              :baseColor="getColor(Number(key) - 1)"
              :color="getColor(Number(key) - 1)"
              :label="_disTypeNames[Number(key) - 1]"
              @update:modelValue="v => updateValue(v!, Number(key) - 1)"
            />
          </VCol>
          <!-- 级别 -->
          <VCol v-for="s in range((Number(key) - 1) * 4, (Number(key) - 1) * 4 + 3)" :key="s" cols="2">
            <VCheckbox v-model="pubRules[s]" :baseColor="getColor(Math.floor(s / 4))" :color="getColor(Math.floor(s / 4))" :label="`${(s % 4) + 1}`" />
          </VCol>

          <!-- 分割线 -->
          <VCol cols="12">
            <VDivider />
          </VCol>
        </template>
      </VRow>
    </template>
  </VExpansionPanel>
</VExpansionPanels>
</template>
