<script setup lang="ts">
import type { Pq } from '@compose/types'

import type { RouteRecordNormalized } from 'vue-router'
import type { PlatformType } from '@/__generated/model/enums'
import type { ResponseOf } from '@/api'
import { arrayToPage } from '@compose/shared'
import { SHA256 } from 'crypto-js'

import { api } from '@/api'
import DiffMenuView from '@/pages/a/sys/menu/DiffMenuView.vue'

const router = useRouter()
const allRoutes = router.getRoutes()
const uniqueRoutes = Array.from(new Set(allRoutes.map((route) => route.path)))
  .map((path) => allRoutes.find((route) => route.path === path))
  .filter((it): it is RouteRecordNormalized => Boolean(it?.path))
  .toSorted((a, b) => a.path.localeCompare(b.path))

const allPath = computed(() => uniqueRoutes.map((it) => it.path).join(''))
const clientVersion = computed(() => SHA256(btoa(allPath.value)).toString())

const pq = ref<Pq>({
  s: 10,
  o: 0,
})

function getPlatform(path: string): PlatformType {
  if (path.startsWith('/a')) {
    return 'WEB_ADMIN_SITE'
  } else if (path.startsWith('/wxpa')) {
    return 'WECHAT_PUBLIC_ACCOUNT'
  } else {
    return 'PC_WEB_SITE'
  }
}

/* @unocss-include */
function getPlatformDesc(platform: PlatformType): { icon: string, title: string, color: string } {
  if (platform === 'WEB_ADMIN_SITE') {
    return {
      icon: 'i-mdi:web',
      title: '管理后台',
      color: 'primary',
    }
  } else if (platform === 'WECHAT_PUBLIC_ACCOUNT') {
    return {
      icon: 'i-mdi:wechat',
      title: '公众号',
      color: 'green',
    }
  } else {
    return {
      icon: 'i-mdi:gpu',
      title: 'PC',
      color: 'error',
    }
  }
}
interface _SelectItem {
  path: string
  title: string
  platform: PlatformType
  color: string
  icon: string
  platformTitle: string
}
const uniqueRouteRecords: _SelectItem[] = uniqueRoutes.map((it) => {
  const platform = getPlatform(it.path)
  const style = getPlatformDesc(platform)
  return {
    path: it.path,
    title: it.meta?.title as string,
    platform,
    color: style.color,
    icon: style.icon,
    platformTitle: style.title,
  }
})

const clientRoutesPage = computed(() => arrayToPage(uniqueRouteRecords, pq.value))
const serverRoutePage = ref<ResponseOf<typeof api.aclV2Api.getMenus>>()

const serverVersion = ref<string>()
async function getServerMenu() {
  serverVersion.value = await api.aclV2Api.getServerMenuVersion({ spec: {} })
  serverRoutePage.value = await api.aclV2Api.getMenus({ spec: pq.value })
}
onMounted(getServerMenu)
const diffServer = computed(() => {
  return clientVersion.value === serverVersion.value
})
const diffColor = computed(() => {
  if (serverVersion.value === '') {
    return 'error'
  }
  return diffServer.value ? 'info' : 'warning'
})
const selectPattern = ref<_SelectItem>()

interface _Item {
  id: _SelectItem
  value: boolean
  path: _SelectItem[]
}
function selectedItem(item: unknown | _Item) {
  if ((item as _Item).value) {
    selectPattern.value = (item as _Item).path[0]
  } else {
    selectPattern.value = void 0
  }
}

async function putAllMenu() {
  await api.aclV2Api.putMenusAsAdmin({
    body: uniqueRouteRecords.map((e) => {
      return {
        pattern: e.path,
        platformType: e.platform,
        title: e.title ?? '',
        requireLogin: false,
        roles: [],
        permissions: [],
      }
    }),
  })
}
</script>

<template>
<VCard>
  <VCardText>
    <VAlert :type="diffColor" :color="diffColor">
      <VAlertTitle>当前菜单版本与服务器{{ (diffServer ? '保持一致' : '不一致') }}{{ serverVersion ? '' : '，且服务器并未初始化菜单' }}</VAlertTitle>
      <div class="p-y-4 space-y-4">
        <div>客户端菜单版本：{{ clientVersion }}</div>
        <div>服务器菜单版本：{{ serverVersion || '服务器无任何菜单配置，请尽快上传菜单配置' }}</div>
      </div>
    </VAlert>
  </VCardText>
  <VCardItem>
    <VRow>
      <VCol cols="3">
        <VSheet :border="true">
          <VList v-if="clientRoutesPage.d" :items="clientRoutesPage.d" @click:select="selectedItem">
            <VListItem v-for="(it, idx) in clientRoutesPage.d" :key="it.path" color="primary" :border="true" class="bg-m" :value="it">
              <template #prepend>
                <VChip :color="it.color">
                  <div class="f-y-c space-x-2">
                    <YIco class="text-6" :class="[it.icon]" />
                    <div>{{ it.platformTitle }}</div>
                  </div>
                </VChip>
              </template>
              <template v-if="it">
                <VListItemTitle>
                  {{ it.path }}
                </VListItemTitle>
                <VListItemSubtitle>
                  {{ serverRoutePage?.d?.[idx]?.title ?? it.title ?? '...' }}
                </VListItemSubtitle>
              </template>
            </VListItem>
          </VList>
          <PaginatorComp v-model:pq="pq" v-model:pr="clientRoutesPage" />
        </VSheet>
      </VCol>
      <VCol cols="9">
        <DiffMenuView :platform="selectPattern?.platform" :color="selectPattern?.color" :platformTitle="selectPattern?.platformTitle" :icon="selectPattern?.icon" :path="selectPattern?.path" :title="selectPattern?.title">
          <template #actions>
            <VBtn color="success" @click="putAllMenu">
              更新并覆盖全部
            </VBtn>
          </template>
        </DiffMenuView>
      </VCol>
    </VRow>
  </VCardItem>
</VCard>
</template>
