import type {Executor} from '../';
import type {WxpaSignature, WxpaUserInfo} from '../model/static/';

/**
 * # 微信公众号接口
 */
export class WechatPublicAccountApi {
  
  constructor(private executor: Executor) {}
  
  /**
   * ## 对当前 url 进行签名
   */
  readonly getJsApiUrlSignature: (options: WechatPublicAccountApiOptions['getJsApiUrlSignature']) => Promise<
    WxpaSignature | undefined
  > = async(options) => {
    let _uri = '/v2/wxpa/js_api_signature';
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.url;
    _uri += _separator
    _uri += 'url='
    _uri += encodeURIComponent(_value);
    _separator = '&';
    _value = options.nonceString;
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'nonceString='
      _uri += encodeURIComponent(_value);
      _separator = '&';
    }
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<WxpaSignature | undefined>;
  }
  
  /**
   * ## 获取Token状态信息
   */
  readonly getTokenStatus: () => Promise<
    {[key:string]: string}
  > = async() => {
    let _uri = '/v2/wxpa/token_status';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<{[key:string]: string}>;
  }
  
  /**
   * ## code 换取用户信息
   * 
   * @parameter {WechatPublicAccountApiOptions['getUserInfoByCode']} options
   * - code 获取用户信息的 code
   */
  readonly getUserInfoByCode: (options: WechatPublicAccountApiOptions['getUserInfoByCode']) => Promise<
    WxpaUserInfo | undefined
  > = async(options) => {
    let _uri = '/v2/wxpa/user_info';
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.code;
    _uri += _separator
    _uri += 'code='
    _uri += encodeURIComponent(_value);
    _separator = '&';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<WxpaUserInfo | undefined>;
  }
  
  /**
   * ## 强制刷新所有Token
   */
  readonly refreshTokens: () => Promise<
    string
  > = async() => {
    let _uri = '/v2/wxpa/refresh_tokens';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<string>;
  }
  
  /**
   * ## 微信消息验证接口
   */
  readonly verifyBasicConfig: (options: WechatPublicAccountApiOptions['verifyBasicConfig']) => Promise<
    string | undefined
  > = async(options) => {
    let _uri = '/v2/wxpa';
    let _separator = _uri.indexOf('?') === -1 ? '?' : '&';
    let _value: any = undefined;
    _value = options.signature;
    _uri += _separator
    _uri += 'signature='
    _uri += encodeURIComponent(_value);
    _separator = '&';
    _value = options.timestamp;
    _uri += _separator
    _uri += 'timestamp='
    _uri += encodeURIComponent(_value);
    _separator = '&';
    _value = options.nonce;
    _uri += _separator
    _uri += 'nonce='
    _uri += encodeURIComponent(_value);
    _separator = '&';
    _value = options.echostr;
    _uri += _separator
    _uri += 'echostr='
    _uri += encodeURIComponent(_value);
    _separator = '&';
    return (await this.executor({uri: _uri, method: 'GET'})) as Promise<string | undefined>;
  }
}

export type WechatPublicAccountApiOptions = {
  'getUserInfoByCode': {
    /**
     * 获取用户信息的 code
     */
    code: string
  }, 
  'verifyBasicConfig': {
    signature: string, 
    timestamp: string, 
    nonce: string, 
    echostr: string
  }, 
  'getTokenStatus': {}, 
  'refreshTokens': {}, 
  'getJsApiUrlSignature': {
    url: string, 
    nonceString?: string | undefined
  }
}
