import type { App } from 'vue'
import { Quasar } from 'quasar'
import quasarLang from 'quasar/lang/zh-CN'

export function registerQuasar(app: App) {
  app.use(Quasar, {
    lang: quasarLang,
  })
}
