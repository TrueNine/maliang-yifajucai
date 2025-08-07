import type { AuditTyping, CertContentTyping, CertPointTyping, CertTyping, DisTyping, GenderTyping, RelationItemTyping, RelationTyping } from '@/__generated/model/enums'
import { AuditTyping_CONSTANTS, CertContentTyping_CONSTANTS, CertPointTyping_CONSTANTS, CertTyping_CONSTANTS, DisTyping_CONSTANTS, RelationItemTyping_CONSTANTS, RelationTyping_CONSTANTS } from '@/__generated/model/enums'

export function getCertPointTypingDesc(
  type?: CertPointTyping | null,
) {
  if (type === void 0 || type === null) {
    return '未知类型'
  }
  switch (type) {
    case 'HEADS': return '正面'
    case 'TAILS': return '反面'
    case 'DOUBLE': return '双面'
    case 'NONE': return '无'
    case 'ALL': return '全部'
    case 'ALL_CONTENT': return '全部内容'
    case 'INTACT': return '完整'
    default: return '未知类型'
  }
}
export const CertPointTyping_OPTIONS = CertPointTyping_CONSTANTS.map((it) => ({
  k: getCertPointTypingDesc(it),
  v: it,
}))
export function getCertContentTypingDesc(
  type?: CertContentTyping | null,
) {
  if (type === void 0 || type === null) {
    return '未知类型'
  }
  switch (type) {
    case 'SCANNED_IMAGE': return '扫描件'
    case 'COPYFILE_IMAGE': return '复印件'
    case 'IMAGE': return '图片'
    case 'PROCESSED_IMAGE': return '已处理图片'
    case 'VIDEO': return '视频'
    case 'RECORDING': return '录音'
    case 'REMAKE_IMAGE': return '翻拍图片'
    case 'PROCESSED_SCANNED_IMAGE': return '已处理扫描件'
    case 'PROCESSED_VIDEO': return '已处理视频'
    case 'PROCESSED_AUDIO': return '已处理音频'
    case 'SCREEN_SHOT' :return '屏幕截图'
    case 'NONE':
    default: return '未知类型'
  }
}
export function getDisTypingDesc(type?: DisTyping | null) {
  if (type == null) {
    return '未知类型'
  }
  switch (type) {
    case 'EYE': return '视力'
    case 'EAR': return '听力'
    case 'MOUTH': return '言语'
    case 'BODY': return '肢体'
    case 'IQ': return '智力'
    case 'NERVE' :return '精神'
    case 'MULTIPLE': return '多重'
    default: return '未知类型'
  }
}
export const DisTyping_OPTIONS = DisTyping_CONSTANTS.map((it) => ({
  k: getDisTypingDesc(it),
  v: it,
}))

export function getCertTypingDesc(
  type?: CertTyping | null,
) {
  if (type == null) {
    return '未知类型'
  }
  switch (type) {
    case 'ID_CARD':
    case 'ID_CARD2': return '身份证'
    case 'DISABILITY_CARD':
    case 'DISABILITY_CARD2':
    case 'DISABILITY_CARD3': return '残疾证'
    case 'CONTRACT': return '合同'
    case 'BIZ_LICENSE': return '营业执照'
    case 'PERSONAL_INCOME_TAX_VIDEO': return '个人所得税视频'
    case 'HOUSEHOLD_CARD': return '户口'
    case 'TITLE_IMAGE': return '寸照'
    case 'BANK_CARD': return '银行卡'
    case 'NONE':
    default: return '未知类型'
  }
}
export const CertTyping_OPTIONS = CertTyping_CONSTANTS.map((it) => ({
  k: getCertTypingDesc(it),
  v: it,
}))
export const CertContentTyping_OPTIONS = CertContentTyping_CONSTANTS.map((it) => ({
  k: getCertContentTypingDesc(it),
  v: it,
}))
export function getGenderTypingDesc(type?: GenderTyping | null) {
  if (type == null) {
    return '未知'
  }
  switch (type) {
    case 'MAN': return '男'
    case 'WOMAN': return '女'
    case 'UNKNOWN': return '未知'
    default: return '未知'
  }
}
export function getAuditTypingDesc(type?: AuditTyping | null) {
  if (type == null) {
    return '无'
  }
  switch (type) {
    case 'NONE': return '无'
    case 'ASSIGNED': return '已分配'
    case 'PASS': return '审核通过'
    case 'FAIL': return '审核失败'
    case 'CANCEL': return '审核取消'
    case 'EXPIRED': return '审核过期'
    case 'REJECT': return '审核拒绝'
    case 'SHADOW_BAN': return '审核拉黑'
  }
}
export const AuditTyping_OPTIONS = AuditTyping_CONSTANTS.map((it) => ({
  k: getAuditTypingDesc(it),
  v: it,
}))
export function getRelationItemTypingDesc(type?: RelationItemTyping | null) {
  if (type == null) {
    return '未知类型'
  }
  switch (type) {
    case 'NONE': return '无'
    case 'USER': return '用户'
    case 'CUSTOMER': return '客户'
    case 'ENTERPRISE': return '企业'
    case 'EMPLOYEE': return '员工'
    case 'OTHER': return '其他'
  }
}
export const RelationItemTyping_OPTIONS = RelationItemTyping_CONSTANTS.map((it) => ({
  k: getRelationItemTypingDesc(it),
  v: it,
}))
export function getRelationTypingDesc(type?: RelationTyping | null) {
  if (type == null) {
    return '未知类型'
  }
  switch (type) {
    case 'NONE': { return '无' }
    case 'OTHER': { return '其他' }
    case 'BENEFICIARIES': { return '受益人' }
    case 'PARTICIPATOR': { return '参与者' }
    case 'WITNESS': { return '见证人' }
  }
}
export const RelationTyping_OPTIONS = RelationTyping_CONSTANTS.map((it) => ({
  k: getRelationTypingDesc(it),
  v: it,
}))
