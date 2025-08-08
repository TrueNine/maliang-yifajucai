import type {Executor} from '../';

/**
 * 敏感信息模拟接口
 * 
 */
export class SensitiveMockApi {
  
  constructor(private executor: Executor) {}
  
  /**
   * ## 获取一个当前数据库内不存在的随机电话号码
   */
  readonly getGlobalNowUserInfoUniquePhone: () => Promise<
    string
  > = async() => {
    let _uri = '/v2/sensitiveMock/global_user_info_unique_phone';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<string>;
  }
}

export type SensitiveMockApiOptions = {
  'getGlobalNowUserInfoUniquePhone': {}
}
