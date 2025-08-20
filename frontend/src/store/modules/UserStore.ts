import type { AuthService_AuthTokenView as AuthServiceAuthTokenView } from '@/__generated/model/static'
import { Headers as HeadersR } from '@compose/req'
import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

type _LoginResult = AuthServiceAuthTokenView
export const userStoreKey = 'userStore'
export const useUserStore = defineStore(
  userStoreKey,
  () => {
    const authToken = computed({
      get: () => {
        return globalThis.localStorage.getItem(HeadersR.authorization) ?? void 0
      },
      set: (v) => {
        globalThis.localStorage.setItem(HeadersR.authorization, v ?? '')
      },
    })
    const account = ref<string>()
    const roles = ref<string[]>([])
    const depts = ref<string[]>([])
    const timeout = ref<string>()
    const permissions = ref<string[]>([])
    const activeTimeout = ref<string>()
    const authHeaderName = ref<string>()
    const isLogin = computed<boolean>(() => {
      return authToken.value !== void 0 && authToken.value !== '' && authToken.value !== null
    })

    function logoutClear() {
      authToken.value = void 0
      account.value = void 0
      roles.value = []
      permissions.value = []
      depts.value = []
      timeout.value = void 0
      activeTimeout.value = void 0
      authHeaderName.value = void 0
    }

    function setLoginState(loginResult: _LoginResult) {
      if (!loginResult.login) {
        logoutClear()
        return
      }
      // getHeaderName 字段在新API中不存在
      authHeaderName.value = void 0
      roles.value = Array.from(loginResult.roles)
      account.value = loginResult.account
      timeout.value = loginResult.sessionTimeout
      authToken.value = loginResult.sessionId
      permissions.value = Array.from(loginResult.permissions)
      // activeTimeout 字段在新API中不存在
      activeTimeout.value = void 0
    }

    return {
      setLoginState,
      logoutClear,

      timeout,
      isLogin,
      account,
      authToken,
      permissions,
      activeTimeout,
      authHeaderName,
      roles,
      depts,
    }
  },
  { persist: true },
)
