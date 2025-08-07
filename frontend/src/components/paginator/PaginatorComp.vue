<script setup lang="ts">
import type { i32, latenil, Pq } from '@compose/types'
import { Pw } from '@compose/shared'

interface OPr {
  p?: latenil<number>
  t?: latenil<number>
}

export interface Props {
  pq?: Pq
  pr?: latenil<OPr>
  maxPage?: i32
  totalVisible?: i32
  /**
   * 当此属性为 true 时，将不会触发 change 事件
   */
  freeze?: boolean
  /**
   * 是否使用简洁模式（适用于移动端）
   */
  compact?: boolean
}

export interface Emits {
  'update:pq': [value: Pq]
  'change': [value: Pq]
  'update:freeze': [value: boolean]
  'update:offset': [value: i32]
  'update:pageSize': [value: i32]
}

const props = withDefaults(defineProps<Props>(), {
  pq: () => Pw.DEFAULT_MAX,
  pr: void 0,
  totalVisible: 10,
  maxPage: 6,
  freeze: false,
  compact: false,
})
const emit = defineEmits<Emits>()

const modelPage = computed({
  get: () => props.pq,
  set: (v) => {
    emit('update:pq', v)
  },
})
const modelOffset = computed({
  get: () => 1 + (modelPage.value.o ?? 0),
  set: (v) => {
    modelPage.value = { ...props.pq, o: v - 1 }
  },
})
const _freeze = useVModel(props, 'freeze', emit, { passive: true })
function clk() {
  if (_freeze.value) {
    return
  }
  emit('change', props.pq)
}

// 响应式判断是否为移动端
const isSmallScreen = useMediaQuery('(max-width: 639px)')
// 根据屏幕尺寸动态计算显示页码数
const visiblePages = computed(() =>
  isSmallScreen.value || props.compact ? Math.min(3, props.totalVisible) : props.totalVisible,
)

// 页码总数
const totalPages = computed(() => props.pr?.p ?? 1)

// 上一页
function prevPage() {
  if (modelOffset.value > 1 && !_freeze.value) {
    modelOffset.value--
    clk()
  }
}

// 下一页
function nextPage() {
  if (modelOffset.value < totalPages.value && !_freeze.value) {
    modelOffset.value++
    clk()
  }
}
</script>

<template>
<div class="paginator-container w-full">
  <!-- PC端分页 (中等屏幕及以上) -->
  <div class="paginator-desktop hidden sm:flex sm:items-center sm:justify-center">
    <VPagination
      v-bind="$attrs"
      v-model="modelOffset"
      variant="text"
      :totalVisible="visiblePages"
      :maxPages="maxPage"
      :length="totalPages"
      class="paginator-pc"
    >
      <template #item="{ key, isActive, props: p, page }">
        <VBtn
          :key="key"
          v-bind="p"
          :active="isActive"
          variant="text"
          class="pagination-btn"
          :class="{ 'is-active': isActive }"
          @click="clk"
        >
          {{ page }}
        </VBtn>
      </template>
    </VPagination>
  </div>

  <!-- 移动端简洁分页 -->
  <div class="paginator-mobile flex items-center justify-center gap-2 sm:hidden">
    <VBtn
      variant="text"
      icon="i-mdi:chevron-left"
      :disabled="modelOffset <= 1 || _freeze"
      class="mobile-nav-btn"
      @click="prevPage"
    />

    <div class="flex items-center gap-1">
      <span class="current-page">{{ modelOffset }}</span>
      <span class="text-gray-500 dark:text-gray-400">/</span>
      <span class="total-pages">{{ totalPages }}</span>
    </div>

    <VBtn
      variant="text"
      icon="i-mdi:chevron-right"
      :disabled="modelOffset >= totalPages || _freeze"
      class="mobile-nav-btn"
      @click="nextPage"
    />
  </div>
</div>
</template>

<style scoped lang="scss">
.paginator-container {
  .paginator-pc {
    .pagination-btn {
      min-width: 36px;
      height: 36px;
      transition: all 0.2s ease;

      &.is-active {
        font-weight: bold;
        color: var(--color-primary);
      }

      &:hover {
        background-color: rgba(var(--color-primary-rgb), 0.1);
      }

      @media (min-width: 768px) {
        min-width: 40px;
        height: 40px;
      }
    }
  }

  .paginator-mobile {
    .mobile-nav-btn {
      width: 36px;
      height: 36px;
      border-radius: 50%;

      &:disabled {
        opacity: 0.5;
      }
    }

    .current-page {
      font-weight: bold;
      color: var(--color-primary);
    }
  }
}

:root {
  --color-primary: #1976d2;
  --color-primary-rgb: 25, 118, 210;
}

.dark {
  --color-primary: #64b5f6;
  --color-primary-rgb: 100, 181, 246;
}
</style>
