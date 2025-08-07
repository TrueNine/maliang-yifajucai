import type { App } from 'vue'
import type { VuetifyOptions } from 'vuetify'
import { createVuetifyMount } from '@compose/ui'
import * as directives from 'vuetify/directives'
import 'vuetify/styles'
import 'vuetify/lib/styles/main.css'
import '@mdi/font/css/materialdesignicons.css'

export function registerVuetify(app: App) {
  app.use(
    createVuetifyMount((o) =>
      Object.assign(
        {
          directives,
          theme: {},
        } as VuetifyOptions,
        o,
      ),
    ),
  )
}
