<script setup lang="ts">
import type { AuditTyping } from '@/__generated/model/enums'
import { computed } from 'vue'
import { VCard, VCardText, VCardTitle, VSheet } from 'vuetify/components'

interface Props {
  title?: string
  src?: string
  srcAuditStatus?: AuditTyping
  tailSrc?: string
  tailSrcAuditStatus?: AuditTyping
  alt?: string
}
interface Emits {
  (e: 'clear'): void
}
const props = withDefaults(defineProps<Props>(), {
  src: void 0,
  tailSrc: void 0,
  srcAuditStatus: void 0,
  tailSrcAuditStatus: void 0,
})
const emit = defineEmits<Emits>()
// 缓存计算结果以提升性能
const typeDescCache = computed(() => {
  if (!props.src) {
    return ['未上传', '未上传']
  }
  return props.tailSrc ? ['正面', '背面'] : ['双面', '双面']
})
const typedAuditStatus = computed(() => {
  return [
    props.srcAuditStatus,
    props.tailSrcAuditStatus,
  ]
})

const dynamicHeightCache = computed(() => {
  if (!props.tailSrc) {
    return 'h-72'
  } else {
    return 'h-24'
  }
})

const srcs = computed(() => {
  return [props.src, props.tailSrc]
})

const isEmpty = computed(() => {
  return !srcs.value.some((s) => !!s)
})

const slots = useSlots()

const previewUrls = computed(() => {
  return srcs.value.filter((v): v is string => Boolean(v))
})
const previewActive = ref(false)
const previewIndex = ref(0)

function preview(idx: number) {
  previewIndex.value = idx
  previewActive.value = !previewActive.value
}
function onClear() {
  emit('clear')
}
</script>

<template>
<!-- 创建一个卡片组件 -->
<VCard>
  <!-- 卡片标题，显示传入的title属性 -->
  <VCardTitle>
    <div class="flex justify-between">
      <div>
        {{ props.title }}
      </div>
      <VBtn v-if="!isEmpty" variant="tonal" color="error" @click="onClear">
        删除
      </VBtn>
    </div>
  </VCardTitle>
  <!-- 卡片内容区域，主要用于显示图像或提示信息 -->
  <VCardText>
    <div class="flex justify-between space-x-4">
      <div class="w-full flex space-x-2">
        <!-- 当isEmpty属性为真时，显示占位符 -->
        <template v-if="isEmpty">
          <!-- 显示占位符，包括图标和文本 -->
          <VSheet elevation="3" class="relative max-h-96 min-h-48 w-full bg-m">
            <div class="h-full w-full flex flex-col items-center justify-center">
              <YIco class="i-mdi:image-off text-12" />
              <strong>未上传对应的 {{ props.title }}</strong>
              <!-- 如果有自定义的空状态上传区域，则显示 -->
              <div class="py-4">
                <template v-if="slots['empty-upload']">
                  <slot name="empty-upload" />
                </template>
              </div>
            </div>
          </VSheet>
        </template>
        <!-- 遍历srcs数组，显示图像 -->
        <template v-for="(s, idx) in srcs" :key="idx">
          <template v-if="s">
            <!-- 显示图像，并应用动态高度 -->
            <VSheet elevation="3" color="info" class="relative max-h-96 min-h-24 w-full" :class="[dynamicHeightCache]">
              <VarImage fit="cover" :alt="props.alt" :src="s" class="h-full w-full" @click="() => preview(idx)" @error="() => console.error('Image load failed')" />
              <!-- 在图像底部显示描述信息 -->
              <div class="absolute bottom-0 h-40% w-full wh-0 flex flex items-end items-end justify-between opacity-70 from-black to-transparent bg-gradient-to-t">
                <strong class="p2 text-white">{{ typeDescCache[idx] }}</strong>
                <AuditTypingChipView :type="typedAuditStatus[idx]" />
              </div>
            </VSheet>
          </template>
        </template>
      </div>
    </div>
  </VCardText>
  <VarImagePreview v-model:show="previewActive" :images="previewUrls" :initialIndex="previewIndex" @close="previewActive = false" />
</VCard>
</template>
