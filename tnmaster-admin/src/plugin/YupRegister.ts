import { Regexes } from '@compose/shared'
import { addMethod, string } from 'yup'

addMethod(string, 'idCard2', function (message?: string) {
  return this.min(18, message ?? '身份证长度不正确')
    .max(18, message ?? '身份证长度不正确')
    .matches(Regexes.CHINA_ID_CARD, { excludeEmptyString: true, message: '身份证格式不正确' })
})
addMethod(string, 'disCard2', function (message?: string) {
  return this.min(20, message ?? '残疾证号最少20位')
    .max(22, message ?? '残疾证号最多22位')
    .test('disCard2', message ?? '残疾证号格式不正确', (value) => {
      if (value == null) {
        return true
      }
      if (value.length === 19 || value.length === 21) {
        return false
      }
    })
})
addMethod(string, 'phone', function (message?: string) {
  return this.matches(Regexes.CHINA_PHONE, { excludeEmptyString: true, message: (message != null) || '手机号格式不正确' })
})

export function registerYup() {}
