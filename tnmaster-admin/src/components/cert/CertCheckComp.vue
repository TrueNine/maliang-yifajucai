<script setup lang="tsx">
import type { dynamic } from '@compose/types'
import type { CertContentTyping, CertPointTyping, CertTyping } from '@/api'

export interface Props {
  modelValue?: CertTyping
  completed?: boolean
  dialogActive?: boolean
  src?: string
  pointType?: CertPointTyping
  contentType?: CertContentTyping
}

export interface Emits {
  (e: 'update:dialogActive', value?: boolean): void

  (e: 'update:modelValue', value: CertTyping): void

  (e: 'update:pointType', value: CertPointTyping): void

  (e: 'update:contentType', value: CertContentTyping): void
}

const props = withDefaults(defineProps<Props>(), {
  completed: false,
  modelValue: void 0,
  contentType: void 0,
  src: void 0,
  pointType: void 0,
  dialogActive: false,
})

const emits = defineEmits<Emits>()

const useSlots = defineSlots<Slots>()

function createRotationRef() {
  const deg = ref(0)

  function toRight() {
    if (deg.value === 360) {
      deg.value = 0
    } else {
      deg.value += 90
    }
  }

  function toLeft() {
    if (deg.value === 0) {
      deg.value = 360
    } else {
      deg.value -= 90
    }
  }

  return {
    deg,
    toRight,
    toLeft,
  }
}

interface Slots {
  expand?: () => dynamic
  default?: (options: { p: dynamic, dialogActive: boolean }) => dynamic
  complete?: () => dynamic
  options?: () => dynamic
  footer?: () => dynamic
  actions?: (options: { dialogActive: boolean }) => dynamic
}

const { modelValue: _modelValue, pointType: _pointType, contentType: _contentType, completed: _completed } = useVModels(props, emits, { passive: true })
const _dialogActive = useVModel(props, 'dialogActive', emits, { passive: true })
const certWrapper = ref(null)

const { deg, toLeft, toRight } = createRotationRef()

function activeDialog() {
  _dialogActive.value = !_dialogActive.value
}

const { width: certWidth } = useElementSize(certWrapper)

const isExpand = !!useSlots.expand?.()
const expandShow = ref(false)

const isUseAction = computed<boolean>(() => {
  return !!useSlots.footer
})
</script>

<template>
<div>
  <VDialog v-model="_dialogActive">
    <template #activator="{ props: p }">
      <VCard v-if="isUseAction">
        <div class="relative">
          <slot name="complete">
            <div v-if="_completed" class="absolute">
              <div class="f-c flex-col bg-black bg-opacity-50">
                <div class="i-mdi:success-circle text-16 c-green" />
                <div>已提交</div>
              </div>
            </div>
          </slot>
          <div class="f-c">
            <slot v-bind="{ p, dialogActive: _dialogActive }" name="default">
              <VImg :previewDisabled="true" objectFit="cover" :src="props.src" alt="证件图片" height="144px" @click="activeDialog" />
            </slot>
          </div>
        </div>

        <VCardActions>
          <slot name="footer" />
        </VCardActions>
      </VCard>

      <template v-else>
        <slot v-bind="{ p, dialogActive: _dialogActive }" name="default">
          <VImg :previewDisabled="true" objectFit="cover" :src="props.src" alt="证件图片" height="144px" @click="activeDialog" />
        </slot>
      </template>
    </template>

    <VCard class="f-x-c">
      <VCardTitle> 编辑证件信息</VCardTitle>
      <div rounded p1>
        <VRow :dense="true">
          <VCol ref="certWrapper" :cols="9">
            <div relative>
              <div class="absolute right-0 z-1" :style="{ height: `100%` }">
                <div v-if="isExpand" class="h-full f-c">
                  <VBtn @click="expandShow = !expandShow">
                    <div class="i-mdi:keyboard-arrow-left text-8" />
                  </VBtn>

                  <div v-show="expandShow" class="h-full">
                    <div class="h-full">
                      <slot :show="expandShow" name="expand" />
                    </div>
                  </div>
                </div>
              </div>

              <VImg
                :style="{ transform: `rotate(${deg}deg)` }"
                :previewDisabled="true"
                objectFit="cover"
                :height="`${certWidth}px`"
                :width="`${certWidth}px`"
                :src="props.src"
              />
            </div>
          </VCol>

          <VCol :cols="3">
            <VCard>
              <VCardTitle> 为其标记为</VCardTitle>
              <CertCheckOptionComp v-bind="$attrs" v-model:value="_modelValue" v-model:pointType="_pointType" v-model:contentType="_contentType">
                <template #default="exp">
                  <slot name="options" :vBind="{ ...exp, dialogActive }" />
                </template>
              </CertCheckOptionComp>
              <VCardActions>
                <VSpacer />
                <slot name="actions" v-bind="{ dialogActive }" />
              </VCardActions>
            </VCard>
          </VCol>
        </VRow>
      </div>

      <VCardActions>
        <VBtn variant="text" @click="toLeft">
          <div class="i-mdi:rotate-left text-4" />
        </VBtn>
        <VBtn variant="text" @click="toRight">
          <div class="i-mdi:rotate-right text-4" />
        </VBtn>
        <VSpacer />
        <VBtn color="error" @click="_dialogActive = false">
          关闭
        </VBtn>
      </VCardActions>
    </VCard>
  </VDialog>
</div>
</template>
