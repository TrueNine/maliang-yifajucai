<script setup lang="ts">
import type { ResponseOf } from '@/__generated'
import { z } from 'zod'
import { api, DisTyping_CONSTANTS, GenderTyping_CONSTANTS } from '@/api'
import { confirmation } from '@/common'
import { file } from '@/common/zod'
import { useSnackbar } from '@/components'
import TnDisCodeInputComp from '@/components/TnDisCodeInputComp.vue'
import CertImageWindowView from '@/pages/wxpa/me/cert/CertImageWindowView.vue'

interface Emits {
  (e: 'complete'): void
}

const emit = defineEmits<Emits>()

const data = shallowRef<ResponseOf<typeof api.certV2Api.getIdCard2AttachmentAsMe>>()

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
onMounted(async () => {
  data.value = await api.certV2Api.getDisCard2AttachmentAsMe()
})
onUnmounted(() => {
  emit('complete')
})
const snackbar = useSnackbar()

const schema = z.object({
  adCode: z.string().length(6, '地区代码长度为6位').describe('地区代码'),
  idcardCode: z.string().idCard2Code().describe('身份证号'),
  firstName: z.string({ message: '姓不能为空' }).nonempty('姓不能为空').describe('姓'),
  lastName: z.string({ message: '名不能为空' }).nonempty('名不能为空').describe('名'),
  birthday: z.coerce.string().describe('出生日期'),
  phone: z.string().phone().describe('手机号'),
  gender: z.enum(GenderTyping_CONSTANTS).describe('性别'),
  disType: z.enum(DisTyping_CONSTANTS).describe('残疾类型'),
  disLevel: z.number().int().min(1).max(4).describe('残疾等级'),
  disCode: z.string().disCardCode3().describe('残疾证号'),
  files: z.array(file()).min(1).max(2).fileNotRepeat().describe('残疾证扫描件'),
})
type Schema = z.infer<typeof schema>
const closeFn = ref<() => void>()

const dialogShow = ref<boolean>()

async function onOk(value?: Schema) {
  if (!value) {
    return
  }
  data.value = await snackbar.message(
    api.certV2Api.postDisCard2AttachmentAsMe({
      body: {
        disInfoDto: {
          dsType: value.disType,
          level: value.disLevel,
          certCode: value.disCode,
        },
        userInfoBase: {
          phone: value.phone,
          idCard: value.idcardCode,
          addressCode: value.adCode,
          gender: value.gender,
          firstName: value.firstName,
          birthday: value.birthday,
          lastName: value.lastName,
        },
        head: value.files[0],
        tail: value.files[1],
      },
    }),
  )
  emit('complete')
  closeFn.value?.()
}
const { confirm, ok, cancel } = confirmation({
  onOk,
  show: dialogShow,
})

async function deleteDisCard2() {
  await snackbar.message(api.certV2Api.deleteDisCard2AttachmentAsMe())
  data.value = void 0
  emit('complete')
}

function error(e: unknown) {
  console.error(e)
}
</script>

<template>
<CertImageWindowView
  :src="pair.src.url"
  :srcAuditStatus="pair.src.auditStatus"
  :tailSrc="pair.tailSrc.url"
  :tailSrcAuditStatus="pair.tailSrc.auditStatus"
  title="个人残疾证" @clear="deleteDisCard2"
>
  <template #empty-upload>
    <TnFullScreenDialog title="上传个人身份证">
      <template #activator="{ props: pps }">
        <VBtn color="primary" variant="flat" v-bind="pps">
          立即上传
        </VBtn>
      </template>
      <YForm class="wh-full" :schema="schema" @error="error" @submit="confirm">
        <FullScreenConfirmLayout>
          <YField name="files">
            <MobileFileUploader :maxLength="2" />
          </YField>
          <div class="pb-4">
            <YFieldMessage name="files" />
          </div>
          <YField label="姓名" :name=" ['__name', 'firstName', 'lastName']">
            <TnNameInputComp />
          </YField>
          <YField label="残疾证号" :name="['__disCode', 'birthday', 'disCode', 'adCode', 'idcardCode', 'disLevel', 'disType', 'gender']">
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
      <ContractDialogConfirmView
        v-model:dialog="dialogShow"
        :delay="5000"
        @ok="ok"
        @cancel="cancel"
      />
    </TnFullScreenDialog>
  </template>
</CertImageWindowView>
</template>
