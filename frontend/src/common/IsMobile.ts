const deviceDetectionCache = {
  isMobile: void 0 as boolean | undefined,
  isWechat: void 0 as boolean | undefined,
  isMiniProgram: void 0 as boolean | undefined,
}

// 为微信小程序环境声明全局类型
declare global {
  interface Window {
    __wxjs_environment?: 'miniprogram'
  }
}

/**
 * 检测是否为微信环境
 */
function isWechat(): boolean {
  if (deviceDetectionCache.isWechat !== void 0) {
    return deviceDetectionCache.isWechat
  }
  const ua = navigator.userAgent.toLowerCase()
  deviceDetectionCache.isWechat = /micromessenger/.test(ua)
  return deviceDetectionCache.isWechat
}

/**
 * 检测是否为小程序环境
 */
function isMiniProgram(): boolean {
  if (deviceDetectionCache.isMiniProgram !== void 0) {
    return deviceDetectionCache.isMiniProgram
  }
  // 检测小程序环境
  const ua = navigator.userAgent.toLowerCase()
  deviceDetectionCache.isMiniProgram = /miniprogram/.test(ua) || window.__wxjs_environment === 'miniprogram'
  return deviceDetectionCache.isMiniProgram
}

/**
 * 检测是否为移动设备
 * 支持检测:
 * 1. 传统移动设备
 * 2. 现代移动设备特征
 * 3. 微信环境
 * 4. 小程序环境
 */
export function isMobile(): boolean {
  // 使用缓存结果
  if (deviceDetectionCache.isMobile !== void 0) {
    return deviceDetectionCache.isMobile
  }

  // 如果是小程序或微信环境，直接返回 true
  if (isMiniProgram() || isWechat()) {
    deviceDetectionCache.isMobile = true
    return true
  }

  // 传统 UA 检测（包含最新移动设备标识）
  const ua = navigator.userAgent.toLowerCase()
  const mobileKeywords = [
    /android/,
    /iphone/,
    /ipod/,
    /ipad/,
    /windows phone/,
    /mobile/,
    /webos/,
    /blackberry/,
    /playbook/,
    /kindle/,
    /silk/,
    /meego/,
    /opera mini/,
    /harmony/,
    /huawei/,
    /ucbrowser/,
    /qqbrowser/,
    /miuibrowser/,
  ]

  // 屏幕特征检测（考虑设备像素比和方向）
  const maxMobileWidth = 1024
  const screenWidth = window.screen.width * (window.devicePixelRatio || 1)
  const isSmallScreen = screenWidth <= maxMobileWidth

  // 输入方式检测（现代浏览器支持）
  const hasCoarsePointer = matchMedia('(pointer: coarse)').matches
  const hasTouch = 'ontouchstart' in window || navigator.maxTouchPoints > 0 || matchMedia('(any-hover: none)').matches

  // 移动设备特征检测
  const result = mobileKeywords.some((re) => re.test(ua))
    || (isSmallScreen && (hasCoarsePointer || hasTouch))
    || matchMedia('(max-device-width: 1024px)').matches
    || matchMedia('(orientation: portrait)').matches

  // 缓存结果
  deviceDetectionCache.isMobile = result
  return result
}
