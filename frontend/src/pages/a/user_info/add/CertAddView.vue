<script setup lang="ts">
import type { RefId } from '@compose/types'
import { z } from 'zod'
import { CertContentTyping_OPTIONS, CertPointTyping_OPTIONS, CertTyping_OPTIONS } from '@/api/_Enums'
import { useSnackbar } from '@/components'

interface Props {
  userInfoId?: RefId
  userAccountId?: RefId
  files?: (File | null)[]
}

interface Emits {
  'update:userInfoId': [value: RefId]
  'update:userAccountId': [value: RefId]
  'update:files': [value: (File | null)[]]
}

const props = withDefaults(defineProps<Props>(), {
  userInfoId: void 0,
  userAccountId: void 0,
  files: () => [] as (File | null)[],
})
const emit = defineEmits<Emits>()

const _userInfoId = useVModel(props, 'userInfoId', emit, { passive: true })
const _userAccountId = useVModel(props, 'userAccountId', emit, { passive: true })
const _files = useVModel(props, 'files', emit, { passive: true })

const certs = ['ID_CARD2', 'DISABILITY_CARD3', 'HOUSEHOLD_CARD', 'TITLE_IMAGE', 'BANK_CARD'] as const
const certOptions = CertTyping_OPTIONS.filter((it) => {
  return certs.includes(it.v as typeof certs[number])
})
const points = ['HEADS', 'TAILS', 'DOUBLE'] as const
const pointOptions = CertPointTyping_OPTIONS.filter((it) => {
  return points.includes(it.v as typeof points[number])
})
const contents = ['SCANNED_IMAGE', 'PROCESSED_IMAGE'] as const
const cotentOptinos = CertContentTyping_OPTIONS.filter((it) => {
  return contents.includes(it.v as typeof contents[number])
})

// 表单验证协议
const schema = z.object({
  userInfoId: z.string().id().nullable().default(_userInfoId.value ?? null),
  userAccountId: z.string().id().nullable().default(_userAccountId.value ?? null),
  files: z.array(z.instanceof(File).nullable()).min(1).max(10).default(() => {
    return _files.value ?? []
  }),
  certs: z.array(z.object({
    doType: z.enum(certs),
    poType: z.enum(points),
    coType: z.enum(contents),
  })).min(1).max(10),
}).refine(
  ({ files, certs }) => files.length === certs.length,
  '证书文件数量必须与证书信息数量一致',
).refine(
  ({ userInfoId, userAccountId }) => userInfoId != null || userAccountId != null,
  '用户信息或账号不能同时为空',
).refine(
  ({ files }) => files.every((file) => file != null),
  '所有证件都必须上传文件',
)
type Schema = z.infer<typeof schema>
const submitData = ref<Schema>({
  userInfoId: null,
  userAccountId: null,
  files: [],
  certs: [],
})

// 计算预览 URL
const previewUrls = computed(() => {
  const files = Array.isArray(submitData.value.files) ? submitData.value.files : []
  return files.map((file) => {
    if (!file) {
      return null
    }
    if (file.type.startsWith('image/')) {
      return URL.createObjectURL(file)
    }
    return null
  })
})

// 在组件卸载时清理 URL
onBeforeUnmount(() => {
  previewUrls.value.forEach((url) => {
    if (url) {
      URL.revokeObjectURL(url)
    }
  })
})

watch(_files, (files) => {
  const currentFiles = Array.isArray(files) ? files : []
  submitData.value = {
    userInfoId: _userInfoId.value ?? null,
    userAccountId: _userAccountId.value ?? null,
    files: currentFiles,
    certs: Array.from({ length: currentFiles.length }, () => ({
      doType: void 0 as unknown as 'ID_CARD2',
      poType: void 0 as unknown as 'HEADS',
      coType: void 0 as unknown as 'SCANNED_IMAGE',
    })),
  }
}, { immediate: true })
// 添加证件
function addCert() {
  const currentFiles = Array.isArray(submitData.value.files) ? submitData.value.files : []
  submitData.value = {
    ...submitData.value,
    certs: [
      ...(submitData.value.certs || []),
      {
        doType: 'ID_CARD2',
        poType: 'HEADS',
        coType: 'SCANNED_IMAGE',
      },
    ],
    files: [...currentFiles, null],
  }
}

// 删除证件
function removeCert(index: number) {
  const currentFiles = Array.isArray(submitData.value.files) ? submitData.value.files : []
  const currentCerts = Array.isArray(submitData.value.certs) ? submitData.value.certs : []

  submitData.value = {
    ...submitData.value,
    certs: currentCerts.filter((_, i) => i !== index),
    files: currentFiles.filter((_, i) => i !== index),
  }
}

// 更新证件文件
function updateCertFile(index: number, file: File | null) {
  const currentFiles = Array.isArray(submitData.value.files) ? submitData.value.files : []

  submitData.value = {
    ...submitData.value,
    files: currentFiles.map((item, i) => i === index ? file : item),
  }
}

async function onSubmit(body: Schema) {
  // 确保所有文件都不为 null
  const validBody = {
    ...body,
    files: body.files.filter((file): file is File => file != null),
  }
  await api.certV2Api.postCertsAsAdmin({ body: validBody })
}
const snackbar = useSnackbar()

// 添加警告状态跟踪
const showWarning = ref(true)

function onSubmitError(error: unknown) {
  console.error(error)
  if (error instanceof Error) {
    snackbar.error(error.message)
  }
  if (typeof error !== 'object' || error === null) {
    return
  }
  if (!('errors' in error)) {
    return
  }
  if (typeof error.errors !== 'object' || error.errors === null) {
    return
  }

  if (error.errors) {
    Object.entries(error.errors).forEach(([, value]) => {
      snackbar.error(value)
    })
  }
}
</script>

<template>
<YForm v-model="submitData" :schema="schema" @submit="onSubmit" @error="onSubmitError">
  <VContainer>
    <!-- 添加警告提示 -->
    <VAlert
      v-if="showWarning && !submitData.userInfoId && !submitData.userAccountId" type="warning" title="缺少用户信息"
      text="请先设置用户信息或账号ID，否则无法提交证件信息" closable class="mb-4" @click:close="showWarning = false"
    >
      <template #prepend>
        <VIcon icon="i-mdi:alert-circle-outline" />
      </template>
    </VAlert>

    <VRow>
      <!-- 空状态提示 -->
      <VCol v-if="!submitData?.certs?.length" cols="12">
        <VCard class="empty-state-card">
          <VCardText class="d-flex align-center flex-column justify-center py-12">
            <VIcon icon="i-mdi:card-account-details-outline" size="80" color="primary" class="mb-4" />
            <div class="text-h5 mb-2">
              暂无证件
            </div>
            <div class="text-medium-emphasis text-body-1 mb-6">
              请点击下方按钮添加证件
            </div>
            <VBtn color="primary" prependIcon="i-mdi:plus" variant="elevated" @click="addCert">
              添加第一张证件
            </VBtn>
          </VCardText>
        </VCard>
      </VCol>

      <!-- 证件列表 -->
      <VCol v-for="(cert, index) in submitData.certs" :key="index" cols="12" sm="12" md="6" xl="4" class="pa-2">
        <VCard>
          <VCardTitle class="d-flex align-center">
            <span>证件 #{{ index + 1 }}</span>
            <VSpacer />
            <VBtn color="error" icon="i-mdi:delete" variant="text" @click="removeCert(index)" />
          </VCardTitle>

          <VCardText>
            <VRow>
              <!-- 图片预览 -->
              <VCol v-if="previewUrls[index]" cols="12">
                <div class="preview-container">
                  <VImg :src="previewUrls[index]" :aspectRatio="16 / 9" cover class="rounded-lg" />
                </div>
              </VCol>

              <!-- 文件上传 -->
              <VCol cols="12">
                <YField :name="`files[${index}]`" :label="`证件 #${index + 1}`">
                  <VFileInput
                    accept="image/*" hideDetails="auto" variant="outlined" density="comfortable"
                    prependIcon="i-mdi:file-upload" :multiple="false"
                    @update:modelValue="(files) => updateCertFile(index, Array.isArray(files) ? files[0] || null : files)"
                  />
                </YField>
              </VCol>

              <!-- 证件类型选择 -->
              <VCol cols="12">
                <YField :name="`certs[${index}].doType`" label="证件类型">
                  <VSelect
                    v-model="cert.doType" clearable itemTitle="k" itemValue="v" :items="certOptions"
                    hideDetails="auto" variant="outlined" density="comfortable"
                  />
                </YField>
              </VCol>

              <!-- 证件印面 -->
              <VCol cols="12">
                <YField :name="`certs[${index}].poType`" label="证件印面">
                  <VSelect
                    v-model="cert.poType" clearable itemTitle="k" itemValue="v" :items="pointOptions"
                    hideDetails="auto" variant="outlined" density="comfortable"
                  />
                </YField>
              </VCol>

              <!-- 证件内容 -->
              <VCol cols="12">
                <YField :name="`certs[${index}].coType`" label="内容类别">
                  <VSelect
                    v-model="cert.coType" clearable itemTitle="k" itemValue="v" :items="cotentOptinos"
                    hideDetails="auto" variant="outlined" density="comfortable"
                  />
                </YField>
              </VCol>
            </VRow>
          </VCardText>
        </VCard>
      </VCol>
    </VRow>

    <!-- 添加证件按钮 -->
    <VRow class="mt-4">
      <VCol cols="12" class="text-center">
        <div class="d-flex flex-column justify-sm-end flex-sm-row gap-4">
          <VBtn
            v-if="submitData?.certs?.length" color="primary" prependIcon="i-mdi:plus" variant="elevated"
            :block="$vuetify.display.xs" @click="addCert"
          >
            添加证件
          </VBtn>
          <VBtn
            color="success" prependIcon="i-mdi:check" variant="elevated" :block="$vuetify.display.xs"
            type="submit" :disabled="!submitData?.files?.length"
          >
            提交
          </VBtn>
        </div>
      </VCol>
    </VRow>
  </VContainer>
</YForm>
</template>

<style scoped lang="scss">
.v-card {
  border: thin solid rgb(var(--v-theme-outline));
}

.v-container {
  max-width: 1600px;
}

.preview-container {
  position: relative;
  width: 100%;
  max-height: 200px;
  overflow: hidden;
  border-radius: 8px;
  border: thin solid rgb(var(--v-theme-outline));
}

.empty-state-card {
  min-height: 300px;
  border: 1px dashed rgb(var(--v-theme-primary));
  background: rgba(var(--v-theme-primary), 0.05);
}
</style>
