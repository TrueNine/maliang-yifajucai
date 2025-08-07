import { addMethod, array, mixed } from 'yup'

export function file() {
  return mixed<File>().test('file-type', '文件类型错误', (value, context) => {
    if (!value) {
      return true
    }
    if (!(value instanceof File)) {
      return context.createError({
        message: '文件输入类型错误',
      })
    }
    if (value.size <= 0) {
      return context.createError({
        message: '文件大小不能为空',
      })
    }
    if (!value.type) {
      return context.createError({
        message: '文件类型不能为空',
      })
    }
    if (!value.name) {
      return context.createError({
        message: '文件名称不能为空',
      })
    }
    return true
  })
}
addMethod(array, 'fileNotRepeat', function (message?: string) {
  return this.test('fileNotRepeat', message ?? '文件重复', (values, context) => {
    if (!values || values.length === 0) {
      return true
    }
    const fileList = values as File[]
    const firstFile = fileList[0]
    for (let i = 1; i < fileList.length; i++) {
      const file = fileList[i]
      if (file.name === firstFile.name) {
        return context.createError({
          message: '不能上传两个相同文件',
        })
      }
    }
    return true
  })
})
