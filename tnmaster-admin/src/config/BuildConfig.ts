import type { InjectionKey, Ref } from 'vue'

// eslint-disable-next-line ts/ban-ts-comment
// @ts-ignore
export const GLOBAL_CONFIG_BASE_URL = import.meta.env.VITE_APP_BASE_URL as unknown as string
// eslint-disable-next-line ts/ban-ts-comment
// @ts-ignore
export const GLOBAL_CONFIG_BASE_DOMAIN = import.meta.env.VITE_APP_BASE_DOMAIN as unknown as string
// eslint-disable-next-line ts/ban-ts-comment
// @ts-ignore
export const BuildConfigInjectionKey = Symbol('InjectionKey<Ref<typeof BuildConfig>>') as InjectionKey<Ref<typeof BuildConfig>>

/**
 * 全项目环境变量，以及构建配置
 */
export const BuildConfig = {
  baseUrl: GLOBAL_CONFIG_BASE_URL,
  baseDomain: GLOBAL_CONFIG_BASE_DOMAIN,
  pingUrl: `${GLOBAL_CONFIG_BASE_URL}/v1/heartbeat/ping`,
  reportErrorUrl: `${GLOBAL_CONFIG_BASE_URL}/v1/error/report`,
  // eslint-disable-next-line ts/ban-ts-comment
  // @ts-ignore
  prod: import.meta.env.VITE_APP_PROD === 'true',
  // eslint-disable-next-line ts/ban-ts-comment
  // @ts-ignore
  appName: import.meta.env.VITE_APP_NAME as unknown as string,
  // eslint-disable-next-line ts/ban-ts-comment
  // @ts-ignore
  envName: import.meta.env.VITE_APP_ENV_NAME as unknown as string,
  // eslint-disable-next-line ts/ban-ts-comment
  // @ts-ignore
  meta: import.meta.env as unknown as Record<string, string>,
}
