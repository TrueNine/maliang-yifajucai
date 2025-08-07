<script setup lang="ts">
import type { RequestOf, ResponseOf } from '@/api'
import { object, string } from 'yup'
import { api } from '@/api'
import { useSnackbar } from '@/components/TnSnackbar'
import { useConfigStore } from '@/store'

const cfg = useConfigStore()
cfg.bottomNavigationShow = false

const snackbar = useSnackbar()
const spec = ref<RequestOf<typeof api.blackListV2Api.getPersonalEventDoc>['spec']>({})
const eventVo = ref<ResponseOf<typeof api.blackListV2Api.getPersonalEventDoc>>()

const activeResult = ref(false)

async function query(r: RequestOf<typeof api.blackListV2Api.getPersonalEventDoc>['spec']) {
  await snackbar?.message(
    async () => {
      eventVo.value = await api.blackListV2Api.getPersonalEventDoc({ spec: r })
      activeResult.value = !!eventVo.value
      throw new Error('查询到结果')
    },
    { message: '此人目前安全', errorMessage: '查询到结果' },
  )
}

function changePrimaryCode(code: string) {
  if (code.length < 18) {
    return
  }
  switch (code.length) {
    case 18:
      spec.value.blackUserInfoIdCard = code.slice(0, 18)
      break
    case 20:
    case 22:
      spec.value.blackUserInfoDisInfoCertCode = code.slice(0, 22)
      break
    default:
      break
  }
}

const schema = object({
  blackUserInfoPhone: string().nullable().optional().max(11, '手机号长度不正确').min(11, '手机号长度不正确'),
  blackUserInfoName: string().min(2, '姓名过短').max(4, '姓名过长').nullable().optional(),
  __code: string().optional(),
}).test('at-least-one', '至少需要填写一个查询参数', (value) => {
  const e = value && (value.blackUserInfoPhone || value.blackUserInfoName || value.__code)
  return !!e
})

const ranks = ref<{ name: string, value: number }[]>()
const maxRankNum = computed(() => {
  return ranks.value?.[0]?.value
})

function percent(num: number) {
  if (!maxRankNum.value) {
    return 0
  }
  return (num / maxRankNum.value * 100).toFixed(2)
}

async function getProvincesStatistics() {
  const a = await api.blackListV2Api.getProvincesStatistics({ rankNum: 10 })
  const b = await api.addressV2Api.getFullPathsByCodes({ codes: Object.keys(a) })
  ranks.value = Object.entries(a).map(([k, v]) => {
    return {
      name: b.find((e) => e.code === k)?.fullPath ?? '',
      value: v,
    }
  }).toSorted((a, b) => b.value - a.value)
}
onMounted(getProvincesStatistics)
</script>

<template>
<div class="h-full w-full p-2">
  <YForm :schema="schema" @submit="query">
    <VCard>
      <VCardTitle> 黑名单查询 </VCardTitle>
      <VCardText>
        <YField name="__code" label="身份证号 或 残疾证号" @change="changePrimaryCode">
          <VTextField />
        </YField>

        <YField name="blackUserInfoPhone" label="手机号">
          <VTextField />
        </YField>

        <YField name="blackUserInfoName" label="姓名">
          <TnNameInputComp />
        </YField>
        <YFieldMessage name="" />
      </VCardText>
      <VCardActions>
        <VBtn :block="true" color="primary" type="submit">
          查询
        </VBtn>
      </VCardActions>
    </VCard>
  </YForm>

  <VCard class="mt-8">
    <VCardTitle> 省份排名 TOP 10 </VCardTitle>
    <VCardText>今天有给你的省份丢脸吗？</VCardText>
    <VDivider />
    <VCardText>
      <div v-if="ranks">
        <VTable>
          <thead>
            <tr>
              <th class="w-20">
                省份
              </th>
              <th>人数</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(item, index) in ranks" :key="index">
              <td class="w-20">
                {{ item.name }}
              </td>
              <td>
                <VProgressLinear :rounded="true" height="25" :modelValue="percent(item.value)" color="primary">
                  <strong>{{ item.value }}</strong>
                </VProgressLinear>
              </td>
            </tr>
          </tbody>
        </VTable>
      </div>
    </VCardText>
  </VCard>

  <VDialog v-model="activeResult" persistent>
    <VCard class="my4">
      <VCardTitle class="f-x-c">
        事件报告
      </VCardTitle>
      <div v-if="eventVo" p1>
        <VRow :dense="true">
          <VCol v-if="eventVo.blackUserInfo?.name" :cols="4">
            姓名
          </VCol>
          <VCol v-if="eventVo?.blackUserInfo?.name" :cols="8">
            {{ eventVo.blackUserInfo.name }}
          </VCol>
          <VCol v-if="eventVo?.blackUserInfo?.phone" :cols="4">
            电话
          </VCol>
          <VCol v-if="eventVo?.blackUserInfo?.phone" :cols="8">
            {{ eventVo.blackUserInfo.phone }}
          </VCol>
          <VCol v-if="eventVo?.blackUserInfo?.idCard" :cols="4">
            身份证号
          </VCol>
          <VCol v-if="eventVo?.blackUserInfo?.idCard" :cols="8">
            {{ eventVo?.blackUserInfo.idCard }}
          </VCol>
          <VCol v-if="eventVo?.blackUserInfo?.disInfo?.certCode" :cols="4">
            残疾证号
          </VCol>
          <VCol v-if="eventVo?.blackUserInfo?.disInfo?.certCode" :cols="8">
            {{ eventVo?.blackUserInfo.disInfo?.certCode }}
          </VCol>
          <VCol v-if="eventVo?.blackUserInfo?.disInfo?.dsType" :cols="4">
            残疾类型
          </VCol>
          <VCol v-if="eventVo?.blackUserInfo?.disInfo?.dsType" :cols="8">
            <DisTypeAndLevelChipView :type="eventVo.blackUserInfo.disInfo.dsType" :level="eventVo.blackUserInfo.disInfo.level" />
          </VCol>
        </VRow>
      </div>
      <div v-else class="p1">
        <p>未查询到结果</p>
      </div>

      <div class="rounded bg-m p1">
        <p>
          {{ eventVo?.eventDoc }}
        </p>
      </div>
      <VCardActions>
        <VBtn :block="true" variant="text" color="primary" @click="activeResult = !activeResult">
          确定
        </VBtn>
      </VCardActions>
    </VCard>
  </VDialog>
</div>
</template>
