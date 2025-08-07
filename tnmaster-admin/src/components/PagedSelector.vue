<script setup lang="ts" generic="T extends Record<string, dynamic>">
import type { dynamic, Pq, Pr } from '@compose/types'
import { Pw } from '@compose/shared'
import { useDisplay } from 'vuetify'

interface Props {
  modelValue?: T
  items?: Pr<T>
  pq?: Pq
  eager?: boolean
  multiple?: boolean
  itemTitle?: keyof T
  itemValue?: keyof T
}

interface Emits {
  (e: 'update:items', value?: Pr<T>): void

  (e: 'update:pq', value?: Pq): void

  (e: 'update:modelValue', value?: T): void

  (e: 'search', searchString?: string): void

  (e: 'page', pq: Pq): void
}

const props = withDefaults(defineProps<Props>(), {
  items: void 0,
  eager: false,
  multiple: false,
  modelValue: void 0,
  pq: () => Pw.DEFAULT_MAX,
})
const emit = defineEmits<Emits>()
const { items: _items, modelValue: _modelValue, pq: _pq } = useVModels(props, emit, { passive: true })
const _nonEmptyItems = computed(() => {
  return _items.value?.d?.length ? _items.value?.d : []
})

function changePage(pq: Pq) {
  emit('page', pq)
}

const free = ref(false)
const { mobile } = useDisplay()

function onUpdateSearch(value?: string) {
  free.value = true
  _pq.value = { ..._pq.value, o: 0 }
  free.value = false
  emit('search', value)
}

onMounted(() => {
  changePage(_pq.value)
})
</script>

<template>
<VCombobox v-model="_modelValue" :multiple="props.multiple" chips returnobject :itemTitle="props.itemTitle as unknown as string" :itemValue="props.itemValue as unknown as string" :items="_nonEmptyItems" @update:search="onUpdateSearch">
  <template #prepend-item>
    <PaginatorComp v-model:freeze="free" v-model:pq="_pq" v-model:pr="_items" :totalVisible="mobile ? 4 : 7" @change="changePage" />
  </template>
</VCombobox>
</template>
