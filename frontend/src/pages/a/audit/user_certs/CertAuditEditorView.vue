<script setup lang="ts">
import type { CertTyping } from '@/__generated/model/enums'
import type { CertView } from '@/__generated/model/static'
import type { ResponseOf } from '@/api'
import { api, getCertTypingDesc } from '@/api'
import { useSnackbar } from '@/components'

interface Props {
  certs?: CertView[]
}

interface Emits {
  pass: [cert: CertView[]]
  reject: [cert: CertView[]]
}

const props = withDefaults(defineProps<Props>(), {
  certs: () => [],
})
const emit = defineEmits<Emits>()

const certType = computed<CertTyping | undefined>(() => {
  if (!props.certs || !props.certs.length) {
    return void 0
  }
  const isSame = props.certs.every((c, _, cs) => {
    return c.doType === cs[0]?.doType
  })
  return isSame ? props.certs[0]?.doType : void 0
})
const userAccountId = computed(() => {
  return props.certs[0]?.userId
})
const groupCode = computed(() => {
  return props.certs[0]?.groupCode
})

const data = ref<ResponseOf<typeof api.userInfoV2Api.getCertifiedUserInfoViewAsAdmin>>()
watch(userAccountId, async (value) => {
  if (!value) {
    return
  }
  data.value = await api.userInfoV2Api.getCertifiedUserInfoViewAsAdmin(
    {
      spec: {
        userAccountIdOrUserInfoId: value,
        doType: certType.value,
        groupCode: groupCode.value,
      },
    },
  )
})

const certTypeDesc = computed(() => {
  return getCertTypingDesc(certType?.value)
})
const snackbar = useSnackbar()
function auditPass() {
  emit('pass', props.certs.map((e) => ({ ...e, auditStatus: 'PASS' })))
  snackbar.success('审核通过')
}
function auditReject() {
  emit('reject', props.certs.map((e) => ({ ...e, auditStatus: 'REJECT' })))
  snackbar.error('审核不通过')
}
</script>

<template>
<VCard v-if="props.certs && props.certs.length">
  <VCardText>
    <VCarousel height="300">
      <template v-for="(cert, _) in props.certs" :key="_">
        <VCarouselItem :src="cert.waterMarkerAttachment?.fullAccessUrl" />
      </template>
    </VCarousel>
    <!-- 证件信息补全 -->
    <VCard v-if="certType">
      <VCardTitle> 证件类型：{{ certTypeDesc }}</VCardTitle>
      <VCardText>
        <!-- 审核证照对应信息 -->
        <template v-if="data">
          <VTable>
            <thead>
              <tr>
                <th>类别</th>
                <th>值</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td>姓名</td>
                <td>{{ data.metadataName }}</td>
              </tr>
              <tr>
                <td>推断地址</td>
                <td>
                  {{ data.address?.fullPath }}
                </td>
              </tr>
              <tr>
                <td>身份证号</td>
                <td>
                  {{ data.metadataIdCard }}
                  <GenderTypingChipView :type="data.gender" />
                </td>
              </tr>
              <tr>
                <td>残疾证号</td>
                <td>
                  {{ data.disInfo?.metadataCertCode }}
                  <DisTypeAndLevelChipView :type="data.disInfo?.dsType" :level="data.disInfo?.level" />
                </td>
              </tr>
              <tr>
                <td>电话</td>
                <td>{{ data.metadataPhone }}</td>
              </tr>
            </tbody>
          </VTable>
        </template>
      </VCardText>
    </VCard>
  </VCardText>
  <VCardActions class="flex justify-center">
    <VBtn variant="flat" color="primary" @click="auditPass">
      通过
    </VBtn>
    <VBtn variant="flat" color="error" @click="auditReject">
      不合规
    </VBtn>
  </VCardActions>
</VCard>
</template>
