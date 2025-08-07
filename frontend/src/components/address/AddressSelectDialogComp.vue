<script setup lang="ts">
import type { inst, nil } from '@compose/types'
import { ElPopover } from 'element-plus'

export interface Props {
  code?: string
  fullPath?: string
  active?: boolean
  label?: string
}

export type Emits = (e: 'update:active', v: boolean) => void

const props = withDefaults(defineProps<Props>(), {
  code: void 0,
  fullPath: void 0,
  active: false,
  label: '地址',
})
const emits = defineEmits<Emits>()
const { active: _active, code: _code, label: _label, fullPath: _fullPath } = useVModels(props, emits, { passive: true })

const ppr = ref<nil<inst<typeof ElPopover>>>(null)
const _selectedLevel = ref(0)
function clear() {
  _code.value = ``
  _selectedLevel.value = 0
}
</script>

<template>
<div f-y-c>
  <ElPopover ref="ppr" :visible="_active" width="360px" placement="bottom" trigger="click">
    <template #reference>
      <VBtn color="primary" @click="_active = true">
        {{ (_code ? '重新选择' : '选择') + _label }}
      </VBtn>
    </template>
    <div class="w-full flex">
      <VBtn variant="text" @click="_active = false">
        取消
      </VBtn>
      <VSpacer />
      <VBtn variant="text" color="error" @click="clear">
        清空
      </VBtn>
      <VBtn variant="text" color="primary" @click="_active = false">
        确定
      </VBtn>
    </div>
    <AddressSelectComp v-model:code="_code" v-model:fullPath="_fullPath" v-model:selectedLevel="_selectedLevel" />
  </ElPopover>
  <div text-ell>
    {{ _fullPath }}
  </div>
</div>
</template>
