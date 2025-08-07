import { Regexes } from '@compose/shared'
import { z } from 'zod'

interface ErrorMessageMap {
  [key: string]: string
}

const ERROR_MESSAGES: ErrorMessageMap = {
  unrecognized_keys: '出现未知字段',
  invalid_type: '类型错误',
  invalid_type_required: '此处必须填写',
  invalid_literal: '值不符合要求',
  invalid_union: '值不符合任一类型要求',
  invalid_union_discriminator: '类型标识符无效',
  invalid_enum_value: '枚举值无效',
  invalid_arguments: '参数无效',
  invalid_return_type: '返回值类型无效',
  invalid_date: '日期格式无效',
  invalid_string: '字符串格式无效',
  too_small: '值太小',
  too_big: '值太大',
  invalid_intersection_types: '类型交集无效',
  not_multiple_of: '不是指定倍数的值',
  not_finite: '不是有限数值',
  custom: '自定义错误',
  unknown: '未知错误',
}

z.setErrorMap((message, ctx) => {
  // 处理必填字段错误
  if (message.code === 'invalid_type' && ctx.defaultError === 'Required') {
    return { message: ERROR_MESSAGES.invalid_type_required }
  }

  // 处理自定义错误
  if (message.code === 'custom') {
    return { message: message.message ?? ERROR_MESSAGES.custom }
  }

  // 处理其他已知错误
  const errorMessage = ERROR_MESSAGES[message.code]
  if (errorMessage) {
    return { message: errorMessage }
  }

  // 处理未知错误
  console.error('未知错误类型:', { message, ctx })
  return { message: ERROR_MESSAGES.unknown }
})

z.ZodString.prototype.idCard2Code = function () {
  return this
    .length(18, '身份证号长度不正确')
    .regex(Regexes.CHINA_ID_CARD, '身份证号格式不正确')
    .nonempty('身份证号不能为空')
    .transform((e) => e.toUpperCase())
}
z.ZodString.prototype.disCardCode3 = function () {
  return this
    .min(20, '残疾证号长度不正确')
    .max(22, '残疾证号长度不正确')
    .regex(Regexes.CHINA_DIS_CARD, '残疾证号格式不正确')
    .nonempty('残疾证号不能为空')
    .transform((e) => e.toUpperCase())
}

z.ZodString.prototype.phone = function () {
  return this
    .length(11, '电话号码长度不正确')
    .regex(Regexes.CHINA_PHONE, '电话号码长度不正确')
    .nonempty('电话号码不能为空')
}

z.ZodString.prototype.id = function () {
  return this
    .min(1, 'ID不能为空')
    .regex(/^\d+$/, 'ID必须为纯数字')
}

z.ZodArray.prototype.fileNotRepeat = function () {
  return this.refine((value) => {
    if (value.length === 0 || value.length === 1) {
      return true
    }
    const firstFile = value[0] as { name: string, size: number }
    for (let i = 1; i < value.length; i++) {
      const file = value[i] as { name: string, size: number }
      if (file.name === firstFile.name
        && file.size === firstFile.size) {
        return false
      }
    }
    return true
  }, '文件内容重复')
}

export function registerZod() {}
