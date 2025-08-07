<script setup lang="ts">
import type { entities, ResponseOf } from '@/api'
import { Pw } from '@compose/shared'
import { api } from '@/api'

interface Props {
  roleGroup?: entities.Dynamic_RoleGroup
}

const props = withDefaults(defineProps<Props>(), {
  roleGroup: void 0,
})
const pq = ref({ ...Pw.DEFAULT_MAX })
const pr = ref<ResponseOf<typeof api.aclV2Api.getRoleGroupUserAccounts>>()

function init() {
  void nextTick(async () => {
    if (props.roleGroup?.id) {
      pr.value = await api.aclV2Api.getRoleGroupUserAccounts({
        spec: {
          ...pq.value,
          roleGroupIds: [
            props.roleGroup.id,
          ],
        },
      })
    }
  })
}
watch(
  () => props.roleGroup,
  () => {
    pq.value = { ...Pw.DEFAULT_MAX }
    init()
  },
)
</script>

<template>
<VCard>
  <VCardTitle>{{ roleGroup?.name }} 角色组所属的用户</VCardTitle>
  <VCardText v-if="roleGroup">
    <VList v-if="pr?.d">
      <UserListItemView v-for="u in pr.d" :key="u.id" :user="u" />
    </VList>
    <YPager v-model="pq" v-model:pr="pr" />
  </VCardText>
</VCard>
</template>
