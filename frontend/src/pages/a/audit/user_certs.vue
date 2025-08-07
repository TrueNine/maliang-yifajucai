<script setup lang="ts">
import type { RefId } from '@compose/types'
import type { CertView } from '@/__generated/model/static'
import type { ResponseOf } from '@/api'
import { formatDate } from '@compose/external/dayjs'
import { api } from '@/api'
import { useSnackbar } from '@/components'
import CertAuditEditorView from './user_certs/CertAuditEditorView.vue'
import CertAuditView from './user_certs/CertAuditView.vue'

type UserAccount = ResponseOf<typeof api.authApi.getUserAccountsByIdsAsAdmin>[number]
type UserAccountWithCertCount = UserAccount & {
  userAccountId?: string
  count?: number
}

const data = ref<ResponseOf<typeof api.certV2Api.getNotAuditCertCounts>>()
const accounts = ref<UserAccountWithCertCount[]>()
const loading = ref(false)

async function loadData() {
  loading.value = true
  try {
    data.value = void 0
    accounts.value = void 0
    data.value = await api.certV2Api.getNotAuditCertCounts({
      spec: {
        hasAuditStatus: ['NONE'],
      },
    })
    const ids = Object.keys(data.value)
    if (!ids.length) {
      return
    }
    accounts.value = (
      await api.authApi.getUserAccountsByIdsAsAdmin({
        ids: ids as unknown as number[],
      })
    ).map((e) => {
      return {
        ...e,
        userAccountId: e.id,
        count: data.value?.[e.id ?? 'none'] ?? 0,
      }
    })
  } finally {
    loading.value = false
  }
}
onMounted(loadData)
const expandActiveId = ref<RefId>()
const selectedCertPage = ref<ResponseOf<typeof api.certV2Api.getNotAuditCertsAsAdmin>>()

async function updateAllPages() {
  selectedCertPage.value = void 0
  if (!expandActiveId.value) {
    return
  }
  selectedCertPage.value = await api.certV2Api.getNotAuditCertsAsAdmin({
    spec: {
      hasUserAccountIds: [expandActiveId.value],
    },
  })
}

watch(expandActiveId, updateAllPages)

const userCerts = ref<CertView[]>()
const snackbar = useSnackbar()
async function changeAuditStatus(auditList?: CertView[]) {
  if (!auditList) {
    return
  }
  await snackbar.message(
    api.certV2Api.patchCertsAuditStatusesAsAdmin({
      body: auditList,
    }),
  )

  await loadData()
  await updateAllPages()
}
</script>

<template>
<VContainer fluid class="pa-0">
  <VFadeTransition>
    <VProgressLinear v-if="loading" indeterminate color="primary" />
  </VFadeTransition>

  <VSlideYTransition group>
    <template v-if="!loading && !accounts?.length">
      <VRow class="align-center fill-height justify-center" style="min-height: 40vh">
        <VCol cols="12" class="text-center">
          <VIcon icon="mdi-certificate" size="64" color="grey-lighten-1" />
          <div class="text-h6 text-grey-lighten-1 mt-4">
            暂无待审核的证件
          </div>
        </VCol>
      </VRow>
    </template>

    <template v-else-if="accounts?.length">
      <VRow dense>
        <!-- 选项卡 -->
        <VCol cols="2">
          <VCard class="h-60vh">
            <VCardText class="pa-0">
              <VExpansionPanels v-model="expandActiveId" variant="accordion">
                <VSlideYTransition group>
                  <VExpansionPanel v-for="item in accounts" :key="item.userAccountId" :value="item.userAccountId">
                    <VExpansionPanelTitle>
                      <VRow noGutters>
                        <VCol>{{ item.account }} | {{ item.nickName ?? "-" }}</VCol>
                        <VCol cols="auto">
                          <VBadge :content="item.count" color="error" />
                        </VCol>
                      </VRow>
                    </VExpansionPanelTitle>
                    <VExpansionPanelText>
                      <VList lines="two" density="compact">
                        <VListItem>
                          <VListItemTitle>证件数</VListItemTitle>
                          <VListItemSubtitle>{{ item.count }}</VListItemSubtitle>
                        </VListItem>
                        <VListItem v-if="item.crd">
                          <VListItemTitle>注册时间</VListItemTitle>
                          <VListItemSubtitle>{{ formatDate(item.crd) }}</VListItemSubtitle>
                        </VListItem>
                      </VList>
                    </VExpansionPanelText>
                  </VExpansionPanel>
                </VSlideYTransition>
              </VExpansionPanels>
            </VCardText>
          </VCard>
        </VCol>

        <!-- 审核区域 -->
        <VCol cols="5">
          <VCard class="h-60vh overflow-y-auto">
            <VCardText class="pa-0">
              <CertAuditView
                :certPage="selectedCertPage"
                :userAccountId="expandActiveId"
                @select="v => userCerts = v"
              />
            </VCardText>
          </VCard>
        </VCol>

        <VCol cols="5">
          <VCard class="h-60vh">
            <VCardText class="pa-0">
              <CertAuditEditorView
                :certs="userCerts"
                @reject="changeAuditStatus"
                @pass="changeAuditStatus"
              />
            </VCardText>
          </VCard>
        </VCol>
      </VRow>
    </template>
  </VSlideYTransition>
</VContainer>
</template>
