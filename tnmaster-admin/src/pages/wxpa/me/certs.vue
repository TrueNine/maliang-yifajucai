<script setup lang="tsx">
import type { ResponseOf } from '@/api'
import { api } from '@/api'
import AvatarPhotoView from '@/pages/wxpa/me/cert/AvatarPhotoView.vue'
import BankcardCertView from '@/pages/wxpa/me/cert/BankcardCertView.vue'
import Discard2CertView from '@/pages/wxpa/me/cert/Discard2CertView.vue'
import EmptyValueChip from '@/pages/wxpa/me/cert/EmptyValueChip.vue'
import HouseholdCertView from '@/pages/wxpa/me/cert/HouseholdCertView.vue'
import Idcard2CertView from '@/pages/wxpa/me/cert/Idcard2CertView.vue'

type _CurrentUserInfo = ResponseOf<typeof api.userInfoV2Api.getUserInfoAsMe>
const currentUserInfo = ref<_CurrentUserInfo>()

async function getCurrentUserInfo() {
  currentUserInfo.value = await api.userInfoV2Api.getUserInfoAsMe()
}

onMounted(getCurrentUserInfo)
</script>

<template>
<div class="wh-full p-4 space-y-4">
  <VCard>
    <VCardTitle class="font-bold">
      个人信息卡
    </VCardTitle>
    <VDivider />
    <VCardText>
      <div class="flex items-center justify-between px4 py2">
        <div>姓名</div>
        <div><EmptyValueChip :name="currentUserInfo?.name" /></div>
      </div>
      <div class="flex items-center justify-between px4 py2">
        <div>手机</div>
        <div><EmptyValueChip :name="currentUserInfo?.phone" /></div>
      </div>
      <div class="flex items-center justify-between px4 py2">
        <div>身份证号</div>
        <div><EmptyValueChip :name="currentUserInfo?.idCard" /></div>
      </div>
      <div class="flex items-center justify-between px4 py2">
        <div>残疾证号</div>
        <div><EmptyValueChip :name="currentUserInfo?.disInfo?.certCode" /></div>
      </div>
    </VCardText>
  </VCard>

  <VRow>
    <VCol sm="12" md="4" xl="2" :cols="12">
      <Discard2CertView class="h-full" @complete="getCurrentUserInfo" />
    </VCol>
    <VCol sm="12" md="4" xl="2" :cols="12">
      <Idcard2CertView class="h-full" @complete="getCurrentUserInfo" />
    </VCol>
    <VCol sm="12" md="4" xl="2" :cols="12">
      <HouseholdCertView class="h-full" />
    </VCol>
    <VCol sm="12" md="4" xl="2" :cols="12">
      <AvatarPhotoView />
    </VCol>
    <VCol sm="12" md="4" xl="2" :cols="12">
      <BankcardCertView class="h-full" />
    </VCol>
  </VRow>
</div>
</template>
