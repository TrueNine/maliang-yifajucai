<script setup lang="ts">
import type { RefId } from '@compose/types'
import type { ResponseOf } from '@/__generated'
import type { api, CertTyping } from '@/api'
import { getCertTypingDesc } from '@/api'

interface Props {
  userAccountId?: RefId
  certPage?: ResponseOf<typeof api.certV2Api.getNotAuditCertsAsAdmin>
}

type _CertPageD = NonNullable<Props['certPage']>['d']
interface Emits {
  (e: 'select', value: _CertPageD): void
}

const props = withDefaults(defineProps<Props>(), {
  userAccountId: void 0,
})
const emit = defineEmits<Emits>()
const groupedCerts = computed<Record<string, _CertPageD>>(() => {
  if (!props.certPage || !props.certPage.d || !props.certPage.d.length) {
    return {}
  }
  const r = props.certPage.d
  return r.reduce(
    (acc, cur) => {
      if (!cur.groupCode) {
        return acc
      }
      if (acc[cur.groupCode]) {
        acc[cur.groupCode].push(cur)
      } else {
        acc[cur.groupCode] = [cur]
      }
      return acc
    },
    {} as Record<string, _CertPageD>,
  )
})
function selectCert(
  cert?: _CertPageD,
) {
  emit('select', cert ?? [])
}

onUnmounted(() => {
  selectCert(void 0)
})

function getCertType(certType?: CertTyping) {
  return getCertTypingDesc(certType)
}
</script>

<template>
<VCard class="h-full overflow-y-auto">
  <VCardText class="space-y-4">
    <template v-for="(certs, groupCode) in groupedCerts" :key="groupCode">
      <!-- 每组证件 -->
      <VCard>
        <VCardTitle>{{ getCertType(certs[0]?.doType) }}</VCardTitle>
        <VDivider />
        <VCardText class="overflow-y-auto">
          <VRow dense>
            <template v-for="(item, _) in certs" :key="_">
              <VCol :cols="certs.length === 1 ? 12 : 6">
                <TnImagePreview>
                  <template #activator="{ props: pps }">
                    <VImg v-bind="pps" cover :src="item.waterMarkerAttachment?.fullAccessUrl" width="auto" height="auto" />
                  </template>
                </TnImagePreview>
              </VCol>
            </template>
          </VRow>
        </VCardText>
        <VCardActions>
          <VSpacer />
          <VBtn color="primary" @click="() => selectCert(certs)">
            开始审核
          </VBtn>
        </VCardActions>
      </VCard>
    </template>
  </VCardText>
</VCard>
</template>
