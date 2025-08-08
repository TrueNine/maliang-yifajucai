export const PlatformType_CONSTANTS = [
  'NONE', 
  'WECHAT_PUBLIC_ACCOUNT', 
  'WECHAT_MINI_PROGRAM', 
  'WECHAT_OPEN_PLATFORM', 
  'MOBILE_WEB_VIEW', 
  'MOBILE_H5', 
  'WECHAT_WEB_SITE', 
  'PC_WEB_SITE', 
  'WEB_ADMIN_SITE', 
  'OTHER'
] as const;
export type PlatformType = typeof PlatformType_CONSTANTS[number];
