<script setup lang="tsx">
import type { dynamic } from '@compose/types'

import type { Props } from './CopyHtmlBoard'

import { useSnackbar } from '@/components'

const props = withDefaults(defineProps<Props>(), {
  renderCss: '',
  renderTag: 'div',
  show: false,
  disableCss: false,
  defaultCss: true,
})
const css = ref<string>('')
if (props.defaultCss) {
  css.value = (await import('./inject.css?raw')).default as string
}
const popup = useSnackbar()
const rf = ref<null | HTMLElement>(null)
const styleRef = ref<null | HTMLStyleElement>(null)

async function onCopy() {
  void popup?.loading(
    async () => {
      const dom = rf.value
      if (!dom) {
        return
      }
      const oldVisibility = dom.style.visibility
      const oldDisplay = dom.style.display

      dom.style.visibility = 'visible'
      dom.style.display = 'block'

      if (styleRef.value) {
        styleRef.value.textContent = css.value
      }

      const item = new ClipboardItem({
        'text/html': new Blob([dom.innerHTML], { type: 'text/html' }),
      })

      dom.style.visibility = oldVisibility
      dom.style.display = oldDisplay

      if (styleRef.value) {
        styleRef.value.textContent = css.value
      }

      await navigator.clipboard.write([item])
    },
    { message: '复制成功，粘贴即可' },
  )
}

function RenderStyle(p: dynamic) {
  return h('style', { type: 'text/css' }, p.css)
}
</script>

<template>
<slot :onCopy="onCopy" name="activator" />
<Component
  :is="props.renderTag"
  ref="rf"
  :style="{ visibility: props.show ? 'visible' : 'hidden', display: props.show ? 'block' : 'none' }"
  class="compose-meta-ui-copy-html"
>
  <RenderStyle v-if="!props.disableCss" ref="styleRef" :css="css" />
  <slot name="default" />
</Component>
</template>
