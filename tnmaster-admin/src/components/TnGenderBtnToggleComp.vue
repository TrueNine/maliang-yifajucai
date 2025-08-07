<script setup lang="tsx">
import type { GenderTyping } from '@/api'

interface Props {
  modelValue?: GenderTyping
  type?: 'radio' | 'btnToggle'
  label?: string
}
type Emits = (e: 'update:modelValue', v?: GenderTyping) => void

const props = withDefaults(defineProps<Props>(), {
  modelValue: void 0,
  type: 'radio',
  label: void 0,
})

const emits = defineEmits<Emits>()
const { modelValue: _value, type: _type } = useVModels(props, emits, { passive: true })
</script>

<template>
<VBtnToggle v-if="_type === `btnToggle`" v-model="_value" color="primary">
  <VBtn value="MAN" color="blue">
    <YIco class="i-mdi:man text-6" />
    男
  </VBtn>
  <VBtn value="WOMAN" color="pink">
    <YIco class="i-mdi:woman text-6" />
    女
  </VBtn>
</VBtnToggle>
<VRadioGroup v-else-if="_type === `radio`" v-model="_value" :label="props.label" :inline="true">
  <VRadio label="男" value="MAN" color="blue" />
  <VRadio label="女" value="WOMAN" color="pink" />
</VRadioGroup>
</template>
