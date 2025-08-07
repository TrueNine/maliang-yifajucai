<script lang="ts" setup>
import type { TypeOf } from 'zod'
import { useDisplay } from 'vuetify'
import { useSnackbar } from '@/components'
import { authService } from '@/service/AuthService'

const router = useRouter()
const { mobile } = useDisplay()
const schema = z.object({
  account: z.string({ message: '必须输入账号' }).min(4, '账号过短').nonempty(),
  password: z.string({ message: '密码不能为空' }).min(4, '密码过短').nonempty(),
})
const snackbar = useSnackbar()
type Schema = TypeOf<typeof schema>
async function login(value: Schema) {
  const isLogin = await snackbar.message(authService.loginByAccount(value.account, value.password))
  if (isLogin) {
    await router.push(mobile.value ? '/wxpa' : '/a/dashboard')
  }
}

function err() {
  snackbar.error('账号密码填写错误')
}

function routeToMobileLogin() {
  void router.push('/wxpa/auth')
}
</script>

<template>
<YForm :schema="schema" @error="err" @submit="login">
  <YField name="account" label="账号">
    <VTextField />
  </YField>
  <YField name="password" label="密码">
    <VTextField type="password" />
  </YField>
  <div class="flex justify-center py-4">
    <a class="c-primary" @click="routeToMobileLogin">
      我不是管理员，我是用户
    </a>
  </div>

  <VBtn block color="primary" type="submit">
    登录
  </VBtn>
</YForm>
</template>
