<script setup lang="ts">
import { maybeArray } from '@compose/shared'
import CertAddView from './add/CertAddView.vue'
import UserInfoAddView from './add/UserInfoAddView.vue'

enum UserInfoTab {
  INFO = 0,
  CERT = 1,
}

const route = useRoute()
const iId = ref(maybeArray(route.query.iId).find(Boolean) ?? void 0)
const currentTab = ref(UserInfoTab.INFO)
const files = ref<File[]>([])
</script>

<template>
<VCard>
  <VCardTitle>
    <VTabs v-model="currentTab" alignTabs="center">
      <VTab :value="UserInfoTab.INFO">
        信息
      </VTab>
      <VTab :value="UserInfoTab.CERT">
        {{ !iId ? '证件（先添加用户）' : '证件' }}
      </VTab>
    </VTabs>
  </VCardTitle>

  <VWindow v-model="currentTab">
    <VWindowItem :value="UserInfoTab.INFO">
      <VRow>
        <VCol :cols="12" :sm="12" :md="12" :xl="6">
          <!-- 绑定文件 -->
          <TnPackFileExtractor v-model="files" />
          <TnIdCardViewer v-model="files" />
        </VCol>
        <VCol :cols="12" :sm="12" :md="12" :xl="6">
          <UserInfoAddView v-model="iId" />
        </VCol>
      </VRow>
    </VWindowItem>
    <VWindowItem :value="UserInfoTab.CERT">
      <CertAddView v-model:files="files" v-model:userInfoId="iId" />
    </VWindowItem>
  </VWindow>
</VCard>
</template>
