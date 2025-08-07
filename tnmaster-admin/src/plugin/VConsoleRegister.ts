import { BuildConfig } from '@/config'

function register() {
  if (BuildConfig.prod) {
    return
  }

  const ua = navigator.userAgent.toLowerCase()

  const uaIsMobile = Array.from(
    new Set(['Android', 'iPhone', 'SymbianOS', 'Windows Phone', 'iPad', 'iPod', 'Huawei', 'Harmony', 'wexin', 'wechat', 'qq', 'tencent']),
  )
    .map((e) => e.toLowerCase())
    .some((v) => ua.toLowerCase().indexOf(v) > 0)
  if (!uaIsMobile) {
    return
  }
  if (!ua.includes('micromessenger')) {
    return
  }
  void import('vconsole').then(({ default: VConsole }) => {
    const vConsole = new VConsole()
    vConsole.log.log('vconsole loaded')
  })
}

export function registerVConsole() {
  register()
}
