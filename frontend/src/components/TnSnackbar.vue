<script setup lang="ts">
import type { SnackbarMessageType, SnackbarOptions } from './TnSnackbar'
import { isMobile } from '@/common/IsMobile'
import { SnackbarInjectionSymbol } from './TnSnackbar'

// 获取默认position，PC端默认center，移动端默认bottom
function getDefaultPosition(): 'top' | 'bottom' | 'center' {
  return isMobile() ? 'bottom' : 'center'
}

const position = ref<'top' | 'bottom' | 'center'>(getDefaultPosition())
// 改为直接管理激活的snackbar列表，而不是队列
const activeSnackbars = ref<{ id: number, type: string, content: string, timeout: number, visible: boolean, position?: 'top' | 'bottom' | 'center' }[]>([])
// 用于生成唯一ID
let snackbarCounter = 0

// 获取特定snackbar的位置样式
function getPositionStyle(snackbarPosition?: 'top' | 'bottom' | 'center') {
  const pos = snackbarPosition || position.value

  if (pos === 'top') {
    return { top: '16px', bottom: 'auto', transform: 'translateX(-50%)' }
  }
  if (pos === 'bottom') {
    return { bottom: '16px', top: 'auto', transform: 'translateX(-50%)' }
  }
  return { top: '50%', transform: 'translate(-50%, -50%)' }
}

// 根据类型返回对应图标
function getIconForType(type: string): string {
  switch (type) {
    case 'success': return 'mdi-check-circle-outline'
    case 'error': return 'mdi-alert-circle-outline'
    case 'warning': return 'mdi-alert-outline'
    case 'info': return 'mdi-information-outline'
    default: return 'mdi-bell-outline'
  }
}

// 关闭指定id的snackbar
function closeSnackbar(id: number) {
  const index = activeSnackbars.value.findIndex((s) => s.id === id)
  if (index >= 0) {
    // 标记为不可见
    activeSnackbars.value[index].visible = false

    // 一段时间后从列表中删除，以便动画完成
    setTimeout(() => {
      activeSnackbars.value = activeSnackbars.value.filter((s) => s.id !== id)
    }, 400)
    // 延长动画完成时间
  }
}

// 创建新的snackbar并立即显示
function showSnackbar(type: string, content?: string, options: { timeout?: number, position?: 'top' | 'bottom' | 'center' } = {}) {
  if (!content) {
    return
  }

  const id = snackbarCounter++
  const snackbar = {
    id,
    type,
    content,
    timeout: options.timeout ?? 2000,
    visible: true,
    // 处理position为void 0或null的情况，使用默认值
    position: options.position === void 0 || options.position === null ? getDefaultPosition() : options.position,
  }

  // 添加到活动列表
  activeSnackbars.value.push(snackbar)

  // 设置自动关闭
  if (snackbar.timeout > 0) {
    setTimeout(() => {
      closeSnackbar(id)
    }, snackbar.timeout)
  }
}

function success(msg?: string, options?: { position?: 'top' | 'bottom' | 'center' }) {
  showSnackbar('success', msg, { timeout: 2000, position: options?.position })
}

function error(msg?: string, options?: { position?: 'top' | 'bottom' | 'center' }) {
  showSnackbar('error', msg, { timeout: 3000, position: options?.position })
}

async function getMessageResult<T = unknown>(messageType?: SnackbarMessageType<T>) {
  if (!messageType) {
    throw new Error('message is required')
  }
  if (typeof messageType === 'string') {
    return messageType
  }
  if (messageType instanceof Promise) {
    return await messageType
  }
  if (typeof messageType !== 'function') {
    return messageType
  }
  return (messageType as unknown as () => T)()
}

async function message<T = unknown>(message: SnackbarMessageType<T>, options?: SnackbarOptions): Promise<undefined | T> {
  try {
    const ex = await getMessageResult(message)
    const strMessage = typeof ex === 'string' ? ex : void 0
    const successMsg = strMessage ?? options?.message ?? '操作完成'
    showSnackbar(options?.type ?? 'info', successMsg, { position: options?.position })
    if (typeof ex === 'string') {
      return void 0
    }
    return ex
  } catch (e) {
    options?.onError?.(e as Error)
    const errorMsg = options?.errorMessage ?? '操作失败'
    showSnackbar('error', errorMsg, { position: options?.position })
    throw e
  } finally {
    options?.onFinally?.()
  }
}

async function loading<T = unknown>(loadingMessage: SnackbarMessageType<T>, options?: SnackbarOptions): Promise<undefined | T> {
  return await message(loadingMessage, options)
}

provide(SnackbarInjectionSymbol, {
  success,
  error,
  message,
  loading,
})

defineExpose({
  success,
  error,
  message,
  loading,
})
</script>

<template>
<slot name="default" />
<!-- 分别为每个位置创建容器 -->
<template v-for="pos in ['top', 'center', 'bottom']" :key="pos">
  <div class="snackbar-container" :style="getPositionStyle(pos as 'top' | 'bottom' | 'center')">
    <TransitionGroup name="snackbar">
      <div
        v-for="snackbar in activeSnackbars.filter(s => (s.position || position) === pos)"
        :key="snackbar.id"
        class="custom-snackbar"
        :class="[`bg-${snackbar.type}`, { 'snackbar-visible': snackbar.visible }]"
      >
        <!-- 类型图标 -->
        <div class="snackbar-icon">
          <VIcon size="small" :icon="getIconForType(snackbar.type)" />
        </div>

        <div class="snackbar-content">
          <span>{{ snackbar.content }}</span>
          <VBtn
            variant="text"
            density="compact"
            icon="mdi-close"
            class="close-btn"
            @click="closeSnackbar(snackbar.id)"
          />
        </div>

        <!-- 进度条 -->
        <div
          v-if="snackbar.timeout > 0"
          class="snackbar-progress"
          :style="{ animationDuration: `${snackbar.timeout}ms` }"
        />
      </div>
    </TransitionGroup>
  </div>
</template>
</template>

<style lang="scss" scoped>
// 颜色变量
$colors: (
  success: #4caf50,
  // Material Green
  error: #f44336,
  // Material Red
  info: #2196f3,
  // Material Blue
  warning: #ffc107, // Material Amber
);

// 动画变量
$transition-bezier: cubic-bezier(0.25, 0.8, 0.25, 1);

.snackbar-container {
  @apply fixed left-1/2 z-2000 flex flex-col gap-3;
  transform: translateX(-50%);
  max-width: 80%;
  min-width: 320px;
  pointer-events: none; // 允许点击底下元素
}

.custom-snackbar {
  @apply relative rounded-md overflow-hidden flex items-stretch text-white shadow-md;
  opacity: 0;
  transform: translateY(20px) scale(0.9);
  transition:
    opacity 0.1s ease,
    transform 0.1s $transition-bezier;
  pointer-events: auto; // 恢复可点击

  &.snackbar-visible {
    opacity: 1;
    transform: translateY(0) scale(1);
  }

  // 进度条
  .snackbar-progress {
    @apply absolute left-0 bottom-0 w-full;
    height: 4px;
    background-color: rgba(255, 255, 255, 0.3);
    transform-origin: left;
    animation: progress-shrink linear forwards;
  }
}

// 内容样式
.snackbar-content {
  @apply flex-1 flex items-center justify-between;
  padding: 14px 16px;

  .close-btn {
    @apply ml-3 transition-colors duration-200;
    color: rgba(255, 255, 255, 0.8) !important;

    &:hover {
      color: white !important;
    }
  }
}

// 图标样式
.snackbar-icon {
  @apply flex items-center justify-center;
  width: 48px;
  padding: 0 8px;
}

// 生成颜色类
@each $name, $color in $colors {
  .bg-#{$name} {
    background-color: $color;
  }
}

@keyframes progress-shrink {
  0% {
    transform: scaleX(1);
  }
  100% {
    transform: scaleX(0);
  }
}
</style>
