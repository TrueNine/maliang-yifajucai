<script setup lang="ts">
import type { RouteOption } from '@compose/types'

import { generateMenu } from '@compose/external/vue-router'
import { useDisplay } from 'vuetify'
import { AllRouteTable } from '@/router'
import { usePageStore } from '@/store'

const title = useTitle()
const router = useRouter()

const menus = generateMenu(AllRouteTable, (e) => !e.meta?.hidden, 'a') as RouteOption[]

router.beforeEach((to, _, next) => {
  title.value = to.meta.title as string
  next()
})

const { mobile } = useDisplay()
const open = ref(!mobile.value)

const { progress, progressColor, progressLoading } = storeToRefs(usePageStore())
</script>

<template>
<YVSystemBar v-model:progress="progress" v-model:progressLoading="progressLoading" v-model:progressColor="progressColor" v-model:menuOpened="open">
  <template #app-title>
    {{ title }}
  </template>
  <template #drawer>
    <YVMenu v-model:opened="open" :routeMode="true" pathPrefix="a" :routes="menus" />
  </template>

  <template #settings-drawer>
    <RightDrawSettinfgsView />
  </template>

  <VContainer>
    <RouterView v-slot="{ Component }">
      <component :is="Component" />
    </RouterView>
  </VContainer>
</YVSystemBar>
</template>
