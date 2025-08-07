<script setup lang="ts">
import type { ResponseOf } from '@/api'
import { api } from '@/api'
import CitizenVerifiedAlertView from '@/pages/wxpa/me/CitizenVerifiedAlertView.vue'
import FeatureEntranceView from '@/pages/wxpa/me/FeatureEntranceView.vue'
import { useConfigStore, useUserStore } from '@/store'

type _CurrentInfo = ResponseOf<typeof api.userInfoV2Api.getUserInfoAsMe>
const cfgStore = useConfigStore()
cfgStore.setTabBarPage()

const userStore = useUserStore()

const selfUser = ref<_CurrentInfo>()

async function selectUserInfo() {
  if (userStore.isLogin) {
    selfUser.value = await api.userInfoV2Api.getUserInfoAsMe()
  }
}

onMounted(selectUserInfo)
const avatarName = computed(() => {
  return selfUser.value?.account?.nickName?.slice(1, 3)
})

const router = useRouter()
async function routeToLoginPage() {
  void router.push('/wxpa/auth')
}
async function toDetails() {
  if (userStore.isLogin) {
    void router.push('/wxpa/me/details')
  } else {
    await routeToLoginPage()
  }
}
</script>

<template>
<div class="h-full p-2">
  <VarSticky>
    <VCard class="mb-4">
      <VCardText>
        <div class="grid grid-cols-6 h-full gap-1">
          <div class="grid col-span-1 items-center">
            <VAvatar color="#9CA3AFFF" size="50">
              <span v-if="avatarName" class="text-6">{{ avatarName }}</span>
              <template v-else>
                <YIco class="i-mdi:account-network-off text-44" />
              </template>
            </VAvatar>
          </div>
          <div class="col-span-4 text-ellipsis">
            <div class="text-ellipsis text-5">
              <strong v-if="userStore.isLogin">
                {{ selfUser?.account?.nickName }}
              </strong>
              <strong v-else>未登录</strong>
            </div>
            <div v-if="userStore.isLogin" class="text-medium-emphasis">
              {{ selfUser?.account?.account ?? selfUser?.phone }}
            </div>
          </div>
          <div v-if="!userStore.isLogin" class="cols-span-1 grid items-center">
            <VBtn color="primary" variant="text" @click="routeToLoginPage">
              立即登录
            </VBtn>
          </div>
          <div v-else class="cols-span-1 grid items-center">
            <VBtn icon variant="text" @click="toDetails">
              <YIco class="text-medium-emphasis i-mdi:keyboard-arrow-right text-8" />
            </VBtn>
          </div>
        </div>
      </VCardText>
    </VCard>
  </VarSticky>

  <!-- 功能入口 -->
  <VRow dense>
    <VCol v-if="userStore.isLogin" :cols="12">
      <CitizenVerifiedAlertView />
    </VCol>
    <VCol :cols="6">
      <FeatureEntranceView title="个人证件管理" icon="i-mdi:certificate" desc="心仪职位一步到位，告别反复" iconColor="c-green-2" route="/wxpa/me/certs" />
    </VCol>
    <VCol :cols="6">
      <FeatureEntranceView title="预设入职标准" icon="i-mdi:note" iconColor="c-blue-2" desc="避免反复填写信息，一步到位。" route="/wxpa/me/resume" />
    </VCol>
    <VCol :cols="6">
      <FeatureEntranceView divider iconColor="dark:c-white c-black" icon="i-mdi:theme-light-dark" title="夜间模式">
        <template #desc-text>
          <VarSwitch v-model="cfgStore.dark" />
        </template>
      </FeatureEntranceView>
    </VCol>
    <VCol :cols="6">
      <FeatureEntranceView icon="i-mdi:feedback" iconColor="dark:c-#A769DFFF c-#743AA8FF" divider title="意见反馈" desc="我们真的有在听取意见" />
    </VCol>
    <YPreAuthorize :hasAnyPermissions="[`ADMIN`]">
      <VCol :cols="12">
        <FeatureEntranceView icon="i-mdi:code" iconColor="dark:c-red-4 c-red-7" divider title="后台管理端" desc="管理员专属页面" route="/a/dashboard" />
      </VCol>
    </YPreAuthorize>
  </VRow>

  <!-- 版权信息 -->
  <div class="text-medium-emphasis my-24 flex flex-col items-center justify-center">
    <a target="_blank" class="text-medium-emphasis" href="https://beian.miit.gov.cn/">
      湘ICP备2024047530号-1
    </a>
    <a target="_blank" href="https://github.com/TrueNine" class="text-medium-emphasis flex items-center text-ellipsis text-3 space-x-1">
      <i>Powered By TrueNine</i>
      <YIco class="i-mdi:github text-6" />
    </a>
    <i class="text-3">@Copyright 2023 湖南慕残信息科技有限公司</i>
  </div>
</div>
</template>
