<script setup lang="ts">
import { useConfigStore, useUserStore } from '@/store'

const cfgStore = useConfigStore()
const uStore = useUserStore()
</script>

<template>
<VarCell>
  <VSwitch v-model="cfgStore.dark" color="primary" label="深色模式" />
</VarCell>

<LoginInOutBtnView :login="uStore.isLogin" />
</template>
