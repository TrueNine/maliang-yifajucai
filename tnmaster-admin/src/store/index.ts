import { createPinia } from 'pinia'
import { createPersistedState } from 'pinia-plugin-persistedstate'

import { configStoreKey, useConfigStore } from './modules/ConfigStore'
import { formStoreKey, useFormStore } from './modules/FormStore'
import { pageStoreKey, usePageStore } from './modules/PageStore'
import { userStoreKey, useUserStore } from './modules/UserStore'

const Store = createPinia()
  .use(({ store }) => {
    const initialState = JSON.parse(JSON.stringify(store.$state)) as unknown as Record<string, unknown>
    store.$reset = () => {
      store.$state = JSON.parse(JSON.stringify(initialState)) as unknown as Record<string, unknown>
    }
  })
  .use(
    createPersistedState({
      serializer: {
        serialize: (value) => {
          return JSON.stringify(value, (_, v) => {
            if (v instanceof Uint8Array) {
              return `Uint8Array,${btoa(String.fromCharCode(...v))}`
            } else {
              return v as unknown
            }
          })
        },
        deserialize: (value) => {
          return JSON.parse(value, (_, v) => {
            if (typeof v === 'string' && v.startsWith('Uint8Array,')) {
              return Uint8Array.from(atob(v.substring(11)), (c) => c.charCodeAt(0)) as unknown as Record<string, unknown>
            } else {
              return v as unknown as Record<string, unknown>
            }
          }) as unknown as Record<string, unknown>
        },
      },
      storage: {
        getItem(key: string): string | null {
          return globalThis.localStorage.getItem(key)
        },
        setItem(key: string, value: string) {
          globalThis.localStorage.setItem(key, value)
        },
      },
    }),
  )

export { configStoreKey, formStoreKey, pageStoreKey, Store as Pinia, useConfigStore, useFormStore, usePageStore, userStoreKey, useUserStore }
export default Store
