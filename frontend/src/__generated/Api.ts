import type { Executor } from './'
import {
  AccountApi,
  AclV2Api,
  AddressV2Api,
  AuditV2Api,
  AuthApi,
  BlackListV2Api,
  CertV2Api,
  CommonApi,
  JobSeekerV2Api,
  JobV2Api,
  SensitiveMockApi,
  ServerCacheConfigV2Api,
  TaxVideoV2Api,
  UserInfoV2Api,
  WechatPublicAccountApi,
} from './services/'

export class Api {
  readonly accountApi: AccountApi

  readonly aclV2Api: AclV2Api

  readonly addressV2Api: AddressV2Api

  readonly auditV2Api: AuditV2Api

  readonly authApi: AuthApi

  readonly blackListV2Api: BlackListV2Api

  readonly certV2Api: CertV2Api

  readonly commonApi: CommonApi

  readonly jobSeekerV2Api: JobSeekerV2Api

  readonly jobV2Api: JobV2Api

  readonly sensitiveMockApi: SensitiveMockApi

  readonly serverCacheConfigV2Api: ServerCacheConfigV2Api

  readonly taxVideoV2Api: TaxVideoV2Api

  readonly userInfoV2Api: UserInfoV2Api

  readonly wechatPublicAccountApi: WechatPublicAccountApi

  constructor(executor: Executor) {
    this.accountApi = new AccountApi(executor)
    this.aclV2Api = new AclV2Api(executor)
    this.addressV2Api = new AddressV2Api(executor)
    this.auditV2Api = new AuditV2Api(executor)
    this.authApi = new AuthApi(executor)
    this.blackListV2Api = new BlackListV2Api(executor)
    this.certV2Api = new CertV2Api(executor)
    this.commonApi = new CommonApi(executor)
    this.jobSeekerV2Api = new JobSeekerV2Api(executor)
    this.jobV2Api = new JobV2Api(executor)
    this.sensitiveMockApi = new SensitiveMockApi(executor)
    this.serverCacheConfigV2Api = new ServerCacheConfigV2Api(executor)
    this.taxVideoV2Api = new TaxVideoV2Api(executor)
    this.userInfoV2Api = new UserInfoV2Api(executor)
    this.wechatPublicAccountApi = new WechatPublicAccountApi(executor)
  }
}
