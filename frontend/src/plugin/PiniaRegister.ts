import type { App } from 'vue'

import { Pinia as op } from '@/store'

export function registerPinia(app: App) {
  app.use(op)
}
