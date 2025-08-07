<script setup lang="ts">
import type { RequestOf } from '@/api'
import { object, string } from 'yup'
import { api } from '@/api'
import { useSnackbar } from '@/components/TnSnackbar'
import { useUserStore } from '@/store'

const schema = object({
  account: string().required('必须填写登录账号'),
  password: string().required('必须填写密码').min(6, '密码过短'),
})

const pop = useSnackbar()
const router = useRouter()
const userStore = useUserStore()
type LoginDto = RequestOf<typeof api.authApi.loginBySystemAccount>['body']

async function login(dto: LoginDto) {
  await pop?.message(async () => {
    if (!dto.password) {
      return
    }
    dto.password = btoa(dto.password)
    const e = await api.authApi.loginBySystemAccount({
      body: dto,
    })
    userStore.setLoginState(e)
    void router.push('/wxpa')
  })
}

const pwdType = ref('password')
</script>

<template>
<div class="w-full">
  <YForm :schema="schema" @submit="login">
    <YField name="account" label="账号" placeholder="系统直接分配的账号">
      <VTextField>
        <template #append-inner>
          <div>
            <VBtn :disabled="true" color="primary" variant="text">
              找回密码
            </VBtn>
          </div>
        </template>
      </VTextField>
    </YField>
    <YField name="password" label="密码">
      <VTextField :type="pwdType">
        <template #append-inner>
          <div>
            <VBtn icon variant="text" @click="pwdType = pwdType === 'password' ? 'text' : 'password'">
              <YIco class="i-mdi:eye-outline" />
            </VBtn>
          </div>
        </template>
      </VTextField>
    </YField>
    <VBtn :block="true" type="submit" color="primary" variant="elevated">
      登录
    </VBtn>
  </YForm>
</div>
</template>
