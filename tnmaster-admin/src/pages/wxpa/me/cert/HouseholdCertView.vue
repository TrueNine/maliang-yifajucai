<script setup lang="ts">
import type { ResponseOf } from '@/__generated'
import { z } from 'zod'
import { api } from '@/api'
import { file } from '@/common/zod'
import { useSnackbar } from '@/components'
import TnFullScreenDialog from '@/components/TnFullScreenDialog.vue'
import CertImageWindowView from '@/pages/wxpa/me/cert/CertImageWindowView.vue'

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
onMounted(async () => {
  data.value = await api.certV2Api.getHouseHoldCardAttachmentAsMe()
})
const snackbar = useSnackbar()
const closeFn = ref<() => void>()
const schema = z.object({
  files: z.array(file()).min(1).max(2).fileNotRepeat(),
})
type Schema = z.infer<typeof schema>
async function submit(value: Schema) {
  data.value = await snackbar.message(
    api.certV2Api.postHouseholdCardAttachmentAsMe({ body: { head: value.files[0], tail: value.files[1] } }),
  )
}

async function deleteDisCard2() {
  await snackbar.message(
    api.certV2Api.deleteHouseholdAttachmentByUserAccountId(),
  )
  data.value = void 0
}
</script>

<template>
<CertImageWindowView
  :src="pair.src.url"
  :srcAuditStatus="pair.src.auditStatus"
  :tailSrc="pair.tailSrc.url"
  :tailSrcAuditStatus="pair.tailSrc.auditStatus"
  title="个人户口页"
  @clear="deleteDisCard2"
>
  <template #empty-upload>
    <TnFullScreenDialog title="上传个人户口页">
      <template #activator="{ props: pps }">
        <VBtn color="primary" variant="flat" v-bind="pps">
          立即上传
        </VBtn>
      </template>
      <YForm class="wh-full" :schema="schema" @submit="submit">
        <FullScreenConfirmLayout>
          <YField name="files">
            <MobileFileUploader :maxLength="2" />
          </YField>
          <YFieldMessage name="files" />
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
  </template>
</CertImageWindowView>
</template>
