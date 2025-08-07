<script setup lang="tsx">
import type { FormFieldEmits, FormFieldProps } from '@compose/ui'
import { computed, watch } from 'vue'
import { useFormatChinaPhone } from '@/common/phone'

interface Props extends FormFieldProps<string> {
  region?: `+${string}`
  /** 可选的区号列表 */
  availableRegions?: Array<{ k: string, v: `+${string}` }>
}

interface Emits extends FormFieldEmits<string> {
  'update:value': [v: string]
  'regionChange': [v: `+${string}`]
}

const props = withDefaults(defineProps<Props>(), {
  value: '',
  region: '+86',
  label: '手机号',
  availableRegions: () => [
    { k: 'cn', v: '+86' },
    { k: 'us', v: '+1' },
    { k: 'jp', v: '+81' },
    { k: 'kr', v: '+82' },
    { k: 'hk', v: '+852' },
    { k: 'mo', v: '+853' },
    { k: 'tw', v: '+886' },
    { k: 'sg', v: '+65' },
    { k: 'gb', v: '+44' },
    { k: 'de', v: '+49' },
    { k: 'fr', v: '+33' },
    { k: 'ru', v: '+7' },
    { k: 'ua', v: '+380' },
    { k: 'cz', v: '+420' },
    { k: 'br', v: '+55' },
    { k: 'in', v: '+91' },
  ],
})

const emits = defineEmits<Emits>()
const __phone = useVModel(props, 'modelValue', emits, { passive: true })
const _region = useVModel(props, 'region', emits, { passive: true })
const _phone = useFormatChinaPhone(__phone)

// 使用计算属性优化regions排序，避免重复计算
const regions = computed(() => {
  return [...new Set(props.availableRegions)].sort((a, b) => {
    if (a.v.length === b.v.length) {
      return a.v.localeCompare(b.v)
    }
    return a.v.length - b.v.length
  })
})

// 使用计算属性优化当前选中的region信息获取
const currentRegion = computed(() => {
  return regions.value.find((e) => e.v === _region.value) ?? regions.value[0]
})

function focusClear() {
  if (!_phone.value?.includes('*')) {
    return
  }
  _phone.value = ''
}

// 监听region变化
watch(_region, (newRegion) => {
  emits('regionChange', newRegion)
})

/* @unocss-include */
</script>

<template>
<VRow dense>
  <VCol :cols="4">
    <VSelect
      v-model="_region"
      :items="regions"
      itemTitle="k"
      itemValue="v"
      :returnObject="false"
      density="compact"
    >
      <template #selection="s">
        {{ s.item.raw.v }}
      </template>
      <template #prepend-inner>
        <YIco :class="`i-flag:${currentRegion.k}-4x3`" />
      </template>
      <template #item="{ props: _props, item }">
        <VListItem :value="item.title" v-bind="_props">
          <template #title>
            <div class="d-flex align-center gap-2">
              <YIco :class="`i-flag:${item.title}-4x3`" />
              <span>{{ item.value }}</span>
            </div>
          </template>
        </VListItem>
      </template>
    </VSelect>
  </VCol>
  <VCol>
    <VTextField
      v-bind="$attrs"
      v-model="_phone"
      :errorMessages="errorMessages"
      :label="props.label"
      :hint="props.hint"
      density="compact"
      @focus="focusClear"
    />
  </VCol>

  <!-- 国旗初始化 -->
  <div v-if="false" v-show="false" style="visibility: hidden; display: none">
    <div class="i-flag:cn-4x3" />
    <div class="i-flag:us-4x3" />
    <div class="i-flag:jp-4x3" />
    <div class="i-flag:kr-4x3" />
    <div class="i-flag:gb-4x3" />
    <div class="i-flag:ru-4x3" />
    <div class="i-flag:ua-4x3" />
    <div class="i-flag:ru-4x3" />
    <div class="i-flag:tw-4x3" />
    <div class="i-flag:kr-4x3" />
    <div class="i-flag:mo-4x3" />
    <div class="i-flag:hk-4x3" />
    <div class="i-flag:gb-4x3" />
    <div class="i-flag:sg-4x3" />
    <div class="i-flag:br-4x3" />
    <div class="i-flag:de-4x3" />
    <div class="i-flag:cz-4x3" />
    <div class="i-flag:in-4x3" />
    <div class="i-flag:fr-4x3" />
  </div>
</VRow>
</template>

<style scoped>
.v-select :deep(.v-field__input) {
  min-height: 40px;
}
</style>
