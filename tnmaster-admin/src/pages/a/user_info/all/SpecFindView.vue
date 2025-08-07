<script setup lang="ts">
import type { Pq } from '@compose/types'
import type { RequestOf, ResponseOf } from '@/api'
import { Pw } from '@compose/shared'
import { VBtn } from 'vuetify/components'
import { z } from 'zod'
import { api, DisTyping_CONSTANTS, DisTyping_OPTIONS } from '@/api'
import { useSnackbar } from '@/components/TnSnackbar'

type Result = ResponseOf<typeof api.userInfoV2Api.getUserInfosAsAdmin>
type Param = RequestOf<typeof api.userInfoV2Api.getUserInfosAsAdmin>['spec']

interface Props {
  results?: Result
  pq?: Pq
  spec?: Param
}

const props = withDefaults(defineProps<Props>(), {
  results: void 0,
  spec: () => ({}),
  pq: () => Pw.DEFAULT_MAX,
})

const emit = defineEmits<Emits>()
const bankNames = ref<string[]>([])
const schema = z.object({
  addressCode: z.string().nonempty().optional().describe('地址').transform((v) => v?.trim()).transform((v) => v || void 0),
  name: z.string().nonempty().optional().describe('姓名').transform((v) => v?.trim()).transform((v) => v || void 0),
  disInfoCertCode: z.string().nonempty().optional().describe('残疾证号').transform((v) => v?.trim()).transform((v) => v || void 0).optional(),
  disInfoTypeIn: z.array(
    z.enum(DisTyping_CONSTANTS).describe('残疾类型'),
  ).optional(),
  disInfoLevelIn: z.array(
    z.number().min(1).max(4).int().describe('残疾等级'),
  ).optional(),
  phone: z.string().nonempty().optional().describe('电话').transform((v) => v?.trim()).transform((v) => v || void 0).optional().transform((v) => v || void 0),
  idCard: z.string().nonempty().describe('身份证号').idCard2Code().transform((v) => v?.trim()).transform((v) => v || void 0).optional(),
  isBlacked: z.boolean().optional().describe('是否拉黑').default(false),
  certsBankGroupValueIn: z.array(
    z.string().nonempty().describe('银行类型'),
  ).optional(),
})
type Schema = z.infer<typeof schema>

interface Emits {
  'submit': []
  'update:results': [ results: Result]
  'update:spec': [spec: Param]
  'update:pq': [pq: Pq]
}

const _results = useVModel(props, 'results', emit, { passive: true })

const _spec = useVModel(props, 'spec', emit, { passive: true })
const _pq = useVModel(props, 'pq', emit, { passive: true })

const snackbar = useSnackbar()

async function getAllSubmit(value: Schema) {
  _results.value = await snackbar.message(
    api.userInfoV2Api.getUserInfosAsAdmin({
      spec: {
        ...value,
        ..._pq.value,
      },
    }),
  )
}

async function getAll() {
  emit('submit')
  const data = {
    ..._spec.value,
    ..._pq.value,
    isBlacked: _spec.value.isBlacked ?? false,
  }
  getAllSubmit(data)
}

function reset() {
  _pq.value = { ...Pw.DEFAULT_MAX }
  _spec.value = {}
  void getAll()
}

onMounted(async () => {
  void getAll()
  bankNames.value = await api.certV2Api.getBanks().then((b) => b.map((e) => e.bankName)?.filter((e): e is string => e !== void 0))
})
watch(_spec, () => {
  _pq.value = Pw.DEFAULT_MAX
})
</script>

<template>
<YForm :schema="schema" class="w-full rounded-lg bg-gray-50 p4 shadow-sm dark:bg-gray-900" @submit="getAllSubmit" @reset="reset">
  <!-- 残疾类别和等级 -->
  <div class="mb-6">
    <h2 class="mb-3 text-lg text-gray-800 font-medium dark:text-gray-200">
      残疾信息筛选
    </h2>
    <VRow dense class="transition-all duration-300 ease-in-out">
      <VCol cols="12" sm="6" md="4" lg="4" class="transition-all duration-200 ease-in-out">
        <YField name="disInfoTypeIn" label="残疾类型" placeholder="请选择残疾类型">
          <VSelect
            :multiple="true"
            clearable
            :items="DisTyping_OPTIONS"
            itemTitle="k"
            itemValue="v"
            variant="outlined"
            class="rounded-md"
          />
        </YField>
      </VCol>
      <VCol cols="12" sm="6" md="4" lg="4" class="transition-all duration-200 ease-in-out">
        <YField name="disInfoLevelIn" label="残疾等级" placeholder="请选择残疾等级">
          <VSelect
            :multiple="true"
            clearable
            :items="Array.from({ length: 4 }, (_, i) => i + 1)"
            label="残疾等级"
            variant="outlined"
            class="rounded-md"
          />
        </YField>
      </VCol>
      <VCol cols="12" sm="6" md="4" lg="4" class="transition-all duration-200 ease-in-out">
        <YField :name="{ addressCode: 'adCode' }" label="地址" placeholder="请选择地址">
          <AddressSelect2View />
        </YField>
      </VCol>
      <VCol cols="12" sm="6" md="4" lg="4" class="transition-all duration-200 ease-in-out">
        <YField name="certsBankGroupValueIn" label="持有银行卡" placeholder="请选择银行">
          <VSelect
            :multiple="true"
            clearable
            :items="bankNames"
            variant="outlined"
            class="rounded-md"
            prependInnerIcon="i-mdi:bank"
          />
        </YField>
      </VCol>
    </VRow>
  </div>

  <VDivider class="my-4">
    <span class="px-2 text-primary font-medium">精准信息</span>
  </VDivider>

  <!-- 个人信息字段 -->
  <div class="mb-6 mt-4">
    <VRow dense class="transition-all duration-300 ease-in-out">
      <VCol cols="12" sm="6" md="4" lg="3" class="transition-all duration-200 ease-in-out">
        <YField name="name" label="姓名" placeholder="请输入姓名">
          <VTextField variant="outlined" class="rounded-md" prependInnerIcon="i-mdi:account" />
        </YField>
      </VCol>
      <VCol cols="12" sm="6" md="4" lg="3" class="transition-all duration-200 ease-in-out">
        <YField name="phone" label="电话" placeholder="请输入电话">
          <VTextField variant="outlined" class="rounded-md" prependInnerIcon="i-mdi:phone" />
        </YField>
      </VCol>
      <VCol cols="12" sm="6" md="4" lg="3" class="transition-all duration-200 ease-in-out">
        <YField name="idCard" label="身份证号" placeholder="请输入身份证号">
          <VTextField variant="outlined" class="rounded-md" prependInnerIcon="i-mdi:card-account-details" />
        </YField>
      </VCol>
      <VCol cols="12" sm="6" md="4" lg="3" class="transition-all duration-200 ease-in-out">
        <YField name="disInfoCertCode" label="残疾证号" placeholder="请输入残疾证号">
          <VTextField variant="outlined" class="rounded-md" prependInnerIcon="i-mdi:file-certificate" />
        </YField>
      </VCol>
      <VCol cols="12" sm="6" md="4" lg="3" class="transition-all duration-200 ease-in-out">
        <YField name="isBlacked" label="是否拉黑">
          <VSwitch color="error" :hideDetails="true" density="compact" />
        </YField>
      </VCol>
    </VRow>
  </div>

  <!-- 按钮和分页 -->
  <slot name="submit" :submit="getAll" :reset="reset">
    <div class="my-4 flex justify-end gap-4">
      <VBtn
        color="default"
        variant="outlined"
        type="reset"
        class="rounded-md px-6"
        prependIcon="i-mdi:refresh"
      >
        重新筛选
      </VBtn>
      <VBtn
        color="success"
        type="submit"
        class="rounded-md px-6"
        prependIcon="i-mdi:magnify"
        elevation="1"
      >
        查询
      </VBtn>
    </div>
  </slot>

  <div class="mt-4 flex items-center justify-center">
    <PaginatorComp v-model:pq="_pq" :pr="_results" class="w-full" @change="getAll" />
  </div>
</YForm>
</template>

<style scoped lang="scss">
.v-divider {
  border-color: rgba(var(--v-theme-primary), 0.2);

  &::before,
  &::after {
    border-color: rgba(var(--v-theme-primary), 0.2);
  }
}

:deep(.v-field) {
  border-radius: 8px;
  transition: all 0.2s ease;

  &:hover {
    box-shadow: 0 0 0 1px rgba(var(--v-theme-primary), 0.3);
  }

  &.v-field--focused {
    box-shadow: 0 0 0 2px rgba(var(--v-theme-primary), 0.5);
  }
}
</style>
