<script setup lang="ts">
import loginBgImage from '@/assets/login-bg-mobile.svg'
import logoPngImage from '@/assets/logo.png'

enum LoginType {
  SMS,
  ACCOUNT,
  WXPA,
}

const tab = ref(LoginType.SMS)
const enableAccountLogin = ref(false)

function toAccountLogin() {
  enableAccountLogin.value = true
  tab.value = LoginType.ACCOUNT
}

const isWx = computed(() => {
  return window.navigator.userAgent.toLowerCase().includes('micromessenger')
})

// 检测设备类型
const isMobile = ref(false)
onMounted(() => {
  checkDeviceType()
  window.addEventListener('resize', checkDeviceType)
})
onUnmounted(() => {
  window.removeEventListener('resize', checkDeviceType)
})

function checkDeviceType() {
  isMobile.value = window.innerWidth < 600
}
</script>

<template>
<div class="h-screen w-screen flex transition-all duration-100 ease-in-out bg-cover bg-center bg-no-repeat" :class="[isMobile ? 'flex-col items-center justify-around' : 'items-center']">
  <img :src="loginBgImage" class="absolute left-0 top-0 h-screen w-screen object-cover" alt="登录背景" />
  <!-- 左侧LOGO区域 -->
  <div
    class="transform transition-all duration-300 ease-in-out" :class="[
      isMobile ? 'flex flex-col items-center justify-center gap-2' : 'flex-1 flex flex-col items-center justify-center gap-4',
    ]"
  >
    <div class="h-150px w-150px overflow-hidden rounded-full shadow-md transition-transform duration-100">
      <img :src="logoPngImage" class="h-150px w-150px object-cover" alt="LOGO" />
    </div>
    <div class="text-8 font-extrabold transition-all duration-100">
      易发聚才
    </div>
  </div>

  <!-- 右侧登录区域 -->
  <div
    class="transform transition-all duration-100 ease-in-out" :class="[
      isMobile ? 'w-full px-4' : 'flex-1 flex flex-col items-center justify-center gap-8 h-full',
    ]"
  >
    <VCard class="max-w-500px w-full transition-all duration-100">
      <VCardTitle>
        <div class="h-12 min-h-12">
          <VTabs v-if="enableAccountLogin" v-model="tab" alignTabs="center" color="primary">
            <VTab :value="LoginType.SMS">
              验证码登录
            </VTab>
            <VTab :value="LoginType.ACCOUNT">
              账号登录
            </VTab>
          </VTabs>
        </div>
      </VCardTitle>
      <VCardText>
        <VWindow v-model="tab" class="w-full">
          <VWindowItem :value="LoginType.SMS">
            <div class="my-4 w-full px-4 transition-all duration-300">
              <RouterView name="sms" />
            </div>
          </VWindowItem>
          <VWindowItem :value="LoginType.ACCOUNT">
            <div class="my-4 w-full px-4 transition-all duration-300">
              <RouterView name="account" />
            </div>
          </VWindowItem>
        </VWindow>
      </VCardText>
      <VCardActions
        class="d-flex justify-space-around transition-all duration-100" :class="[
          isMobile ? 'w-full px-4 mb-8' : 'w-full max-w-500px',
        ]"
      >
        <!-- 底部按钮区域 -->

        <RouterView v-if="isWx" name="wxpa" />
        <div class="flex flex-col items-center justify-center transition-all duration-100">
          <VBtn icon variant="tonal" @click="toAccountLogin">
            <YIco class="i-mdi:form-textbox-password text-6" />
          </VBtn>
          <div class="text-medium-emphasis">
            账号登录
          </div>
        </div>
      </VCardActions>
    </VCard>
  </div>
</div>
</template>
