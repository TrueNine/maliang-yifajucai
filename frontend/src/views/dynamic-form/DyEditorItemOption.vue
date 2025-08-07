<script setup lang="ts">
import type { dynamic } from '@compose/types'
import type { ModelValueEmits, ModelValueProps } from '@compose/ui'

interface Props extends ModelValueProps<dynamic> {
  item?: dynamic
}
interface DyEmits extends ModelValueEmits<dynamic> {
  (e: 'update:emits', v: dynamic): void
}
const props = withDefaults(defineProps<Props>(), {
  item: void 0,
})
const emits = defineEmits<DyEmits>()
const { modelValue: _results, item: _item } = useVModels(props, emits, { passive: true })

const syncedModelValueAndDefaultValue = computed({
  get: () => _item.value.modelValue,
  set: (v) => {
    _item.value.modelValue = v
    _results.value.defaultValue = v
  },
})
</script>

<template>
<div v-if="_item && _results">
  <VTextField v-model="syncedModelValueAndDefaultValue" label="默认值" />
  <VTextField v-if="!_item.noLabel" v-model="_results.label" label="标签" />
  <VSwitch v-if="!_item.readonly" v-model="_results.required" color="primary" label="必填" />
  <VSwitch v-if="!_item.readonly" v-model="_results.interactive" color="primary" label="与其他项联动" />
  <VSwitch v-if="!_item.readonly" v-model="_results.repeatable" color="primary" label="不可重复" />
</div>
</template>
