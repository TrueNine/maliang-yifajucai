<script setup lang="ts">
import type { FormFieldEmits, FormFieldProps } from '@compose/ui'

interface Props extends FormFieldProps<string> {
  value?: string
}

interface Emits extends FormFieldEmits<string> {
  'update:value': [value: string]
}

const props = withDefaults(defineProps<Props>(), { value: void 0 })
const emits = defineEmits<Emits>()
const _value = useVModel(props, 'modelValue', emits, { passive: true })

const _search = ref('')
const emails = Array.from(new Set(['qq.com', 'sina.com', 'foxmail.com', '126.com', '162.com', '163.com', 'gmail.com']))
const prompts = computed(() => {
  if (!_search.value) {
    return []
  } else if (emails.includes(_search.value)) {
    return []
  } else if (_search.value.endsWith('@')) {
    return emails.map((e) => `${_search.value}${e}`)
  } else {
    const first = _search.value.split('@')[0]
    return emails.map((e) => `${first}@${e}`)
  }
})
const rules = [(v: string) => !v || v.includes('@') || '请输入正确的邮箱地址']

const isSuc = computed(() => !!_value.value)
</script>

<template>
<VAutocomplete
  v-bind="$attrs"
  v-model:search="_search"
  v-model="_value"
  :label="label"
  :placeholder="placeholder"
  :clearable="true"
  :rules="rules"
  :items="prompts"
>
  <template v-if="isSuc" #append-inner>
    <YIco class="i-mdi:success-circle text-4" />
  </template>
  <template #no-data>
    <VListItem>
      <VListItemTitle>
        {{ _search ? '请输入正确的邮箱地址' : '请输入邮箱地址' }}
      </VListItemTitle>
    </VListItem>
  </template>
</VAutocomplete>
</template>
