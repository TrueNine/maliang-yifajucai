<script setup lang="ts">
import type { dynamic, RefId } from '@compose/types'
import { z } from 'zod'
import { api, DisTyping_CONSTANTS, DisTyping_OPTIONS, GenderTyping_CONSTANTS } from '@/api'
import { useSnackbar } from '@/components'
import TnDisCodeInputComp from '@/components/TnDisCodeInputComp.vue'
import GenderBtnToggleComp from '@/components/TnGenderBtnToggleComp.vue'
import NameFieldView from '@/views/NameFieldView.vue'

interface Props {
  modelValue?: RefId
}

interface Emits {
  'update:modelValue': [v: RefId]
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: void 0,
})

const emit = defineEmits<Emits>()
const { modelValue: _iId } = useVModels(props, emit, { passive: false })

const schema = z.object({
  firstName: z.string({ message: '姓氏不能为空' }).nonempty(),
  lastName: z.string({ message: '名不能为空' }).nonempty(),
  idCard: z.string().idCard2Code().optional(),
  gender: z.enum(GenderTyping_CONSTANTS, { message: '性别值不正确' }).optional(),
  phone: z.string().nonempty().phone().optional(),
  sparePhone: z.string().phone().optional(),
  addressCode: z.string().optional(),
  disInfo: z.object({
    certCode: z.string().disCardCode3().optional(),
    level: z.number().optional(),
    dsType: z.enum(DisTyping_CONSTANTS).optional(),
  }).partial(),
  email: z.string().email().optional(),
}).refine((data) => data.idCard || data.phone, {
  message: '身份证号或手机号必须填写一个',
  path: ['idCard', 'phone'],
})
const snackbar = useSnackbar()

type Schema = z.infer<typeof schema>
const formValue = ref()
const userInfoIsValid = ref(true)
const dsLevels = [1, 2, 3, 4]

async function initDataFn() {
  if (!_iId.value) {
    return
  }
  const data = await api.userInfoV2Api.getUserInfoPutFormAsAdmin({ spec: { id: _iId.value } })
  if (!data) {
    return
  }
  if (data.firstName || data.lastName) {
    (data as { __fullName: string }).__fullName = (data.firstName ?? '') + (data.lastName ?? '')
  }
  if (data.idCard) {
    (data as { __code: string }).__code = data.idCard
  }
  if (data.disInfo?.certCode) {
    (data as { __code: string }).__code = data.disInfo.certCode
  }
  formValue.value = data
}
onMounted(initDataFn)

async function submit(value: Schema) {
  if (_iId.value) {
    await api.userInfoV2Api.putUserInfoAsAdmin({ body: { ...value, id: _iId.value } })
  } else {
    const r = await api.userInfoV2Api.postUserInfoAsAdmin({ body: value })
    _iId.value = r.id
  }
  snackbar.success('保存成功')
  formValue.value = value
}

function clear(value: dynamic) {
  formValue.value = value
}

function onError(error: unknown) {
  console.error(error)
  if (error instanceof Error) {
    snackbar.error(error.message)
  }
  if (typeof error !== 'object' || error === null) {
    return
  }
  if (!('errors' in error)) {
    return
  }
  if (typeof error.errors !== 'object' || error.errors === null) {
    return
  }

  if (error.errors) {
    Object.entries(error.errors).forEach(([, value]) => {
      snackbar.error(value)
    })
  }
}

defineExpose({
  clear,
})
</script>

<template>
<VCard class="mx-auto" :maxWidth="1000" elevation="2">
  <VCardText>
    <YForm v-model="formValue" :schema="schema" @error="onError" @submit="submit" @reset="clear">
      <VContainer>
        <!-- 基本信息卡片 -->
        <VCard variant="outlined" class="mb-4">
          <VCardTitle class="font-weight-medium text-subtitle-1">
            <VIcon icon="mdi-account" class="mr-2" />
            基本信息
          </VCardTitle>
          <VCardText>
            <VRow>
              <VCol cols="12" md="6">
                <YField
                  :name="{
                    __fullName: 'modelValue',
                    firstName: 'firstName',
                    lastName: 'lastName',
                  }"
                >
                  <NameFieldView />
                </YField>
              </VCol>

              <VCol cols="12" md="6">
                <YField
                  :name="[
                    { __code: 'modelValue' },
                    'gender',
                    'birthday',
                    { 'idCard': 'idcardCode', 'disInfo.certCode': 'disCode', 'disInfo.level': 'disLevel', 'addressCode': 'adCode', 'disInfo.dsType': 'disType' },
                  ]" label="残疾证/身份证号"
                >
                  <TnDisCodeInputComp />
                </YField>
              </VCol>
            </VRow>

            <VRow>
              <VCol cols="12" md="6">
                <YField name="gender" label="性别">
                  <GenderBtnToggleComp />
                </YField>
              </VCol>
              <VCol cols="12" md="6">
                <!-- <VTextField :modelValue="_showBirthday" type="date" @update:modelValue="birthday" /> -->
              </VCol>
            </VRow>
          </VCardText>
        </VCard>

        <!-- 联系方式卡片 -->
        <VCard variant="outlined" class="mb-4">
          <VCardTitle class="text-subtitle-1 font-weight-medium">
            <VIcon icon="mdi-phone" class="mr-2" />
            联系方式
          </VCardTitle>
          <VCardText>
            <VRow>
              <VCol cols="12" md="6">
                <YField name="phone" label="手机号">
                  <PhoneInputComp />
                </YField>
              </VCol>
              <VCol cols="12" md="6">
                <YField name="sparePhone" label="备用手机号">
                  <PhoneInputComp />
                </YField>
              </VCol>
            </VRow>

            <VRow>
              <VCol cols="12">
                <YField name="email" label="邮箱">
                  <TnEmailInputComp />
                </YField>
              </VCol>
            </VRow>
          </VCardText>
        </VCard>

        <!-- 地址信息卡片 -->
        <VCard variant="outlined" class="mb-4">
          <VCardTitle class="text-subtitle-1 font-weight-medium">
            <VIcon icon="mdi-map-marker" class="mr-2" />
            地址信息
          </VCardTitle>
          <VCardText>
            <YField :name="{ addressCode: 'adCode' }" label="所在地">
              <AddressSelect2View />
            </YField>
          </VCardText>
        </VCard>

        <!-- 残疾信息卡片 -->
        <VCard variant="outlined" class="mb-4">
          <VCardTitle class="text-subtitle-1 font-weight-medium">
            <VIcon icon="mdi-card-account-details" class="mr-2" />
            残疾信息
          </VCardTitle>
          <VCardText>
            <VRow>
              <VCol cols="12" md="6">
                <YField name="disInfo.dsType" label="残疾类别">
                  <VSelect
                    :items="DisTyping_OPTIONS" itemTitle="k" itemValue="v" density="comfortable"
                    variant="outlined"
                  />
                </YField>
              </VCol>
              <VCol cols="12" md="6">
                <YField name="disInfo.level" label="残疾等级">
                  <VSelect :items="dsLevels" density="comfortable" variant="outlined" />
                </YField>
              </VCol>
            </VRow>
          </VCardText>
        </VCard>

        <!-- 提交按钮 -->
        <VRow justify="end">
          <VCol cols="12" md="3">
            <VBtn
              :disabled="!userInfoIsValid" color="primary" type="submit" variant="elevated" :block="true"
              height="44" class="text-subtitle-1"
            >
              <VIcon icon="mdi-check" class="mr-2" />
              提交
            </VBtn>
          </VCol>
        </VRow>
      </VContainer>
    </YForm>
  </VCardText>
</VCard>
</template>
