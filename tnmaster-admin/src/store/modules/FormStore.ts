import type { Dynamic_DisInfo as DisInfo } from '@/__generated/model/dynamic/Dynamic_DisInfo'
import type { Dynamic_Job as Job } from '@/__generated/model/dynamic/Dynamic_Job'
import type { Dynamic_UserInfo as UserInfo } from '@/__generated/model/dynamic/Dynamic_UserInfo'
import type { UserInfoAdminSpec } from '@/__generated/model/static'
import { defineStore } from 'pinia'

import { ref } from 'vue'

export const formStoreKey = 'formStore'
export const useFormStore = defineStore(
  formStoreKey,
  () => {
    const pubJob = ref<Job>({ title: '' })

    function resetJob() {
      pubJob.value = {
        title: '',
        rqDisRule: new Uint8Array(Array.from({ length: 28 }, () => 0)) as unknown as number[],
      }
    }

    return {
      resetJob,
      userInfo: ref<UserInfo>({}),
      userInfoSpec: ref<UserInfoAdminSpec>({}),
      disInfo: ref<DisInfo>({}),
      job: pubJob,
    }
  },
  {
    persist: true,
  },
)
