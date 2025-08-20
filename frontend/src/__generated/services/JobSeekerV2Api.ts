import type { Executor } from '../'
import type { JobSeekerDto } from '../model/dto/'
import type { JobSeekerAdminSpec } from '../model/static/'

/**
 * 第二版求职者API接口
 */
export class JobSeekerV2Api {
  constructor(private executor: Executor) {}

  /**
   * 后台筛选求职者
   */
  readonly getAdminJobSeekerList: (options: JobSeekerV2ApiOptions['getAdminJobSeekerList']) => Promise<
    Array<JobSeekerDto['JobSeekerV2Api/ADMIN_JOB_SEEKER']>
  > = async (options) => {
    let _uri = '/v2/jobSeeker/admin'
    let _separator = !_uri.includes('?') ? '?' : '&'
    let _value: any
    _value = options.spec.id
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'id='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.exAddressCode
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'exAddressCode='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.regAddressCode
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'regAddressCode='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.maxCreateDatetime
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'maxCreateDatetime='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.degree
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'degree='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.accountNickName
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'accountNickName='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.accountAccount
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'accountAccount='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.userInfoMaxAge
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'userInfoMaxAge='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.userInfoDisInfoDsType
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'userInfoDisInfoDsType='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    _value = options.spec.userInfoDisInfoLevel
    if (_value !== undefined && _value !== null) {
      _uri += _separator
      _uri += 'userInfoDisInfoLevel='
      _uri += encodeURIComponent(_value)
      _separator = '&'
    }
    return (await this.executor({ uri: _uri, method: 'GET' })) as Promise<Array<JobSeekerDto['JobSeekerV2Api/ADMIN_JOB_SEEKER']>>
  }
}

export interface JobSeekerV2ApiOptions {
  getAdminJobSeekerList: {
    spec: JobSeekerAdminSpec
  }
}
