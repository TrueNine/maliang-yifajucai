<script setup lang="ts">
import type { Emits, Props } from './FullScreenLoader'
import { FullScreenLoaderSymbol } from './FullScreenLoader'

const props = withDefaults(defineProps<Props>(), {
  delay: 3000,
  autoClose: false,
  show: false,
})

const emits = defineEmits<Emits>()
const maxDelay = Number.MAX_SAFE_INTEGER
const { show: _show, delay: _delay } = useVModels(props, emits, { passive: true })

function load() {
  _show.value = true
}

function close() {
  _show.value = false
}

provide(FullScreenLoaderSymbol, { load, close })
defineExpose({
  load,
  close,
})
</script>

<template>
<slot name="default" />
<VOverlay v-model="_show" :persistent="true" :closeOnContentClick="false" :closeDelay="props.autoClose ? _delay : maxDelay" class="f-c">
  <slot name="loader">
    <VProgressCircular color="primary" indeterminate size="128" />
  </slot>
</VOverlay>
</template>
