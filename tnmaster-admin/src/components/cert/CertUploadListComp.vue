<script setup lang="ts">
import type { i32, Maybe } from '@compose/types'
import type { FormFieldEmits, FormFieldProps } from '@compose/ui'

import type { MultipleData } from './CertUploadList.ts'
import { maybeArray } from '@compose/shared'

import TnPackFileExtractor from '@/components/cert/TnPackFileExtractor.vue'
import { certContents, certPointMaps, certs, defaultData } from './CertUploadList'

export interface Props extends FormFieldProps<MultipleData[]> {
  readonly?: boolean
  noneType?: boolean
  maxSize?: number
}

export interface Emits extends FormFieldEmits<MultipleData[]> {
  (e: 'update:readonly', v?: boolean): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => [],
  readonly: false,
  noneType: false,
  maxSize: 10,
})
const emits = defineEmits<Emits>()

const _multipleData = useVModel(props, 'modelValue', emits, { passive: true })

watch(_multipleData, (v) => {
  if (props.maxSize && v.length > props.maxSize) {
    _multipleData.value = v.splice(props.maxSize)
  }
})

function getDataUrl(d: File | null) {
  if (d) {
    return URL.createObjectURL(d)
  }
}

function rmIndex(idx: number) {
  return _multipleData.value.splice(idx, 1)
}

function isImage(mediaType?: string) {
  return mediaType?.includes('image')
}

const disabledAddUploadSlots = computed(() => {
  return props.readonly || _multipleData.value.length >= props.maxSize
})

function addItems() {
  if (!disabledAddUploadSlots.value) {
    _multipleData.value.push({ ...defaultData })
  }
}

function stickup(a: ClipboardEvent, idx: i32) {
  const files = a.clipboardData?.files
  if (!files) {
    return
  }
  const fs = Array.from(files)
  for (const f of fs) {
    _multipleData.value[idx].file = f
  }
}

// 从文件列表转换为多文件数据列表的计算属性
const _filesToMultipleData = computed({
  get: () =>
    _multipleData.value
      .filter(Boolean)
      .map((e) => e.file)
      .filter(Boolean) as File[],
  set: (v) => {
    _multipleData.value = v.filter(Boolean).map((e) => {
      return {
        ...defaultData,
        file: e,
        name: e.name,
      }
    })
  },
})

function clearAll(idx: number) {
  _multipleData.value[idx].pointType = void 0
  _multipleData.value[idx].contentType = void 0
}
</script>

<template>
<VRow>
  <VCol v-for="(data, dataIdx) in _multipleData" :key="dataIdx" :cols="12" :sm="6" :md="4" :xl="3">
    <VCard class="shadow-md">
      <div class="p-1">
        <!-- 图片显示 -->
        <div v-if="data.file && isImage(data.file?.type)" class="overflow-hidden py-1">
          <div class="w-full flex items-center justify-between bg-m p1">
            <div class="text-ell">
              {{ data?.file?.name }}
            </div>
            <VBtn v-if="!props.readonly" variant="text" color="error" @click="rmIndex(dataIdx)">
              <YIco class="i-mdi:delete" />
            </VBtn>
          </div>
          <div class="f-x-c">
            <VImg :cover="true" height="300" width="300" :src="getDataUrl(data.file)" class="rounded" />
          </div>
        </div>

        <VRow dense>
          <VCol v-if="data.file == null" :cols="12">
            <VRow dense noGutters>
              <VCol :cols="11" :md="4">
                <VTextField label="粘贴图片" @paste="(e: ClipboardEvent) => stickup(e, dataIdx)" />
              </VCol>
              <VCol :cols="10" :md="6">
                <VFileInput
                  accept=".jpg,.jpeg,.png,.webp,.bmp"
                  prependIcon="mdi-image"
                  label="选择文件"
                  @update:modelValue="(e: Maybe<File>) => (_multipleData[dataIdx].file = maybeArray(e)[0])"
                />
              </VCol>
              <VCol :cols="1">
                <VBtn v-if="!props.readonly" variant="text" color="error" :block="true" @click="rmIndex(dataIdx)">
                  <YIco class="i-mdi:delete" />
                </VBtn>
              </VCol>
            </VRow>
          </VCol>
          <template v-else>
            <VCol v-if="!props.noneType" :cols="12" :sm="6" :md="4">
              <VSelect
                v-model.trim="_multipleData[dataIdx].type"
                :readonly="props.readonly"
                label="类型"
                itemTitle="k"
                itemValue="v"
                :items="certs"
                @update:modelValue="() => clearAll(dataIdx)"
              />
            </VCol>
            <VCol v-if="!props.noneType" :cols="12" :sm="6" :md="4">
              <VSelect
                v-model.trim="_multipleData[dataIdx].pointType"
                :readonly="props.readonly"
                label="印面"
                itemTitle="k"
                itemValue="v"
                :items="certPointMaps[_multipleData[dataIdx]?.type ?? 0]"
              />
            </VCol>
            <VCol v-if="!props.noneType" :cols="12" :sm="6" :md="4">
              <VSelect
                v-model.trim="_multipleData[dataIdx].contentType"
                :readonly="props.readonly"
                label="内容"
                itemTitle="k"
                itemValue="v"
                :items="certContents"
              />
            </VCol>
          </template>
        </VRow>
      </div>
    </VCard>
  </VCol>
</VRow>

<div v-if="!props.readonly">
  <VDivider class="py-2" />
  <VRow noGutters dense>
    <VCol :cols="12" :sm="11" />
    <VCol :cols="12" :sm="1">
      <VBtn class="my-4" variant="text" color="primary" :block="true" :disabled="disabledAddUploadSlots" @click="addItems">
        <div class="i-mdi:plus" />
        <div>添加上传槽位</div>
      </VBtn>
    </VCol>
  </VRow>
  <TnPackFileExtractor v-model="_filesToMultipleData" />
</div>
</template>
