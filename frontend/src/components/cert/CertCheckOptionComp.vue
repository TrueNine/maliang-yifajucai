<script setup lang="ts">
import type { CertContentTyping, CertPointTyping, CertTyping } from '@/api'

import { certContents, certPointMaps, certs } from '@/components/cert/CertUploadList'

interface Props {
  modelValue?: CertTyping
  pointType?: CertPointTyping
  contentType?: CertContentTyping
}

interface Emits {
  (e: 'update:modelValue', t: CertTyping): void

  (e: 'update:contentType', t: CertContentTyping): void

  (e: 'update:pointType', t: CertPointTyping): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: void 0,
  pointType: 'NONE',
  contentType: 'NONE',
})
const emits = defineEmits<Emits>()
const { modelValue: _certType, contentType: _contentType, pointType: _pointType } = useVModels(props, emits, { passive: true })

watch(_certType, () => {
  _pointType.value = 'NONE'
  _contentType.value = 'NONE'
})
</script>

<template>
<div>
  <VRadioGroup v-model="_certType" label="证件" centerAffix :inline="true" color="primary">
    <template v-for="(it, _) in certs" :key="_">
      <VRadio :value="it.v" :label="it.k" />
    </template>
  </VRadioGroup>

  <template v-if="_certType">
    <VRadioGroup v-model="_pointType" label="印面类型" centerAffix :inline="true" color="primary">
      <template v-for="(it, _) in certPointMaps[_certType]" :key="_">
        <VRadio :value="it.v" :label="it.k" />
      </template>
    </VRadioGroup>
  </template>

  <VRadioGroup v-model="_contentType" label="内容" centerAffix :inline="true" color="primary">
    <template v-for="(it, _) in certContents" :key="_">
      <VRadio :value="it.v" :label="it.k" />
    </template>
  </VRadioGroup>
  <slot name="default" :value="_certType" :contentType="_contentType" :pointType="_pointType" />
</div>
</template>
