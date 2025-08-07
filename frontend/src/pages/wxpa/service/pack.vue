<script setup lang="ts">
import { downloadBlob } from '@compose/external/browser/document'
import JSZip from 'jszip'
import { useConfigStore } from '@/store'

const unpackFiles = ref<File[]>([])
const packName = ref<string>('')
const packedFile = ref<File | undefined>(void 0)

const cfg = useConfigStore()
cfg.bottomNavigationShow = false

async function pack() {
  const zip = new JSZip()
  unpackFiles.value.forEach((file) => {
    zip.file(file.name, file, { binary: true })
  })
  const blob = await zip.generateAsync({ type: 'blob' })
  const filename = `${packName.value}.zip`
  packedFile.value = new File([blob], filename, { type: 'application/zip' })
}

function downloadZip() {
  if (!packedFile.value) {
    return
  }
  downloadBlob(packedFile.value, packName.value)
}

watch(
  () => unpackFiles.value.length,
  (v) => {
    if (v) {
      return
    }
    packName.value = ''
    packedFile.value = void 0
  },
)
const orEdit = ref(true)
</script>

<template>
<div class="h-full flex flex-col justify-center p-4">
  <div>
    <VAlert color="error" closable>
      该功能需要在浏览器内进行使用，微信公众号不可用，请点击右上角，在浏览器内打开
    </VAlert>
  </div>

  <div class="w-full py-8">
    <VFileInput v-model="unpackFiles" multiple label="选择多个需要压缩的文件">
      <template #selection="{ fileNames }">
        <template v-for="fileName in fileNames" :key="fileName">
          <VChip class="me-2" color="primary" size="small" label>
            {{ fileName }}
          </VChip>
        </template>
      </template>
    </VFileInput>
  </div>

  <div class="w-full">
    <VBtn v-if="!packedFile" :disabled="!unpackFiles?.length" color="primary" :block="true" @click="pack">
      {{ !unpackFiles?.length ? '请选择需要压缩的文件' : '开始压缩' }}
    </VBtn>
  </div>

  <div class="py-8">
    <VCard v-if="packedFile">
      <VCardText>
        <div class="f-y-c flex-col">
          <YIco color="blue" class="i-mdi:zip-box text-48" />
          <div w-full>
            <VTextField v-if="orEdit" v-model="packName" label="压缩文件名" @blur="() => (orEdit = false)" />
            <div v-else class="f-c">
              <h2 @click="() => (orEdit = true)">
                {{ packName }}
                <YIco class="i-mdi:edit text-6 c-blue" />
              </h2>
            </div>
          </div>
        </div>
      </VCardText>
      <VCardActions>
        <VBtn :disabled="!packName" block color="primary" variant="elevated" @click="downloadZip">
          {{ !packName ? '请输入压缩文件名' : '下载压缩包' }}
        </VBtn>
      </VCardActions>
    </VCard>
  </div>
</div>
</template>
