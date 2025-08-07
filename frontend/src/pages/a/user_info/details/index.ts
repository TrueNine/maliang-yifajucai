import type { Ref } from 'vue'
import type { api, ResponseOf } from '@/api'
import { downloadBlob } from '@compose/external/browser/document'

import JSZip from 'jszip'
import { getCertContentTypingDesc, getCertPointTypingDesc, getCertTypingDesc } from '@/api'

type CertView = ResponseOf<typeof api.certV2Api.getUserInfoWatermarkCerts>[number]

export function getDocDesc(a: CertView) {
  if (!a.coType || !a.poType || !a.doType) {
    return 'none'
  }
  const d = getCertTypingDesc(a.doType)
  const c = getCertContentTypingDesc(a.coType)
  const p = getCertPointTypingDesc(a.poType)
  return d + c + p
}

export interface DownloadZipOptions {
  selectedCerts: CertView[]
  zipFName: string
  copyText: string
  onProgress?: (text: string) => void
}

function getFileExt(fileName?: string) {
  if (fileName == null) {
    return '.txt'
  }
  const ext = fileName.split('.').pop()
  if (ext == null) {
    return '.txt'
  }
  return `.${ext}`
}

export async function downloadZip(options: DownloadZipOptions) {
  const { selectedCerts, zipFName, copyText, onProgress } = options

  if (!selectedCerts.length) {
    return
  }
  const certs = selectedCerts
  const zip = new JSZip()
  onProgress?.('开始下载')

  // 并发下载所有文件
  const downloadPromises = certs.map(async (e, i) => {
    let fName = ''
    try {
      fName = getDocDesc(e) + getFileExt(e.waterMarkerAttachment?.metaName)
      const isExists = zip.filter((e) => e === fName).length > 0
      if (isExists) {
        fName = `${getDocDesc(e)}-${(i + 1).toString()}.${getFileExt(e.waterMarkerAttachment?.metaName)}`
      }
      onProgress?.(`${(i + 1).toString()}/${certs.length.toString()} 正在下载 ${fName} ……`)

      if ((e.waterMarkerAttachment?.fullAccessUrl) == null) {
        return null
      }

      const response = await fetch(e.waterMarkerAttachment.fullAccessUrl, {
        mode: 'cors',
        method: 'GET',
      })

      if (!response.ok) {
        throw new Error(`下载失败: ${response.status} ${response.statusText}`)
      }

      const fBlob = await response.blob()

      onProgress?.(`${certs.length.toString()}/${(i + 1).toString()} 下载完成 ${fName} ……`)
      return { fName, fBlob }
    } catch (error) {
      throw new Error(`下载文件 ${fName} 失败: ${error instanceof Error ? error.message : String(error)}`)
    }
  })

  const results = await Promise.all(downloadPromises)

  // 添加下载的文件到zip
  results.forEach((result) => {
    if (result) {
      zip.file(result.fName, result.fBlob)
    }
  })

  if (copyText) {
    zip.file('个人信息.txt', new Blob([copyText], { type: 'text/plain' }))
  }

  onProgress?.('正在压缩 ……')
  const b = await zip.generateAsync({ type: 'blob' })
  onProgress?.('压缩完成 ……')
  downloadBlob(b, `${zipFName}.zip`)
}

export interface HandleDownloadZipOptions {
  selectedCerts: CertView[]
  zipFName: string
  copyText: string
  lockDialog: Ref<boolean>
  showDialog: Ref<boolean>
  currentText: Ref<string>
}

export async function handleDownloadZip(options: HandleDownloadZipOptions) {
  const { selectedCerts, zipFName, copyText, lockDialog, showDialog, currentText } = options
  if (!selectedCerts.length) {
    showDialog.value = false
    return
  }
  lockDialog.value = true
  try {
    await downloadZip({
      selectedCerts,
      zipFName,
      copyText,
      onProgress: (text) => {
        currentText.value = text
      },
    })
  } catch (e: unknown) {
    console.error(e)
    throw e
  } finally {
    lockDialog.value = false
    showDialog.value = false
  }
}
