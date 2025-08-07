<script setup lang="ts">
interface Props {
  modelValue?: (Blob | undefined | null)[]
}

interface Emits {
  'update:modelValue': [(Blob | undefined | null)[]]
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const files = useVModel(props, 'modelValue', emit)

const objectUrls = ref<string[]>([])

// 使用 watchEffect 管理 URL 对象的生命周期
watchEffect(() => {
  // 清理旧的 URLs
  objectUrls.value.forEach((url) => URL.revokeObjectURL(url))
  // 创建新的 URLs
  objectUrls.value = (files.value || [])
    .filter(Boolean)
    .map((f) => URL.createObjectURL(f as unknown as Blob))
})

// 组件卸载时清理 URLs
onBeforeUnmount(() => {
  objectUrls.value.forEach((url) => URL.revokeObjectURL(url))
  objectUrls.value = []
})

const currentPreviewStates = shallowRef<boolean[]>([])
const currentDataUrl = ref<string>()

function setPreviewDataUrl(idx: number) {
  currentPreviewStates.value = []
  currentPreviewStates.value[idx] = true
  currentDataUrl.value = objectUrls.value[idx]
}

// 删除单张图片
function handleDeleteImage(idx: number) {
  if (!files.value) {
    return
  }
  const newFiles = [...files.value]
  newFiles.splice(idx, 1)
  files.value = newFiles

  // 如果删除的是当前预览的图片，清除预览状态
  if (currentPreviewStates.value[idx]) {
    currentPreviewStates.value = []
    currentDataUrl.value = void 0
  }
}

// 删除所有图片
function handleDeleteAll() {
  files.value = []
  currentPreviewStates.value = []
  currentDataUrl.value = void 0
}
</script>

<template>
<div class="p-1">
  <VRow v-if="objectUrls.length">
    <VCol :cols="3">
      <div class="mb-2 flex items-center justify-between px-1">
        <div class="text-sm text-gray-600">
          图片列表
        </div>
        <VBtn
          variant="text"
          density="compact"
          color="error"
          class="text-sm"
          @click="handleDeleteAll"
        >
          <YIco class="i-mdi:delete-sweep mr-1" />
          清空
        </VBtn>
      </div>
      <div class="border border-gray-200 rounded p-2">
        <div class="max-h-[480px] overflow-y-auto">
          <template v-for="(it, idx) in files" :key="idx">
            <div
              v-if="it"
              class="group relative mb-2 cursor-pointer last:mb-0"
              :class="{
                'ring-2 ring-primary': currentPreviewStates[idx],
              }"
              @click="() => setPreviewDataUrl(idx)"
            >
              <VImg
                class="rounded"
                cover
                :aspectRatio="1.6"
                previewDisabled
                :src="objectUrls[idx]"
              />
              <div
                class="absolute right-1 top-1 z-10"
                @click.stop
              >
                <VBtn
                  variant="tonal"
                  density="comfortable"
                  color="white"
                  icon
                  size="x-small"
                  class="hover:!bg-error !bg-white/90 hover:text-white"
                  @click="handleDeleteImage(idx)"
                >
                  <YIco class="i-mdi:delete text-base text-gray-700" />
                </VBtn>
              </div>
              <div
                class="absolute inset-0 rounded bg-black/10 opacity-0 transition-all duration-200 group-hover:opacity-100"
              />
            </div>
          </template>
        </div>
      </div>
    </VCol>

    <VCol :cols="9">
      <div class="border border-gray-200 rounded p-2">
        <div v-if="currentDataUrl" class="relative h-[480px]">
          <div class="absolute inset-0 overflow-auto">
            <div class="min-h-full min-w-full flex items-center justify-center p-4">
              <VImg
                :src="currentDataUrl"
                class="rounded"
                :width="undefined"
                :height="undefined"
                draggable="false"
              />
            </div>
          </div>
        </div>
        <div v-else class="h-[480px] flex items-center justify-center">
          <div class="text-center text-gray-400">
            <YIco class="i-mdi:image-outline text-6xl" />
            <div class="mt-2">
              点击左侧缩略图预览
            </div>
          </div>
        </div>
      </div>
    </VCol>
  </VRow>

  <div v-else class="h-[480px] flex flex-col items-center justify-center border border-gray-200 rounded">
    <YIco class="i-mdi:image-outline text-8xl text-gray-400" />
    <div class="mt-4 text-gray-500">
      暂无图片
    </div>
  </div>
</div>
</template>
