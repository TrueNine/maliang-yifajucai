<script setup lang="ts">
import type { timestamp } from '@compose/types'
import type { entities } from '@/api'
import { formatDatetime } from '@compose/external/dayjs'

interface Props {
  user?: entities.Dynamic_UserAccount | null
}

withDefaults(defineProps<Props>(), {
  user: void 0,
})
function getTime(l?: timestamp) {
  if (!l) {
    return '-'
  }
  return formatDatetime(l)
}
</script>

<template>
<VListItem v-if="user" :value="user">
  <VListItemTitle>{{ user.nickName || user.account }}</VListItemTitle>
  <VListItemSubtitle>{{ user.doc ?? '-' }}</VListItemSubtitle>
  <template #append>
    最后登陆于：{{ getTime(user.lastLoginTime) }}
  </template>
</VListItem>
</template>
