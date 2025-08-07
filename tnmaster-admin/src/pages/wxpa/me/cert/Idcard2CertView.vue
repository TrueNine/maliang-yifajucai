<script setup lang="ts">
import type { TypeOf } from 'zod'
import type { ResponseOf } from '@/api'
import { api, GenderTyping_CONSTANTS } from '@/api'
import { confirmation } from '@/common'
import { file } from '@/common/zod'
import TnDisCodeInputComp from '@/components/TnDisCodeInputComp.vue'
import TnFullScreenDialog from '@/components/TnFullScreenDialog.vue'
import { useSnackbar } from '@/components/TnSnackbar'
import CertImageWindowView from '@/pages/wxpa/me/cert/CertImageWindowView.vue'
import ContractDialogConfirmView from '@/views/ContractDialogConfirmView.vue'

interface Emits {
  complete: []
}
const emit = defineEmits<Emits>()

const schema = z.object({
  firstName: z.string(),
  lastName: z.string(),
  files: z.array(file())
    .max(2, '证件最多两张')
    .min(1, '至少上传一张图片')
    .fileNotRepeat(),
  adCode: z.string().length(6),
  phone: z.string().phone(),
  idcardCode: z.string().idCard2Code(),
  gender: z.enum(GenderTyping_CONSTANTS),
  birthday: z.coerce.string(),
})
type Schema = TypeOf<typeof schema>

const data = ref<ResponseOf<typeof api.certV2Api.getIdCard2AttachmentAsMe>>()
const pair = computed(() => {
  return {
    src: {
      url: data.value?.[0]?.waterMarkerAttachment?.fullAccessUrl,
      auditStatus: data.value?.[0]?.auditStatus,
    },
    tailSrc: {
      url: data.value?.[1]?.waterMarkerAttachment?.fullAccessUrl,
      auditStatus: data.value?.[1]?.auditStatus,
    },
  }
})

const dialogShow = ref<boolean>(false)

const snackbar = useSnackbar()
const closeFn = ref<() => void>()

async function submit(value?: Schema) {
  if (!value) {
    return
  }
  data.value = await snackbar.message(
    api.certV2Api.postIdCard2CertAsMe({
      body: {
        userInfoBase: {
          idCard: value.idcardCode,
          firstName: value.firstName,
          lastName: value.lastName,
          gender: value.gender,
          phone: value.phone,
          addressCode: value.adCode,
          birthday: value.birthday,
        },
        head: value.files[0],
        tail: value.files[1],
      },
    }),
  )
  emit('complete')
  closeFn.value?.()
}

onMounted(async () => {
  data.value = await api.certV2Api.getIdCard2AttachmentAsMe()
})

const { confirm, ok, cancel } = confirmation(
  {
    onOk: submit,
    show: dialogShow,
  },
)

async function deleteIdCard2() {
  await snackbar.message(
    api.certV2Api.deleteIdCard2AttachmentAsMe(),
  )
  data.value = void 0
}
</script>

<template>
<CertImageWindowView
  :srcAuditStatus="pair.src?.auditStatus"
  :tailSrcAuditStatus="pair?.tailSrc?.auditStatus"
  :src="pair?.src?.url" :tailSrc="pair?.tailSrc?.url"
  title="身份证"
  @clear="deleteIdCard2"
>
  <template #empty-upload>
    <TnFullScreenDialog title="上传个人身份证">
      <template #activator="{ props: pps }">
        <VBtn color="primary" variant="flat" v-bind="pps">
          立即上传
        </VBtn>
      </template>
      <YForm class="wh-full" :schema="schema" @submit="confirm">
        <FullScreenConfirmLayout>
          <YField name="files">
            <MobileFileUploader :maxLength="2" />
          </YField>
          <div class="pb-4">
            <YFieldMessage name="files" />
          </div>
          <YField label="姓名" :name="['__name', 'firstName', 'lastName']">
            <TnNameInputComp />
          </YField>
          <YField label="身份证号" :name="['__idCard', 'birthday', 'adCode', 'idcardCode', 'gender']">
            <TnDisCodeInputComp />
          </YField>
          <YField label="手机号" name="phone">
            <VTextField />
          </YField>

          <VAlert class="my-4">
            <VAlertTitle>提示</VAlertTitle>
            上传单张视为双面，两张则第一张为正面，第二张为反面，仅支持 A4 扫描件
          </VAlert>
          <template #ok="{ onClose }">
            <VBtn variant="flat" :block="true" type="submit" color="primary" @click="closeFn = onClose">
              提交
            </VBtn>
          </template>
        </FullScreenConfirmLayout>
      </YForm>
    </TnFullScreenDialog>
    <ContractDialogConfirmView v-model:dialog="dialogShow" :delay="5000" @ok="ok" @cancel="cancel" />
  </template>
</CertImageWindowView>
</template>
