import type { LocationQuery } from 'vue-router'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

export const pageStoreKey = 'pageStore'
export const usePageStore = defineStore(
  pageStoreKey,
  () => {
    const progress = ref(0)
    const progressLoading = ref(false)
    const progressColor = ref('default')

    const lastRoutePath = ref<string>('')
    const lastRouteQuery = ref<LocationQuery>()
    const lastRouteHash = ref<string>()
    const hasLastRoute = computed(() => {
      return lastRoutePath.value !== ''
    })

    function clearLastRoute() {
      lastRoutePath.value = ''
      lastRouteQuery.value = void 0
      lastRouteHash.value = ''
    }

    return {
      progress,
      progressLoading,
      progressColor,

      hasLastRoute,
      lastRoutePath,
      lastRouteQuery,
      lastRouteHash,
      clearLastRoute,
    }
  },
  {
    persist: true,
  },
)
