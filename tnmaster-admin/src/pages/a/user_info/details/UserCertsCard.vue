<script setup lang="ts">
import type { RefId } from '@compose/types'
import type { ResponseOf } from '@/api'
import { formatDatetime } from '@compose/external/dayjs'
import { computed } from 'vue'
import { api } from '@/api'
import { useSnackbar } from '@/components'
import { getDocDesc, handleDownloadZip } from '../details'

interface Props {
  certs: ResponseOf<typeof api.certV2Api.getUserInfoWatermarkCerts>
  userName?: string
  copyText?: string
  loading?: boolean
  userInfoId?: RefId
  userAccountId?: RefId
}

interface Emits {
  selectionChange: [certs: ResponseOf<typeof api.certV2Api.getUserInfoWatermarkCerts>]
}

const { certs, userName, copyText, loading = false, userInfoId, userAccountId } = defineProps<Props>()
const emit = defineEmits<Emits>()

const selectedCerts = ref<ResponseOf<typeof api.certV2Api.getUserInfoWatermarkCerts>>([])
const showDialog = ref(false)
const zipFName = ref('')
const lockDialog = ref<boolean>(false)
const currentText = ref<string>('')
const snackbar = useSnackbar()

const hasSelectedItems = computed(() => selectedCerts.value.length > 0)

watch(showDialog, (v) => {
  if (!userName || !v) {
    return
  }
  zipFName.value = userName
})

function handleSelectionChange(selected: ResponseOf<typeof api.certV2Api.getUserInfoWatermarkCerts>) {
  selectedCerts.value = selected
  emit('selectionChange', selected)
}

async function handleDownloadZipLocal() {
  if (!selectedCerts.value.length) {
    return
  }
  try {
    await snackbar.message(
      handleDownloadZip({
        selectedCerts: selectedCerts.value,
        zipFName: zipFName.value,
        copyText: copyText || '',
        lockDialog,
        showDialog,
        currentText,
      }),
    )
  } catch (e: unknown) {
    console.error(e)
    snackbar.error('下载失败')
  }
}

async function patchFixCertsMarker() {
  if (userInfoId !== void 0 || userAccountId !== void 0) {
    await api.certV2Api.patchCertTypingMarkersAsAdmin({
      userAccountId: userAccountId as unknown as number,
      userInfoId: userInfoId as unknown as number,
    })
    snackbar.success('修复成功')
  }
}
</script>

<template>
<VCard class="bg-background rounded-lg p-4" flat>
  <!-- 证件 view -->
  <VDialog
    v-model="showDialog"
    :persistent="lockDialog"
    maxWidth="500"
    transition="dialog-bottom-transition"
  >
    <VCard class="bg-background">
      <VCardTitle class="text-h6 border-border border-b p-4">
        准备打包
        <VChip class="ml-2" color="primary" size="small">
          {{ certs?.length }} 个文件
        </VChip>
      </VCardTitle>

      <VCardText class="py-6">
        <div v-if="!lockDialog">
          <VTextField
            v-model="zipFName"
            label="包名"
            variant="outlined"
            density="compact"
            :hideDetails="true"
            class="mb-2"
          />
        </div>
        <div v-if="lockDialog" class="py-4">
          <div class="text-body-2 mb-2">
            {{ currentText }}
          </div>
          <VProgressLinear
            color="primary"
            indeterminate
            rounded
            :height="6"
          />
        </div>
      </VCardText>

      <VCardActions class="border-border border-t pa-4">
        <VSpacer />
        <VBtn
          :disabled="lockDialog"
          color="primary"
          variant="elevated"
          @click="handleDownloadZipLocal"
        >
          <YIco class="i-mdi:download mr-2" />
          下载并复制信息
        </VBtn>
      </VCardActions>
    </VCard>
  </VDialog>

  <VDivider class="border-border mb-4" />

  <!-- 加载状态 -->
  <VSkeletonLoader
    v-if="loading"
    type="table"
    class="mb-4"
  />

  <!-- 空状态 -->
  <VCard
    v-else-if="!certs?.length"
    class="bg-card flex items-center justify-center p-8"
    flat
  >
    <div class="p-12 text-center space-y-4">
      <YIco class="i-mdi:file-document-outline mb-4 text-16 text-disabled" />
      <div class="text-body-1 text-disabled">
        暂无证件信息
      </div>
      <div>
        <VBtn
          color="success"
          :disabled="userInfoId === void 0 && userAccountId === void 0"
          @click="patchFixCertsMarker"
        >
          <YIco class="i-mdi:wrench mr-2" />
          修复证件
        </VBtn>
      </div>
    </div>
  </VCard>

  <!-- 表格内容 -->
  <template v-else>
    <div class="flex flex-col gap-4">
      <ElTable
        class="border-border overflow-hidden rounded-lg"
        stripe
        :border="true"
        :data="certs"
        @selectionChange="handleSelectionChange"
      >
        <ElTableColumn type="selection" width="55" />

        <ElTableColumn label="证件类型" width="350" prop="type">
          <template #default="{ row }">
            <div class="flex items-center">
              <YIco class="i-mdi:file-document mr-2 text-5 text-primary" />
              {{ getDocDesc(row) }}
            </div>
          </template>
        </ElTableColumn>

        <YPreAuthorize :hasAnyPermissions="[`ROOT`, `ADMIN`]">
          <ElTableColumn prop="waterMarkerAttachment.fullAccessUrl" label="文件">
            <template #default="{ row }">
              <VHover v-slot="{ isHovering, props }">
                <VCard
                  v-bind="props"
                  flat
                  :elevation="isHovering ? 4 : 0"
                  class="bg-card duration-300 transition-ease-in-out"
                >
                  <VImg
                    :src="row.waterMarkerAttachment?.fullAccessUrl"
                    height="84"
                    cover
                    class="rounded"
                  />
                </VCard>
              </VHover>
            </template>
          </ElTableColumn>
        </YPreAuthorize>

        <YPreAuthorize :hasAnyPermissions="[`ROOT`]">
          <ElTableColumn prop="uploadDatetime" width="250" label="上传时间">
            <template #default="{ row }">
              <div class="flex items-center">
                <YIco class="i-mdi:clock-outline mr-1 text-4.5 text-primary" />
                {{ formatDatetime(row.createDatetime) }}
              </div>
            </template>
          </ElTableColumn>
        </YPreAuthorize>
      </ElTable>

      <YPreAuthorize :hasAnyPermissions="[`ROOT`, `ADMIN`]">
        <div class="flex justify-end gap-2 p-2">
          <VBtn
            :disabled="!hasSelectedItems"
            color="primary"
            variant="elevated"
            class="px-8"
            @click="showDialog = true"
          >
            <div class="flex items-center">
              <YIco class="i-mdi:package-variant mr-2" />
              打包并下载
            </div>
          </VBtn>
        </div>
      </YPreAuthorize>
    </div>
  </template>
</VCard>
</template>

<style lang="scss">
.el-table {
  @apply border-border bg-background;

  --el-table-border-color: rgb(var(--color-border) / 1);
  --el-table-header-bg-color: rgb(var(--color-muted) / 0.5);
  --el-table-bg-color: rgb(var(--color-background) / 1);
  --el-table-tr-bg-color: rgb(var(--color-background) / 1);
  --el-table-row-hover-bg-color: rgb(var(--color-muted) / 0.5);

  color: rgb(var(--color-text) / 1);

  th {
    @apply font-600 border-b border-border text-text/80;
    background-color: var(--el-table-header-bg-color);
  }

  td {
    @apply border-b border-border;
  }

  .el-table__body tr.el-table__row--striped td {
    background-color: rgb(var(--color-muted) / 0.3);
  }
}
</style>
