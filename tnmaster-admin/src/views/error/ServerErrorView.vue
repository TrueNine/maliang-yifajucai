<script setup lang="ts">
interface StackTrace {
  className?: string
  moduleName?: string
  methodName?: string
  fileName?: string
  lineNumber?: number
  nativeMethod?: boolean
}

interface Traces {
  localizedMessage?: string
  message?: string
  stackTrace?: StackTrace[]
}

interface Props {
  alt?: string
  msg?: string
  code?: number
  debugSerialTrace?: Traces
}
type Emits = (e: 'ok' | 'cancel') => void
const props = withDefaults(defineProps<Props>(), {
  debugSerialTrace: void 0,
})
const emits = defineEmits<Emits>()
const trace = toRef<Traces | undefined>(props.debugSerialTrace)

const isDebug = computed(() => !!props.debugSerialTrace)

function ok() {
  emits('ok')
}
</script>

<template>
<VCardTitle>
  <div> {{ props.msg }}</div>
  <template v-if="isDebug">
    {{ trace?.message }}
    {{ trace?.localizedMessage }}
  </template>
</VCardTitle>
<VDivider />
<VCardText v-if="trace?.message" maxHeight="300">
  {{ trace?.message }}
  <VCardItem v-if="isDebug">
    <div>调试信息</div>
    <div>{{ trace?.localizedMessage }}</div>
    <div>{{ trace?.message }}</div>
    <VList>
      <template v-if="trace?.stackTrace">
        <VListItem v-for="(item, index) in trace?.stackTrace" :key="index" :value="index">
          {{ item.fileName }}: {{ item.lineNumber }}
        </VListItem>
      </template>
    </VList>
  </VCardItem>
</VCardText>
<VCardActions>
  <VBtn color="primary" @click="ok">
    确定
  </VBtn>
</VCardActions>
</template>
