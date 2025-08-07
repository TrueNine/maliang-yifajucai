<script setup lang="ts">
import { useDisplay } from 'vuetify'
import bgImg from '@/assets/login-bg.svg'

import logoPngImage from '@/assets/logo.png'

enum LoginTab {
  Login = 'login',
  Register = 'register',
}

const tab = ref(LoginTab.Login)
const { mobile } = useDisplay()
</script>

<template>
<TnSnackbar>
  <div class="h-screen w-screen f-c">
    <img :src="bgImg" alt="login-bg" class="absolute z--1 wh-full object-cover" />
    <div :class="{ 'max-w-370px min-w-370px': mobile, 'max-w-1/4 min-w-1/5': !mobile }">
      <VCard>
        <VCardTitle>
          <div class="mb-4 flex flex-col items-center justify-center gap-2">
            <div class="h-150px w-150px overflow-hidden rounded-full">
              <img :src="logoPngImage" class="h-150px w-150px object-cover" />
            </div>
            <div>易发聚才（管理端）</div>
          </div>
          <VTabs v-model="tab">
            <VTab :value="LoginTab.Login">
              登录
            </VTab>
            <VTab :value="LoginTab.Register">
              注册账号（尚未开放）
            </VTab>
          </VTabs>
        </VCardTitle>
        <VCardText>
          <VWindow v-model="tab">
            <VWindowItem :value="LoginTab.Login">
              <RouterView name="login" />
            </VWindowItem>

            <VWindowItem :value="LoginTab.Register">
              <RouterView name="register" />
            </VWindowItem>
          </VWindow>
        </VCardText>
      </VCard>
    </div>
  </div>
</TnSnackbar>
</template>
