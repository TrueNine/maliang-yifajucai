<script setup lang="ts">
import type { dynamic } from '@compose/types'

interface Props<T = dynamic> {
  value?: T
  items?: T[]
  around?: boolean
  hiddenBar?: boolean
}
interface Emits<T = dynamic> {
  (e: 'update:value', value: T): void
  (e: 'update:hiddenBar', hiddenBar: boolean): void
  (e: 'complete', index: number): void
  (e: 'first'): void
}
const props = withDefaults(defineProps<Props>(), {
  value: 0,
  items: () => [],
  around: true,
  hiddenBar: false,
})
const emits = defineEmits<Emits>()

const _value = useVModel(props, 'value', emits, { passive: true })
const _items = useVModel(props, 'items', emits, { passive: true })
const _hiddenBar = useVModel(props, 'hiddenBar', emits, { passive: true })

const len = computed(() => _items.value.length)
const currentIndex = computed(() => _items.value.indexOf(_value.value))
const prevDisabled = computed(() => currentIndex.value === 0)
const nextDisabled = computed(() => currentIndex.value === len.value - 1)

function next() {
  if (currentIndex.value < len.value - 1) {
    _value.value = _items.value[currentIndex.value + 1]
  }
}

function complete() {
  if (currentIndex.value === len.value - 1) {
    emits('complete', currentIndex.value)
  }
}
function first() {
  if (currentIndex.value === 0) {
    emits('first')
  }
}

function prev() {
  if (currentIndex.value > 0) {
    _value.value = _items.value[currentIndex.value - 1]
  }
}

const slotProps = {
  current: _value,
  hiddenVar: _hiddenBar,
  currentIndex,
  prevDisabled,
  nextDisabled,
  len,
  first,
  prev,
  next,
  complete,
}

defineExpose({
  first,
  prev,
  next,
  complete,
})
</script>

<template>
<div w-full>
  <slot name="top">
    <div min-h-40 />
  </slot>

  <VWindow v-model="_value">
    <slot name="default" />
  </VWindow>

  <slot name="separator">
    <div class="h-10vh" />
  </slot>

  <slot v-if="!_hiddenBar" name="bar-wrap" v-bind="slotProps">
    <div class="rounded-ts rounded-te fixed bottom-0 left-0 z-100 h-10vh w-full bg-white px-1vw py-.5vh shadow-md dark:bg-gray-800">
      <slot name="bar" v-bind="slotProps">
        <div class="h-full w-full flex items-center justify-between space-x-1">
          <div class="w-50%">
            <template v-if="prevDisabled">
              <slot name="first" v-bind="slotProps">
                <div class="flex space-x-1">
                  <div class="w-50%">
                    <slot name="first-left" />
                  </div>
                  <div class="w-50%">
                    <slot name="first-right">
                      <VBtn :block="true" @click="first">
                        返回
                      </VBtn>
                    </slot>
                  </div>
                </div>
              </slot>
            </template>
            <template v-else>
              <VBtn :block="true" @click="prev">
                上一步
              </VBtn>
            </template>
          </div>
          <div class="w-50%">
            <template v-if="!nextDisabled">
              <VBtn color="primary" :block="true" @click="next">
                下一步
              </VBtn>
            </template>
            <template v-else>
              <slot name="complete" v-bind="slotProps">
                <VBtn color="primary" :block="true" @click="complete">
                  完成
                </VBtn>
              </slot>
            </template>
          </div>
        </div>
      </slot>
    </div>
  </slot>
</div>
</template>
