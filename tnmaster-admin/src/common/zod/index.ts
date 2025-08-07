import { custom } from 'zod'

export function file() {
  return custom<File>((value) => {
    if (!(value instanceof File)) {
      return '文件输入类型错误'
    }
    if (value.size <= 0) {
      return '文件大小不能为空'
    }
    if (!value.type) {
      return '文件类型不能为空'
    }
    if (!value.name) {
      return '文件名称不能为空'
    }
    return true
  })
}
