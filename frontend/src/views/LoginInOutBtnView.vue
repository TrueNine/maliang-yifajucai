<script lang="ts" setup>
import { api } from '@/api'
import { useUserStore } from '@/store'

const props = withDefaults(defineProps<Props>(), {
  block: true,
  mobile: false,
})

const userStore = useUserStore()

interface Props {
  block?: boolean
  mobile?: boolean
}

const router = useRouter()
const clr = computed(() => (userStore.isLogin ? 'error' : 'success'))
const text = computed(() => (userStore.isLogin ? '退出登录' : '登录'))
const variant = computed(() => (userStore.isLogin ? 'text' : void 0))

async function changeLoginState() {
  if (userStore.isLogin) {
    try {
      await api.authApi.logoutByAccount()
    } catch (e) {
      console.error(e)
    }
    userStore.logoutClear()
  }
  void router.push({
    path: props.mobile ? `/wxpa/auth` : `/auth`,
  })
}
</script>

<template>
<VBtn :variant="variant" :block="props.block" :color="clr" @click="changeLoginState">
  {{ text }}
</VBtn>
</template>
