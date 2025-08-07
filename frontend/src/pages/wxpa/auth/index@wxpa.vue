<script setup lang="ts">
import type { ResponseOf } from '@/api'
import { loadWxpaJsSdk } from '@compose/psdk-wxpa'
import { api } from '@/api'
import { useSnackbar } from '@/components'
import { useUserStore } from '@/store'

const router = useRouter()
const route = useRoute()

const userStore = useUserStore()
const sdkLoaded = ref(false)

type _WxpaSignatureResp = ResponseOf<typeof api.wechatPublicAccountApi.getJsApiUrlSignature>
const wxInfo = ref<_WxpaSignatureResp>()
const snackbar = useSnackbar()

// 加载微信 js sdk
async function loadJsSdk() {
  wxInfo.value = await api.wechatPublicAccountApi.getJsApiUrlSignature({ url: window.location.href })
  if (!wxInfo.value.appId || !wxInfo.value.nonceString || !wxInfo.value.sign || !wxInfo.value.timestamp) {
    return
  }
  loadWxpaJsSdk(() => (sdkLoaded.value = true), {
    appId: wxInfo.value.appId,
    nonceStr: wxInfo.value.nonceString,
    signature: wxInfo.value.sign,
    timestamp: wxInfo.value.timestamp,
  })
}

// 微信登录
async function loginByWechatByAccount(code: string) {
  try {
    const login = await api.authApi.loginByWxpaCode({ code })
    if (!login) {
      throw new Error('微信登录失败：未获取到用户信息')
    }
    userStore.logoutClear()
    userStore.setLoginState(login)
    void router.push({
      path: '/wxpa',
    })
  } catch (e) {
    snackbar.error('微信登录失败')
    console.error('微信登录失败：', e)
    void router.replace('/wxpa/auth')
  }
}

// 微信登录 code
const wechatAuthCode = route.query.code
if (wechatAuthCode) {
  void loginByWechatByAccount(wechatAuthCode as string)
}

// 获取微信用户信息
async function getWechatUserInfo() {
  await loadJsSdk()
  if (!wxInfo.value?.appId) {
    throw new Error('wxInfo.appId is undefined')
  }
  const params = new URLSearchParams({
    appid: wxInfo.value.appId,
    redirect_uri: window.location.href,
    response_type: 'code',
    scope: 'snsapi_userinfo',
    state: '',
  })
  window.location.href = `https://open.weixin.qq.com/connect/oauth2/authorize?${params.toString()}#wechat_redirect`
}
</script>

<template>
<div class="flex flex-col items-center justify-center">
  <div>
    <VBtn icon color="success" variant="tonal" @click="getWechatUserInfo">
      <YIco class="i-mdi:wechat text-6" />
    </VBtn>
  </div>
  <div class="text-medium-emphasis">
    微信登录
  </div>
</div>
</template>
