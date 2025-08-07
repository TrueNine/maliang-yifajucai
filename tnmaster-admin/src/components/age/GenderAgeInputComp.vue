<script setup lang="ts">
import type { i32 } from '@compose/types'
import type { GenderTyping } from '@/api'

export interface Props {
  gender?: GenderTyping
  maxManAge?: i32
  minManAge?: i32
  maxWomanAge?: i32
  minWomanAge?: i32
}

export interface Emits {
  (e: 'update:gender', v?: GenderTyping): void

  (e: 'update:maxManAge' | 'update:minManAge' | 'update:maxWomanAge' | 'update:minWomanAge', v?: i32): void
}

const props = withDefaults(defineProps<Props>(), {
  gender: void 0,
  minManAge: 18,
  maxManAge: 60,
  minWomanAge: 18,
  maxWomanAge: 60,
})
const emits = defineEmits<Emits>()
const {
  gender: _gender,
  minWomanAge: womanMinAge,
  maxWomanAge: womanMaxAge,
  maxManAge: manMaxAge,
  minManAge: manMinAge,
} = useVModels(props, emits, { passive: true })

watch(
  () => _gender.value,
  () => {
    manMaxAge.value = void 0 as unknown as number
    manMinAge.value = void 0 as unknown as number
    womanMaxAge.value = void 0 as unknown as number
    womanMinAge.value = void 0 as unknown as number
  },
)
</script>

<template>
<div justify-center space-x-4>
  <label class="text-4">限制性别</label>
  <GenderBtnToggleComp v-model:value="_gender" type="btnToggle" />
</div>
<VRow noGutters :dense="true">
  <VCol v-if="_gender === 'MAN'" :cols="12">
    <VRow>
      <VCol>
        <VNumberInput v-model="manMinAge" controlVariant="split" label="男最小" />
      </VCol>
      <VCol>
        <VNumberInput v-model="manMaxAge" controlVariant="split" label="男最大" :min="minManAge" />
      </VCol>
    </VRow>
  </VCol>
  <VCol v-if="_gender === 'WOMAN'" :cols="12">
    <VRow>
      <VCol>
        <VNumberInput v-model="womanMinAge" controlVariant="split" label="女最小" />
      </VCol>
      <VCol>
        <VNumberInput v-model="womanMaxAge" controlVariant="split" :min="minWomanAge" label="女最大" />
      </VCol>
    </VRow>
  </VCol>
</VRow>
</template>
