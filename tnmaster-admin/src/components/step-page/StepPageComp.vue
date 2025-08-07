<script setup lang="ts">
import type { asyncable, dynamic, i32, late } from '@compose/types'
import { findSlotNodesByName } from '@compose/external/vue'
import { useDisplay } from 'vuetify'

interface Props {
  disableNext?: boolean
  step?: i32
  loop?: boolean
  nextValid?: (step: i32) => asyncable<late<boolean>>
}

type Emits = (e: 'ok' | 'clear' | 'update:step', step: i32) => void
type ItemSlot = () => dynamic

interface IS {
  default?: () => dynamic
  footer?: () => dynamic
}

type Br = Record<`item-${number}`, ItemSlot>
type Slots = Br & IS

const props = withDefaults(defineProps<Props>(), {
  disableNext: false,
  nextValid: () => true,
  step: 0,
  loop: true,
})

const emits = defineEmits<Emits>()

defineSlots<Slots>()
const slots = useSlots()

const _disableNext = computed(() => {
  return props.disableNext
})

const _step = useVModel(props, 'step', emits, { passive: true })
const defaultSlots = slots.default?.()
const stepNodes = findSlotNodesByName('StepHeaderComp', defaultSlots)
const hasPrev = computed(() => _step.value > 0)
const maxStep = computed(() => stepNodes.length - 1)
const isLastStep = computed(() => _step.value >= maxStep.value)

function clear() {
  emits('clear', _step.value)
}

function prevStep() {
  if (hasPrev.value) {
    _step.value -= 1
  }
}

stepNodes.forEach((e, i) => {
  if (!e.props) {
    e.props = { modelValue: i }
  } else {
    e.props.modelValue = i
  }
})

function calcStep(s: number) {
  return s
}

function calcStepColor(s: number) {
  const step = calcStep(s)
  if (step === _step.value) {
    return 'primary'
  } else if (step < _step.value) {
    return 'success'
  } else {
    return 'default'
  }
}

async function nextStep() {
  const validFn = props.nextValid
  const result = await validFn(_step.value)
  if (!result) {
    return
  }
  if (isLastStep.value) {
    emits('ok', _step.value)
    if (props.loop) {
      _step.value = 0
    }
  }
  _step.value += 1
}

const { mobile } = useDisplay()
</script>

<template>
<VStepper v-model="_step" :mobile="mobile">
  <VStepperHeader>
    <slot v-if="false" name="default" />
    <template v-for="(it, idx) in stepNodes" :key="idx">
      <component
        v-bind="{
          color: calcStepColor(idx),
          editable: calcStep(idx) === _step,
          complete: _step > calcStep(idx),
        }"
        :is="it"
      />
      <VDivider v-if="idx < stepNodes.length - 1" :color="calcStep(idx) < _step ? `success` : `default`" />
    </template>
  </VStepperHeader>

  <VStepperWindow v-model="_step">
    <template v-for="(it, idx) in stepNodes" :key="idx">
      <VStepperWindowItem :value="calcStep(idx)">
        <VCard v-if="mobile && it?.props?.title" class="my-4">
          <VCardTitle>{{ it?.props?.title }}</VCardTitle>
          <template v-if="it?.props?.subtitle">
            {{ it?.props?.subtitle }}
          </template>
        </VCard>
        <!-- FIXME 动态高度 -->
        <div class="min-h-100">
          <slot :name="`item-${calcStep(idx)}`" />
        </div>
      </VStepperWindowItem>
    </template>
  </VStepperWindow>

  <slot name="footer" :prevDisabled="hasPrev" :prevStep="prevStep" :nextStep="nextStep" :clear="clear" :ok="isLastStep">
    <footer class="w-full flex p-2 space-x-2">
      <VBtn :disabled="!hasPrev" @click="prevStep">
        上一步
      </VBtn>
      <VSpacer />
      <VBtn type="reset" variant="text" color="error" @click="clear">
        <YIco class="i-mdi:delete" />
        清空
      </VBtn>
      <VBtn type="submit" :disabled="_disableNext" color="primary" @click="nextStep">
        {{ isLastStep ? '完成' : '下一步' }}
      </VBtn>
    </footer>
  </slot>
</VStepper>
</template>
