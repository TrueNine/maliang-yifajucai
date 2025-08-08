import type {Executor} from '../';

/**
 * ## 常用数据/工具接口
 */
export class CommonApi {
  
  constructor(private executor: Executor) {}
  
  /**
   * ## AES解密
   */
  readonly aseDecrypt: (options: CommonApiOptions['aseDecrypt']) => Promise<
    string
  > = async(options) => {
    let _uri = '/v2/common/ass_decrypt';
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.ciphertext;
    _uri += _separator
    _uri += 'ciphertext='
    _uri += encodeURIComponent(_value);
    _separator = '&';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<string>;
  }
  
  /**
   * ## AES加密
   */
  readonly aseEncrypt: (options: CommonApiOptions['aseEncrypt']) => Promise<
    string
  > = async(options) => {
    let _uri = '/v2/common/ase_encrypt';
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.plaintext;
    _uri += _separator
    _uri += 'plaintext='
    _uri += encodeURIComponent(_value);
    _separator = '&';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<string>;
  }
  
  /**
   * ## 所有少数民族
   */
  readonly getAllChinaMinorities: () => Promise<
    Array<string>
  > = async() => {
    let _uri = '/v2/common/china_minorities';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<Array<string>>;
  }
  
  /**
   * ## 获取一个业务单号
   */
  readonly getBusinessOrderCode: () => Promise<
    string
  > = async() => {
    let _uri = '/v2/common/business_order_code';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<string>;
  }
  
  /**
   * ## 获取当前用户的 ip 地址
   */
  readonly getCurrentSessionRemoteIpAddr: () => Promise<
    string
  > = async() => {
    let _uri = '/v2/common/ip';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<string>;
  }
  
  /**
   * ## 雪花算法id
   */
  readonly getServerSnowflakeId: () => Promise<
    string
  > = async() => {
    let _uri = '/v2/common/server_snowflake_id';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<string>;
  }
  
  /**
   * ## 获取服务器异常
   * > 这通常用于测试目的
   */
  readonly getServerThrowable: () => Promise<
    void
  > = async() => {
    let _uri = '/v2/common/server_throwable';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<void>;
  }
  
  /**
   * ## 获取一个 java 版的 uuid
   */
  readonly getUuid: () => Promise<
    string
  > = async() => {
    let _uri = '/v2/common/java_uuid';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<string>;
  }
  
  /**
   * ## 用于检测服务器是否存活
   */
  readonly pong: () => Promise<
    number
  > = async() => {
    let _uri = '/v2/common/ping';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<number>;
  }
}

export type CommonApiOptions = {
  'pong': {}, 
  'getCurrentSessionRemoteIpAddr': {}, 
  'aseDecrypt': {
    ciphertext: string
  }, 
  'aseEncrypt': {
    plaintext: string
  }, 
  'getUuid': {}, 
  'getServerSnowflakeId': {}, 
  'getBusinessOrderCode': {}, 
  'getAllChinaMinorities': {}, 
  'getServerThrowable': {}
}
