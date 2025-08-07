<script setup lang="tsx">
import type { asyncable, late, latenil } from '@compose/types'
import type { FormFieldEmits, FormFieldProps } from '@compose/ui'

import type { AddressSelectItem } from './AddressSelect2'
import { AddressUtils } from '@compose/shared'
import { addressSortFn, getCache } from './AddressSelect2'

export interface Props extends FormFieldProps<AddressSelectItem[]> {
  childrenSelect?: (v: string) => asyncable<latenil<AddressSelectItem[]>>
  init?: () => asyncable<latenil<AddressSelectItem[]>>
  adCode?: string
  level?: number
  deepLevel?: number
  fullPath?: string
  disabled?: boolean
}

export interface Emits extends FormFieldEmits<AddressSelectItem[]> {
  'update:adCode': [value: string]
  'update:fullPath': [value: string]
  'update:level': [value: number]
  'update:disabled': [value: boolean]
}

const props = withDefaults(defineProps<Props>(), {
  init: () => () => [],
  childrenSelect: () => () => [],
  deepLevel: 3,
  adCode: void 0,
  level: void 0,
  fullPath: void 0,
  disabled: true,
})
const emit = defineEmits<Emits>()
const { modelValue: _value, adCode: _adCode, level: _level, fullPath: _fullPath, disabled: _disabled } = useVModels(props, emit, { passive: true })

const storeCache = useStorage<Record<string, AddressSelectItem[]>>('meta-ui__address-select-2-cache', {}, globalThis.localStorage)
const items = ref<AddressSelectItem[]>([])
const loadingFlag = ref(false)

async function _getCache(code?: string) {
  if (!code) {
    return []
  }
  _disabled.value = true
  const e = (await getCache(code, storeCache, props.childrenSelect)) ?? []
  _disabled.value = false
  return e
}

async function init() {
  loadingFlag.value = true
  const provinces = storeCache.value['00'] as late<AddressSelectItem[]>
  if (!provinces || provinces?.length === 0) {
    storeCache.value['00'] = ((await props.init?.()) ?? []).sort(addressSortFn)
    items.value = storeCache.value['00']
  } else {
    items.value = provinces
  }

  loadingFlag.value = false
  if (props.adCode) {
    await loadAdCode(props.adCode)
  }
  _disabled.value = false
}
onMounted(init)

let lock = false

async function loadAdCode(code?: string) {
  lock = true
  if (code) {
    const d = new AddressUtils(code)
    const _container: AddressSelectItem[] = []
    for (let i = 0; i < d.level; i++) {
      const _code = d.serialArray.slice(0, i + 1).join('')
      const _currentCode = d.serialArray.slice(0, i).join('') || '00'
      items.value = []
      items.value = await _getCache(_code)
      const a = storeCache.value[_currentCode]?.find((v) => v?.code === _code)
      if (a) {
        _container.push(a)
      }
    }
    _value.value = _container
    _adCode.value = code
  }
  lock = false
}

watch(_adCode, async (v) => {
  if (!v && lock) {
    return
  }
  if (v === void 0) {
    clearAddr()
    items.value = storeCache.value['00'] ?? []
    _disabled.value = false
    return
  }
  if ((v?.length ?? 0) >= 2) {
    await loadAdCode(v)
  }
})

watch(_value, async (v) => {
  if (v) {
    const level = v.length
    if (level === 0) {
      items.value = storeCache.value['00']
    } else {
      loadingFlag.value = true
      items.value = []

      if (level <= props.deepLevel) {
        const last = v[level - 1]
        _fullPath.value = v.map((v) => v.name).join('')
        _level.value = level
        // 锁住则不更新 code
        if (!lock) {
          _adCode.value = last.code
        }
        if (level < props.deepLevel) {
          items.value = (await _getCache(last.code)) ?? []
        }
      }
      loadingFlag.value = false
    }
  } else {
    items.value = storeCache.value['00']
  }
})
function clearAddr() {
  _value.value = []
  _adCode.value = void 0
  _fullPath.value = void 0
  _level.value = 0
}
</script>

<template>
<VCombobox
  v-model="_value"
  :label="label"
  :placeholder="placeholder"
  :hint="hint"
  :persistentHint="persistentHint"
  :errorMessages="errorMessages"
  clearable
  chips
  smallchips
  hideselected
  multiple
  :hideNoData="false"
  :loading="loadingFlag"
  itemTitle="name"
  :items="items"
  @click:clear="clearAddr"
>
  <template #label>
    {{ props.label }}
  </template>
  <template #no-data>
    <VListItem :disabled="_disabled">
      <VListItemTitle> {{ loadingFlag ? '加载中' : '没有数据' }}</VListItemTitle>
    </VListItem>
  </template>
</VCombobox>
</template>
