<script setup lang="ts">
import type { Pq } from '@compose/types'
import type { HTTPMethod, RequestOf, ResponseOf } from '@/api'
import { array, object, string } from 'yup'
import { api, HTTPMethod_CONSTANTS } from '@/api'
import PagedSelector from '@/components/PagedSelector.vue'

type _Data = ResponseOf<typeof api.aclV2Api.getAllApisAsAdmin>
const data = ref<_Data>()

const pq = ref<Pq>()

async function loadData() {
  data.value = await api.aclV2Api.getAllApisAsAdmin({
    spec: { ...pq.value },
  })
}

onMounted(loadData)

const _permissionsPq = ref<Pq>()
const allPermissions = ref<ResponseOf<typeof api.aclV2Api.getAllPermissionsAsAdmin>>()

async function getAllPermissions(pq?: Pq) {
  _permissionsPq.value = pq
  allPermissions.value = await api.aclV2Api.getAllPermissionsAsAdmin({ spec: { ...pq } })
}

const schema = object({
  apiPath: string()
    .required()
    .min(1)
    .matches(/^\/.*$/, '路径必须以 / 开头'),
  name: string().nullable(),
  doc: string().nullable(),
  permissions: object({
    id: string().required(),
    name: string().required(),
  })
    .optional()
    .nullable(),
  apiMethods: array()
    .required()
    .min(1)
    .of(string().oneOf(HTTPMethod_CONSTANTS)),
})

type _Dto = RequestOf<typeof api.aclV2Api.postApiAsAdmin>['body']

async function saveApi(dto: _Dto & { apiMethods: HTTPMethod[] }) {
  for (const mtd of dto.apiMethods) {
    await api.aclV2Api.postApiAsAdmin({
      body: { ...dto, apiMethod: mtd },
    })
  }
}
</script>

<template>
<div>
  <PaginatorComp v-model:pr="data" v-model:pq="pq" @change="loadData" />
  <VTable class="max-h-200">
    <thead>
      <tr>
        <th>序列号</th>
        <th>名称</th>
        <th>访问路径</th>
        <th>方法</th>
        <th>需登录</th>
        <th>所需权限</th>
        <th>描述</th>
      </tr>
    </thead>
    <tbody v-if="data?.d">
      <tr v-for="v in data.d" :key="v.id">
        <td>{{ v.id }}</td>
        <td>
          {{ v.name }}
        </td>
        <td>
          <strong>{{ v.apiPath }}</strong>
        </td>
        <td>
          <HttpMethodChip :method="v.apiMethod" />
        </td>
        <td>{{ v.requireLogin ? "是" : "否" }}</td>
        <td>{{ v.permissions?.name }}</td>
        <td>{{ v.doc }}</td>
      </tr>
    </tbody>
  </VTable>

  <!-- 添加 pattern -->
  <YForm :schema="schema" @submit="saveApi">
    <VCard class="mt-12">
      <VCardTitle>添加 pattern</VCardTitle>

      <VCardText>
        <YField name="apiPath" label="glob pattern match expression">
          <VTextField />
        </YField>

        <YField name="apiMethods" label="访问方法">
          <VSelect :items="HTTPMethod_CONSTANTS" multiple chips />
        </YField>
        <YField name="permissions" label="所需访问权限">
          <PagedSelector v-model:pq="_permissionsPq" eager itemTitle="name" :items="allPermissions" @page="getAllPermissions" />
        </YField>
        <YField name="requireLogin" label="需登录">
          <VSwitch color="primary" />
        </YField>
        <YField name="name" label="名称">
          <VTextField />
        </YField>
        <YField name="doc" label="描述">
          <VTextarea />
        </YField>
      </VCardText>
      <VCardActions>
        <VSpacer />
        <VBtn type="submit" color="primary">
          提交
        </VBtn>
      </VCardActions>
    </VCard>
  </YForm>
</div>
</template>
