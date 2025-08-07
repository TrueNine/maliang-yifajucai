<script setup lang="ts">
import type { late } from '@compose/types'
import type { entities, enums, ResponseOf } from '@/api'
import { formatDate } from '@compose/external/dayjs'
import { api, getDisTypingDesc, getGenderTypingDesc } from '@/api'
import InfoCardView from './details/InfoCardView.vue'
import UserCertsCard from './details/UserCertsCard.vue'
import UserInfoCard from './details/UserInfoCard.vue'

type CertView = ResponseOf<typeof api.certV2Api.getUserInfoWatermarkCerts>[number]
const route = useRoute()
const infoId = ref(route.query.infoId as late<string>)
const queryError = computed(() => {
  return infoId.value == null
})
const info = ref<entities.Dynamic_UserInfo>()
const init = ref<boolean>(false)

const allCerts = ref<CertView[]>([])
const copyText = ref<string>('')

onMounted(async () => {
  info.value = await api.userInfoV2Api.getUserInfoDetailsByUserInfoIdAsAdmin({
    userInfoId: infoId.value as unknown as number,
  })
  if (info.value?.id) {
    allCerts.value = await api.certV2Api.getUserInfoWatermarkCerts({
      userInfoId: info.value.id as unknown as number,
    })
  }

  copyText.value = [
    { 姓名: info.value?.name },
    { 性别: getGenderTypingDesc(info.value?.gender) },
    { 年龄: info.value?.age },
    { 出生日期: info.value?.birthday ? formatDate(info.value?.birthday) : '' },
    { 身份证号码: info.value?.idCard },
    { 手机号: info.value?.phone },
    { 紧急联系人: info.value?.sparePhone },
    { 残疾证号: info.value?.disInfo?.certCode },
    { 残疾类别: getDisTypingDesc(info.value?.disInfo?.dsType) },
    { 残疾等级: info.value?.disInfo?.level },
    { 户籍属地: info.value?.address?.fullPath },
  ]
    .map((r) => {
      const [a, b] = Object.entries(r)[0]
      if (!b) {
        return ''
      }
      return `${a}：${b as unknown as string}`
    })
    .join('\n')

  init.value = true
})

const firstAvatar = computed(() => {
  return allCerts.value.find((e) => (e.doType as unknown as enums.CertTyping) === 'TITLE_IMAGE')?.waterMarkerAttachment?.fullAccessUrl
})
</script>

<template>
<InfoCardView :avatarUrl="firstAvatar" :name="info?.name">
  <template #name>
    {{ info?.name }}
  </template>
  <template #info>
    <UserInfoCard :info="info" />
  </template>

  <template #certs>
    <div v-if="init && queryError">
      <div>没有该用户，请重新筛选用户</div>
      <VBtn variant="outlined" color="primary" :block="true">
        筛选
      </VBtn>
    </div>
    <UserCertsCard
      v-else
      :userInfoId="info?.id"
      :userAccountId="info?.account?.id"
      :certs="allCerts"
      :userName="info?.name"
      :copyText="copyText"
    />
  </template>
</InfoCardView>
</template>
