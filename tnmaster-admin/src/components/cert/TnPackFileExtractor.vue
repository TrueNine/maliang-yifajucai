<script setup lang="ts">
import type { Maybe } from '@compose/types'
import { extractPdfImages } from '@compose/external/pdfjs-dist'
import { findMediaTypeByFileName } from '@compose/req'
import { maybeArray } from '@compose/shared'
import JSZip from 'jszip'
import { Archive } from 'libarchive.js'
import workerBundleUrl from 'libarchive.js/dist/worker-bundle.js?url'

interface Props {
  modelValue?: File[]
  encode?: string
  accept?: Maybe<string>
}
interface Emits {
  'update:modelValue': [files: File[]]
}
const props = withDefaults(defineProps<Props>(), {
  modelValue: () => [],
  encode: 'UTF-8',
  accept: () => ['.jpg', '.png', '.jpeg', '.webp'],
})
const emits = defineEmits<Emits>()

const selectedFiles = useVModel(props, 'modelValue', emits, { passive: true })
const fileEncoding = useVModel(props, 'encode', emits, { passive: true })
const acceptedFileTypes = useVModel(props, 'accept', emits, { passive: true })

Archive.init({
  workerUrl: workerBundleUrl,
})

const fileInput = useTemplateRef('fileInput')

function triggerFileInput() {
  fileInput.value?.click()
}

async function handleFileInput(event: Event) {
  const files = (event.target as HTMLInputElement).files
  if (files === null || files.length === 0) {
    return
  }
  selectedFiles.value = await unpack(Array.from(files))
}

async function unpackRarArchive(file: File): Promise<File[]> {
  const archiveWorker = await Archive.open(file)
  const extractedFiles = await archiveWorker.extractFiles()
  const processedFiles: File[] = []

  async function traverseFiles(node: object) {
    const promises = Object.entries(node).map(async ([, value]) => {
      if (value instanceof File) {
        processedFiles.push(...(await unpack(new File([value], value.name, { type: findMediaTypeByFileName(value.name) }))))
      } else if (value instanceof Object) {
        await traverseFiles(value)
      }
    })
    await Promise.all(promises)
  }

  if (extractedFiles) {
    await traverseFiles(extractedFiles)
  }
  return processedFiles
}

async function unpackPdfFile(file: File): Promise<File[]> {
  const imageDataUrls = await extractPdfImages(file)
  return imageDataUrls.map((dataUrl, index) => {
    const [metaData, base64Data] = dataUrl.split(',')
    const mimeType = /:(.*?);/.exec(metaData)?.[1]
    const binaryString = atob(base64Data)
    let length = binaryString.length
    const uint8Array = new Uint8Array(length)
    while (length--) {
      uint8Array[length] = binaryString.charCodeAt(length)
    }
    const imageBlob = new Blob([uint8Array], { type: mimeType })
    return new File([imageBlob], `img_${index.toString()}.png`, { type: mimeType })
  })
}

async function unpackZipFile(zipFile: File): Promise<File[]> {
  const baseFileName = `${zipFile.name.replace('.zip', '')}_`
  const textDecoder = new TextDecoder(fileEncoding.value)
  const zipEntry = await JSZip.loadAsync(zipFile, {
    decodeFileName: (bytes) => textDecoder.decode(bytes as Uint8Array),
  })
  const zipContents = zipEntry.files
  const extractedFiles: File[] = []
  for (const filePath in zipContents) {
    const zipItem = zipContents[filePath]
    if (zipItem.dir) {
      continue
    }
    const fileBlob = await zipItem.async('blob')
    const fileName = zipItem.name.replaceAll(/[/:*?"<>|]/g, '_').replace(baseFileName, '')
    const fileType = findMediaTypeByFileName(zipItem.name)
    const extractedFile = new File([fileBlob], fileName, { type: fileType })
    const unpackedFiles = await unpack(extractedFile)
    extractedFiles.push(...unpackedFiles)
  }
  return extractedFiles
}

const fileTypeHandlers = new Map<string, (file: File) => Promise<File[]>>([
  ['zip', unpackZipFile],
  ['pdf', unpackPdfFile],
  ['rar', unpackRarArchive],
  ['7z', unpackRarArchive],
  ['gz', unpackRarArchive],
])

async function unpack(file: Maybe<File>): Promise<File[]> {
  const files = maybeArray(file)
  if (!files.length) {
    return []
  }

  const processFile = async (file: File): Promise<File[]> => {
    const fileExtension = file.name.toLowerCase().split('.').at(-1) ?? ''
    // 如果是接受的图片格式
    if (acceptedFileTypes.value.includes(`.${fileExtension}`)) {
      return [file]
    }
    // 获取对应的处理器
    const handler = fileTypeHandlers.get(fileExtension)
    return handler ? handler(file) : []
  }

  const results = await Promise.all(files.map(processFile))
  return results.flat()
}

async function handlePasteEvent(event: ClipboardEvent) {
  const pastedFiles = event.clipboardData?.files
  if (!pastedFiles) {
    return
  }

  const fileArray = Array.from(pastedFiles)
  if (!selectedFiles.value.length) {
    selectedFiles.value = []
  }
  selectedFiles.value = await unpack(fileArray)
}

// 接受的解压文件类型
const supportedArchiveTypes = ref(['.zip', '.pdf', '.rar', '.7z', '.gz'])
const acceptedFileTypesString = computed(() => supportedArchiveTypes.value.join(','))
</script>

<template>
<VTextField
  :accept="acceptedFileTypesString" :label="`支持粘贴文件或点击左侧图标选择文件 (${acceptedFileTypesString})`"
  placeholder="直接粘贴文件到这里" readonly @click:prepend="triggerFileInput"
  @paste="(event: ClipboardEvent) => handlePasteEvent(event)"
>
  <template #append-inner>
    <YIco class="i-mdi:file-upload cursor-pointer text-6" @click="triggerFileInput" />
  </template>
</VTextField>
<input ref="fileInput" type="file" :accept="acceptedFileTypesString" class="d-none" @change="handleFileInput" />
</template>
