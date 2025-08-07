<script setup lang="ts">
import type { entities } from '@/api'
import { formatDate } from '@compose/external/dayjs'
import ShowPhoneComp from '@/components/ShowPhoneComp.vue'

defineProps<{
  info?: entities.Dynamic_UserInfo
}>()
</script>

<template>
<VContainer>
  <VRow>
    <VCol cols="12">
      <VCard>
        <VCardTitle class="text-h6 primary pa-4">
          <VIcon left>
            mdi-account-details
          </VIcon>
          基本信息
        </VCardTitle>
        <VCardText class="pa-4">
          <VRow>
            <VCol cols="12" sm="6" md="4">
              <div class="align-center d-flex mb-2">
                <VIcon color="primary" class="mr-2">
                  mdi-information
                </VIcon>
                <span class="font-weight-medium">用户信息主次：</span>
                <span class="ml-1">{{ info?.primaryUserInfo ? "主信息" : "次要信息" }}</span>
              </div>
            </VCol>
            <VCol cols="12" sm="6" md="4">
              <div class="d-flex align-center mb-2">
                <VIcon color="primary" class="mr-2">
                  mdi-identifier
                </VIcon>
                <span class="font-weight-medium">用户信息 ID：</span>
                <span class="ml-1">{{ info?.id }}</span>
              </div>
            </VCol>
            <VCol cols="12" sm="6" md="4">
              <div class="d-flex align-center mb-2">
                <VIcon color="primary" class="mr-2">
                  mdi-account
                </VIcon>
                <span class="font-weight-medium">账号 ID：</span>
                <span class="ml-1">{{ info?.id }}</span>
              </div>
            </VCol>
            <VCol cols="12" sm="6" md="4">
              <div class="d-flex align-center mb-2">
                <VIcon color="primary" class="mr-2">
                  mdi-account-circle
                </VIcon>
                <span class="font-weight-medium">姓名：</span>
                <span class="ml-1">{{ info?.name || info?.account?.account }}</span>
              </div>
            </VCol>
            <VCol cols="12" sm="6" md="4">
              <div class="d-flex align-center mb-2">
                <VIcon color="primary" class="mr-2">
                  mdi-card-account-details
                </VIcon>
                <span class="font-weight-medium">身份证号：</span>
                <span class="ml-1">{{ info?.idCard }}</span>
              </div>
            </VCol>
            <VCol cols="12" sm="6" md="4">
              <div class="d-flex align-center mb-2">
                <VIcon color="primary" class="mr-2">
                  mdi-certificate
                </VIcon>
                <span class="font-weight-medium">残疾证号：</span>
                <span class="ml-1">{{ info?.disInfo?.certCode }}</span>
              </div>
            </VCol>
            <VCol cols="12" sm="6" md="4">
              <div class="d-flex align-center mb-2">
                <VIcon color="primary" class="mr-2">
                  mdi-phone
                </VIcon>
                <span class="font-weight-medium">手机号：</span>
                <ShowPhoneComp :phone="info?.phone" />
              </div>
            </VCol>
            <VCol cols="12" sm="6" md="4">
              <div class="d-flex align-center mb-2">
                <VIcon color="primary" class="mr-2">
                  mdi-phone-alert
                </VIcon>
                <span class="font-weight-medium">备用手机号：</span>
                <ShowPhoneComp :phone="info?.sparePhone" />
              </div>
            </VCol>
            <VCol cols="12" sm="6" md="4">
              <div class="d-flex align-center mb-2">
                <VIcon color="primary" class="mr-2">
                  mdi-calendar
                </VIcon>
                <span class="font-weight-medium">出生日期：</span>
                <span class="ml-1">{{ info?.birthday ? formatDate(info?.birthday) : "" }}</span>
              </div>
            </VCol>
            <VCol cols="12" sm="6" md="4">
              <div class="d-flex align-center mb-2">
                <VIcon color="primary" class="mr-2">
                  mdi-map-marker
                </VIcon>
                <span class="font-weight-medium">属地：</span>
                <span class="ml-1">{{ info?.address?.fullPath }}</span>
              </div>
            </VCol>
          </VRow>
        </VCardText>
      </VCard>
    </VCol>
  </VRow>
</VContainer>
</template>
