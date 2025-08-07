<script setup lang="tsx">
import { takeUnless } from '@compose/shared'
import { api } from '@/api'
import NickNamePatchDialogView from '@/pages/wxpa/me/details/NickNamePatchDialogView.vue'
import PasswordPatchDialogView from '@/pages/wxpa/me/details/PasswordPatchDialogView.vue'
import { useFormStore, useUserStore } from '@/store'

const { userInfo, disInfo } = storeToRefs(useFormStore())
const router = useRouter()

async function getInfo() {
  const info = await api.userInfoV2Api.getUserInfoAsMe()
  if (!info) {
    return
  }
  userInfo.value = info
  if (takeUnless(userInfo.value, (it) => !!((it.firstName ?? '') + (it.lastName ?? '')))) {
    await router.push('/wxpa/me/details')
  } else {
    const d = await api.userInfoV2Api.getDisInfoAsMe()
    if (d) {
      disInfo.value = d
    }
  }
}

onMounted(getInfo)

const userStore = useUserStore()

async function logout() {
  userStore.logoutClear()
  await router.push('/wxpa/auth')
}

const active = ref<boolean>(false)

enum DialogType {
  PASSWORD = 'password',
  USER_INFO = 'userinfo',
  NICK_NAME = 'nickname',
}

const dialogType = ref<DialogType>()
const dialogTitle = ref<string>()

function openDialog(type: DialogType, title?: string) {
  dialogType.value = type
  dialogTitle.value = title
  active.value = dialogType.value !== void 0
}

const dialogRenderComponent = computed(() => {
  switch (dialogType.value) {
    case DialogType.NICK_NAME: return NickNamePatchDialogView
    case DialogType.PASSWORD: return PasswordPatchDialogView
    default:throw new Error('unknown dialog type')
  }
})
</script>

<template>
<VCard>
  <VCardText>
    <VList>
      <VListItem :value="DialogType.NICK_NAME" @click="() => openDialog(DialogType.NICK_NAME, '修改呢称')">
        呢称
        <template #append>
          <div v-if="userInfo?.account?.nickName">
            {{ userInfo?.account?.nickName }}
          </div>
          <div v-else>
            <VChip>{{ "未设置" }}</VChip>
          </div>
        </template>
      </VListItem>
      <VListItem :value="DialogType.PASSWORD" @click="() => openDialog(DialogType.PASSWORD, '修改密码')">
        修改密码
      </VListItem>
    </VList>
  </VCardText>

  <VCardActions>
    <VBtn variant="tonal" :block="true" color="error" @click="logout">
      退出登录
    </VBtn>
  </VCardActions>
</VCard>

<!-- 修改信息弹窗 -->
<TnFullScreenDialog v-model="active" :title="dialogTitle">
  <component :is="dialogRenderComponent" @ok="getInfo" />
</TnFullScreenDialog>
</template>
