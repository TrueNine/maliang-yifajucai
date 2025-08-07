import type { Component } from 'vue'
import { createApp } from 'vue'

import { registerAll } from '@/plugin'
import App from './App.vue'

const app = createApp(App as Component)
registerAll(app)
app.mount('#ROOT')
