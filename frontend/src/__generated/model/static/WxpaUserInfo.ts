export interface WxpaUserInfo {
  openId: string;
  nickname?: string | undefined;
  privilege: Array<string>;
  unionId?: string | undefined;
}
