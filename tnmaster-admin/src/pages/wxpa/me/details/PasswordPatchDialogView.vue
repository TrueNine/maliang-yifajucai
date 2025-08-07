<script setup lang="ts">
import { VAlert, VTextField } from 'vuetify/components'
import { z } from 'zod'
import { api } from '@/api'
import { useSnackbar } from '@/components'
import { useUserStore } from '@/store'

const schema = z.object({
  oldPassword: z.string({ message: '旧密码不能为空' }).transform((v) => btoa(v)),
  newPassword: z.string({ message: '新密码不能为空' }).min(8, '密码过短').max(128, '密码过长').regex(/^[a-z0-9]$/i, '密码只能包含字母和数字').nonempty('密码不能为空').transform((v) => btoa(v)),
  newPasswordConfirm: z.string({ message: '新密码不能为空' }).min(8, '密码过短').max(128, '密码过长').regex(/^[a-z0-9]$/i, '密码只能包含字母和数字').nonempty('密码不能为空').transform((v) => btoa(v)),
}).refine((data) => data.newPassword === data.newPasswordConfirm, {
  message: '两次输入的密码不一致',
})
type Schema = z.infer<typeof schema>

const router = useRouter()
const userStore = useUserStore()

const closeFn = ref<() => void>()
const snackbar = useSnackbar()

async function submit(body: Schema) {
  await snackbar.message(
    api.authApi.patchMePassword(body),
    {
      message: '修改成功',
      errorMessage: '密码更改错误',
    },
  )

  closeFn.value?.()
  userStore.logoutClear()
  await router.push('/wxpa/auth')
}
</script>

<template>
<YForm class="wh-full" :schema="schema" @submit="submit">
  <FullScreenConfirmLayout>
    <YField name="oldPassword" label="旧密码">
      <VTextField type="password" />
    </YField>
    <YField name="newPassword" label="新密码">
      <VTextField type="password" />
    </YField>
    <YField name="newPasswordConfirm" label="再次确认新密码">
      <VTextField type="password" />
    </YField>
    <YFieldMessage name="" />
    <VAlert>
      <p>密码必须包含字母、数字，长度大于 8 位</p>
      <p>如果账号通过第三方登录，您可以通过找回密码来修改密码。</p>
    </VAlert>
    <template #ok="{ onClose }">
      <VBtn variant="flat" type="submit" color="primary" block @click="closeFn = onClose">
        提交
      </VBtn>
    </template>
  </FullScreenConfirmLayout>
</YForm>
</template>
