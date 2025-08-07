import { defineStore } from 'pinia'
import { ref } from 'vue'

interface ServerConfig {
  chinaFirstNames?: string[]
}

export const configStoreKey = 'configStore'
export const useConfigStore = defineStore(
  configStoreKey,
  () => {
    const dark = ref(false)
    const osDark = ref(false)
    const bottomNavigationShow = ref(true)
    const activeBottomNavigation = ref<string>('home')
    const serverConfig = ref<ServerConfig>({})

    function setPlainPage() {
      bottomNavigationShow.value = false
    }

    function setTabBarPage() {
      bottomNavigationShow.value = true
    }

    return { setPlainPage, setTabBarPage, dark, osDark, bottomNavigationShow, activeBottomNavigation, serverConfig }
  },
  {
    persist: true,
  },
)
