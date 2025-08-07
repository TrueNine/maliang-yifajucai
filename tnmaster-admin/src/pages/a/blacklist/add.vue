<script setup lang="ts">
import type { RefId } from '@compose/types'
import type { RequestOf } from '@/api'
import { api } from '@/api'
import { useSnackbar } from '@/components/TnSnackbar'

interface UserSelection {
  userId?: RefId
  infoId?: RefId
  displayName?: string
}

const dto = ref<RequestOf<typeof api.blackListV2Api.postBlackListAsAdmin>['body']>({
  eventDoc: '',
  blackListRelations: [],
})

const popup = useSnackbar()

async function submit() {
  await popup?.message(api.blackListV2Api.postBlackListAsAdmin({ body: dto.value }))
}

const reportUser = ref<UserSelection>({})
const blackUser = ref<UserSelection>({})

watch([reportUser, blackUser], ([a, b]) => {
  dto.value.reportUserId = a.userId
  dto.value.reportUserInfoId = a.infoId

  dto.value.blackUserInfoId = b.infoId
  dto.value.reItemType = 'CUSTOMER'
})
</script>

<template>
<div class="space-y-2">
  <div>
    <UserSearchView v-model="reportUser" label="上报用户" />
  </div>
  <div>
    <UserSearchView v-model="blackUser" label="拉黑用户" />
  </div>
</div>

<VTextarea v-model="dto.eventDoc" label="原因" />

<VBtn color="primary" :block="true" @click="submit">
  提交
</VBtn>
</template>
