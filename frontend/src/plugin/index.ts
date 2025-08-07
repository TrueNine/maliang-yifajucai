import type { App } from 'vue'

import { registerPinia, registerQuasar, registerRouter, registerVuetify } from '@/plugin'

import { registerLibArchiveJs } from '@/plugin/LibArchiveJsRegister'

import { registerPdfJs } from '@/plugin/PdfJsRegister'
import { registerWxpaPolyfill } from '@/plugin/WxpaPolyfillRegister'

import { registerYup } from '@/plugin/YupRegister'
import { registerZod } from '@/plugin/ZodRegister'
import 'virtual:uno.css'
import '@compose/ui/dist/unocss.css'
import '@varlet/ui/es/style'

export * from './PiniaRegister'
export * from './QuasarRegister'
export * from './RouterRegister'
export * from './VConsoleRegister'
export * from './VuetifyRegister'

export function registerAll(app: App) {
  registerWxpaPolyfill()
  registerYup()
  registerZod()
  registerPinia(app)
  registerRouter(app)
  registerQuasar(app)
  registerPdfJs()
  registerLibArchiveJs()
  registerVuetify(app)
  return app
}
