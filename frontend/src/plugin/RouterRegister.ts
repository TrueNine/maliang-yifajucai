import type { App } from 'vue'

import { Router } from '@/router'

export function registerRouter(app: App) {
  app.use(Router)
}
