<script setup lang="ts">
import type { RefId } from '@compose/types'
import type { TypeOf } from 'zod'
import type { AuditTyping, ResponseOf } from '@/api'
import { confirmation } from '@/common'
import { file } from '@/common/zod'
import { useSnackbar } from '@/components'
import CertImageWindowView from '@/pages/wxpa/me/cert/CertImageWindowView.vue'

const data = ref<ResponseOf<typeof api.certV2Api.getBankCardsAsMe>>()
const bankNames = ref<ResponseOf<typeof api.certV2Api.getBanks>>()

interface _Src {
  url?: string
  auditStatus?: AuditTyping
}

interface _Srcs {
  id?: RefId
  src?: _Src
  tailSrc?: _Src
}

const pairs = computed<_Srcs[]>(() => {
  const all = data.value?.map((e) => {
    return {
      id: e?.id,
      src: {
        url: e?.certs?.[0]?.waterMarkerAttachment?.fullAccessUrl,
        auditStatus: e?.certs?.[0]?.auditStatus,
      },
      tailSrc: {
        url: e?.certs?.[1]?.waterMarkerAttachment?.fullAccessUrl,
        auditStatus: e?.certs?.[1]?.auditStatus,
      },
    }
  })
  if (!all || !all.length) {
    return [{}]
  }
  return all
})

onMounted(async () => {
  bankNames.value = await api.certV2Api.getBanks()
  data.value = await api.certV2Api.getBankCardsAsMe()
})

const snackbar = useSnackbar()

const schema = z.object({
  bankName: z.string({ message: '银行类型不能为空' }).refine((value) => {
    if (!bankNames.value) {
      return false
    }
    return bankNames.value.some((bank) => bank.bankName === value)
  }, {
    message: '请选择有效的银行类型',
  }),
  issueLocation: z.string({ message: '发卡地不能为空' }).min(6, { message: '发卡地长度必须大于等于6' }).trim(),
  phone: z.string({ message: '预留手机号不能为空' }).phone().trim(),
  code: z.string({ message: '银行卡号不能为空' }).regex(/^\d{16,19}$/, '银行卡号格式不正确').trim(),
  files: z.array(file()).min(1).max(2).fileNotRepeat(),
})

const closeFn = ref<() => void>()
type Schema = TypeOf<typeof schema>
async function onOk(value?: Schema) {
  if (!value) {
    return
  }
  await snackbar.message(
    api.certV2Api.postBankCardAsMe({
      body: {
        dto: {
          bankName: value.bankName,
          code: value.code,
          available: true,
          phone: value.phone,
        },
        head: value.files[0],
        tail: value.files[1],
      },
    }),
  )
  data.value = await api.certV2Api.getBankCardsAsMe()
  closeFn.value?.()
}
const dialogShow = ref(false)
const { confirm, ok, cancel } = confirmation(
  { onOk, show: dialogShow },
)

async function deleteBankCardById(id?: RefId) {
  if (!id) {
    return
  }
  await snackbar.message(
    api.certV2Api.deleteBankCardAttachmentAsMe({
      bankCardId: id as unknown as number,
    }),
  )
  data.value = await api.certV2Api.getBankCardsAsMe()
}

const activeDialog = ref(false)
</script>

<template>
<div class="wh-full space-y-2">
  <template v-for="(pair, _) in pairs" :key="_">
    <CertImageWindowView :src="pair?.src?.url" :srcAuditStatus="pair?.src?.auditStatus" :tailSrc="pair?.tailSrc?.url" :tailSrcAuditStatus="pair?.tailSrc?.auditStatus" title="银行卡" @clear="() => deleteBankCardById(pair.id)">
      <template #empty-upload>
        <VBtn color="primary" variant="flat" @click="activeDialog = true">
          立即上传
        </VBtn>
      </template>
    </CertImageWindowView>
  </template>
  <div v-if="!pairs.length || !(pairs.length === 1 && !pairs[0].src?.url)" class="my-2 flex flex-col items-center justify-center rounded bg-body py-2 space-y-4">
    <div class="text-6">
      添加新的银行卡
    </div>
    <VBtn icon variant="tonal" color="primary" @click="activeDialog = true">
      <YIco class="i-mdi:plus text-6" />
    </VBtn>
  </div>

  <TnFullScreenDialog v-model="activeDialog" title="银行卡">
    <YForm class="wh-full" :schema="schema" @submit="confirm">
      <FullScreenConfirmLayout>
        <YField name="files">
          <MobileFileUploader :maxLength="2" />
        </YField>
        <YFieldMessage name="files" />
        <YField name="bankName" label="银行类型">
          <VAutocomplete :items="bankNames" itemTitle="bankName" itemValue="bankName" />
        </YField>
        <YField name="phone" label="预留手机号">
          <VTextField />
        </YField>
        <YField name="code" label="银行卡号">
          <VTextField />
        </YField>
        <YField name="issueLocation" label="开户行地址">
          <VTextField />
        </YField>

        <VAlert class="my-4">
          <VAlertTitle>提示</VAlertTitle>
          上传单张视为双面，两张则第一张为正面，第二张为反面，仅支持 A4 扫描件
        </VAlert>

        <template #ok="{ onClose }">
          <VBtn :block="true" type="submit" color="primary" variant="flat" @click="closeFn = onClose">
            提交
          </VBtn>
          <ContractDialogConfirmView
            v-model:dialog="dialogShow"
            :delay="5000"
            @ok="ok"
            @cancel="cancel"
          />
        </template>
      </FullScreenConfirmLayout>
    </YForm>
  </TnFullScreenDialog>
</div>
</template>
