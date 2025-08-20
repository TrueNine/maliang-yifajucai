import { api } from '@/api'
import { useUserStore } from '@/store'

class AuthService {
  async loginByAccount(account: string, password: string) {
    if (!account) {
      return false
    }
    if (!password) {
      return false
    }
    if (account.length < 4) {
      return false
    }
    if (password.length < 4) {
      return false
    }
    const userStore = useUserStore()
    userStore.$reset()
    const a = await api.authApi.loginBySystemAccount({ body: { account, password: btoa(password) } })
    userStore.authToken = a.sessionId
    userStore.roles = Array.from(a.roles)
    userStore.timeout = a.sessionTimeout
    // activeTimeout 字段在新API中不存在
    userStore.activeTimeout = void 0
    userStore.permissions = Array.from(a.permissions)
    return true
  }
}

export const authService = new AuthService()
