<script setup lang="ts">
import type { Pq, RefId } from '@compose/types'
import type { AuditTyping, ResponseOf } from '@/api'
import { api, AuditTyping_OPTIONS, RelationItemTyping_OPTIONS } from '@/api'
import { useSnackbar } from '@/components/TnSnackbar'

const pq = ref<Pq>({ s: 20, o: 0 })
const data = ref<ResponseOf<typeof api.blackListV2Api.getBlackListsAsAdmin>>()

async function initGet() {
  data.value = await api.blackListV2Api.getBlackListsAsAdmin({ pq: pq.value })
}

const popup = useSnackbar()

async function changeAudit(id: RefId, auditStatus: AuditTyping) {
  await popup?.message(async () => await api.blackListV2Api.patchAuditStatusByIdAsAdmin({ id: id as unknown as number, auditStatus }), {
    message: '审核状态更改成功',
  })
}

onMounted(initGet)
</script>

<template>
<PaginatorComp v-model:pq="pq" :pr="data" @update:pq="initGet" />

<ElTable v-if="data != null" stripe :data="data?.d">
  <YPreAuthorize :hasAnyRoles="['ROOT']">
    <ElTableColumn width="550" prop="auditStatus" label="审核状态">
      <template #default="{ row }">
        <AuditCheckBtnGroupComp v-model="row.auditStatus" @change="(v: AuditTyping) => changeAudit(row.id, v)" />
      </template>
    </ElTableColumn>

    <template #forbidden>
      <ElTableColumn width="200" prop="auditStatus" label="审核状态">
        <template #default="{ row }">
          <VChip>{{ AuditTyping_OPTIONS.find(e => e.v === row.auditStatus)?.k }}</VChip>
        </template>
      </ElTableColumn>
    </template>
  </YPreAuthorize>

  <ElTableColumn prop="name" label="姓名">
    <template #default="{ row }">
      {{ row.blackUserInfo?.metadataName }}
    </template>
  </ElTableColumn>

  <ElTableColumn prop="eventDoc" label="事件来由" />

  <ElTableColumn prop="account" label="账号">
    <template #default="{ row }">
      {{ row.blackUserInfo?.account?.account }}
    </template>
  </ElTableColumn>

  <ElTableColumn prop="reItemType" label="关系类型">
    <template #default="{ row }">
      {{ RelationItemTyping_OPTIONS.find(e => e.v === row.reItemType)?.k }}
    </template>
  </ElTableColumn>
</ElTable>
</template>
