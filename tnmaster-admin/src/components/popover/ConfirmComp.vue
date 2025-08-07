<script setup lang="ts">
import type { ResizeOption } from '@compose/ui'

interface Props extends ResizeOption {
  value?: boolean
  checkboxValue?: boolean
  checkboxText?: string
  doubleConfirm?: boolean
}

interface Emits {
  (e: 'update:checkboxValue' | 'update:value', value: boolean): void

  (e: 'ok' | 'cancel'): void
}

const props = withDefaults(defineProps<Props>(), {
  value: false,
  maxWidth: 600,
  minWidth: 400,
  checkboxValue: false,
  doubleConfirm: false,
  checkboxText: '我已知晓严重性',
})
const emits = defineEmits<Emits>()

const _value = useVModel(props, 'value', emits, { passive: true })
const _checkboxValue = useVModel(props, 'checkboxValue', emits, { passive: true })

function ok() {
  _value.value = false
  emits('ok')
}

const enableOk = computed(() => {
  return props.doubleConfirm ? _checkboxValue.value : true
})

function cancel() {
  _value.value = false
  emits('cancel')
}
</script>

<template>
<VDialog
  v-bind="$attrs"
  v-model="_value"
  :width="props.width"
  :maxWidth="props.maxWidth"
  :minWidth="props.minWidth"
  :height="props.height"
  :max-height="props.maxHeight"
  :minHeight="props.minHeight"
>
  <template #activator="_s">
    <slot name="activator" v-bind="_s" />
  </template>

  <template #default="_s">
    <VCard>
      <VCardTitle>
        <slot name="title" v-bind="_s">
          提示
        </slot>
      </VCardTitle>

      <VCardText>
        <slot name="default" v-bind="_s">
          <div class="f-y-c space-x-4">
            <slot name="icon">
              <YIco class="i-mdi:info-circle text-16" />
            </slot>
            <div>
              <slot name="text">
                <p>确定要进行此操作吗？</p>
              </slot>
            </div>
          </div>
        </slot>
      </VCardText>

      <VCardActions>
        <slot name="actions" v-bind="{ ..._s, ok, cancel }">
          <VCheckboxBtn v-if="props.doubleConfirm" v-model="_checkboxValue" color="error" :label="props.checkboxText" />
          <VSpacer />
          <VBtn :disabled="!enableOk" color="error" @click="ok">
            确定
          </VBtn>
          <VBtn color="primary" @click="cancel">
            取消
          </VBtn>
        </slot>
      </VCardActions>
    </VCard>
  </template>
</VDialog>
</template>
