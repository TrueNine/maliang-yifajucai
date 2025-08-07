<script setup lang="ts">
interface Props {
  value?: string
}

interface Emits {
  (e: 'update:value', v?: string): void
}

const props = withDefaults(defineProps<Props>(), { value: '' })
const emits = defineEmits<Emits>()

const text = useVModel(props, 'value', emits)
const separators = [',', '.', '，', '；', '：', '。']
const filterWordsSet = Array.from(new Set(['提供', '资产', '项目策划', '一般项目', '一般事项', '许可项目', '凭营业执照依法自主开展经营活动', '除依法须经批准项目外', '除依法须经批准的项目外', '凭营业执照依法自主开展经营活动', '经相关部门批准后方可开展经营活动', '依法须经批准的项目', '不含职业中介活动、劳务派遣服务']))
const cWordsSet = Array.from(new Set(['含', '的', '（', '）', '(', ')']))
const somesSet = Array.from(new Set(['不含', '开展', '不包括', '除依法', '须经批准', '除依法须', '经批准', '经批准的', '方可开展', '以相关部门批准', '凭营业执照', '非法']))
const maxLength = 15
const separatorsRegex = new RegExp(separators.map((separator) => separator.replace(/[-/\\^$*+?.()|[\]{}]/g, '\\$&')).join('|'))

function splitText(text = '') {
  return Array.from(
    new Set(
      text
        .split(separatorsRegex)
        .filter((e) => !separators.includes(e))
        .map((i) => {
          let m = i.trim()
          if (filterWordsSet.includes(m) || somesSet.some((c) => m.includes(c))) {
            return ''
          }
          cWordsSet.forEach((c) => {
            m = m.replaceAll(c, '')
          })
          return m.length <= maxLength ? m : ''
        })
        .filter(Boolean),
    ),
  )
}

const words = splitText(text.value)
</script>

<template>
<div space-x-1 space-y-1>
  <VChip v-for="(it, idx) in words" :key="idx" size="small">
    {{ it }}
  </VChip>
</div>
</template>
