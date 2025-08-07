<script setup lang="ts">
import type { RefId } from '@compose/types'
import type { api, ResponseOf } from '@/api'
import { getDisTypingDesc, getGenderTypingDesc } from '@/api'

// 用户数据类型
type UserInfoType = ResponseOf<typeof api.userInfoV2Api.getUserInfosAsAdmin>['d'][number]

interface Props {
  userData: UserInfoType
}

interface Emits {
  details: [id: RefId]
  modify: [id: RefId]
}

defineProps<Props>()
const emit = defineEmits<Emits>()

function handleDetails(infoId: RefId) {
  emit('details', infoId)
}

function handleModify(infoId: RefId) {
  emit('modify', infoId)
}
</script>

<template>
<!-- 用户信息卡片 -->
<VCard class="user-info-card rounded-lg transition-all duration-300 ease-in-out hover:-translate-y-1">
  <!-- 卡片顶部渐变背景 -->
  <div class="card-header-bg rounded-t-lg" />

  <!-- 用户卡标题 -->
  <VCardTitle class="card-title flex shrink-0 items-center justify-between pb-3 pt-5">
    <div class="inline-flex items-center gap-2">
      <!-- 用户名称 -->
      <span
        :class="{
          'text-error font-weight-bold': userData.isBlacked,
          'text-primary font-weight-bold': userData.wechatOpenid && !userData.isBlacked,
          'text-dark font-weight-bold': !userData.wechatOpenid && !userData.isBlacked,
        }"
        class="text-h6"
      >
        {{ userData.name || userData.account?.nickName }}
      </span>

      <!-- 状态图标 -->
      <div class="inline-flex items-center gap-1">
        <VTooltip v-if="userData.account" text="拥有用户账户" location="top">
          <template #activator="{ props: pps }">
            <VIcon v-bind="pps" icon="mdi-account-box" color="info" size="small" class="status-icon" />
          </template>
        </VTooltip>
        <VTooltip v-if="userData.isBlacked" text="已被拉黑" location="top">
          <template #activator="{ props: pps }">
            <VIcon v-bind="pps" icon="mdi:mdi-block-helper" color="error" size="small" class="status-icon" />
          </template>
        </VTooltip>
        <VTooltip v-if="userData.wechatOpenid" text="已绑定微信" location="top">
          <template #activator="{ props: pps }">
            <VIcon v-bind="pps" icon="mdi-wechat" color="success" size="small" class="status-icon" />
          </template>
        </VTooltip>
      </div>
    </div>

    <!-- 菜单 -->
    <VMenu location="bottom end" transition="slide-y-transition">
      <template #activator="{ props: pps }">
        <VBtn v-bind="pps" icon="mdi-dots-vertical" variant="text" size="small" class="menu-btn" />
      </template>
      <VList density="compact" lines="one" nav class="rounded-lg">
        <VListItem link class="menu-item">
          <template #prepend>
            <VIcon icon="mdi-certificate" size="small" class="mr-2" />
          </template>
          <VListItemTitle>添加证件</VListItemTitle>
        </VListItem>
        <VListItem link class="menu-item" @click="handleModify(userData.id)">
          <template #prepend>
            <VIcon icon="mdi-account-edit" size="small" class="mr-2" />
          </template>
          <VListItemTitle>修改信息</VListItemTitle>
        </VListItem>
        <YPreAuthorize :hasAnyPermissions="[`ROOT`]">
          <VListItem baseColor="error" link class="menu-item">
            <template #prepend>
              <VIcon icon="mdi-delete-sweep-outline" size="small" class="mr-2" />
            </template>
            <VListItemTitle>删除</VListItemTitle>
          </VListItem>
        </YPreAuthorize>
      </VList>
    </VMenu>
  </VCardTitle>

  <VDivider class="mx-4" />

  <VCardText class="flex flex-col flex-grow py-4">
    <!-- 性别和残疾信息 -->
    <div class="user-attributes mb-4 px-1">
      <VChip
        v-if="userData.gender"
        size="small"
        class="font-weight-medium mr-2"
        :color="userData.gender === 'MAN' ? 'info' : 'error'"
        variant="tonal"
      >
        <template #prepend>
          <VIcon size="x-small" :icon="userData.gender === 'MAN' ? 'mdi-gender-male' : 'mdi-gender-female'" />
        </template>
        {{ getGenderTypingDesc(userData.gender) }}
      </VChip>

      <VChip
        v-if="userData.disInfo"
        size="small"
        color="warning"
        variant="tonal"
        class="font-weight-medium"
      >
        <template #prepend>
          <VIcon size="x-small" icon="mdi-wheelchair" />
        </template>
        {{ getDisTypingDesc(userData.disInfo?.dsType) }}{{ userData.disInfo?.level }}
      </VChip>
    </div>

    <!-- 联系信息 -->
    <VList density="compact" lines="one" class="contact-info flex-grow bg-transparent pa-0">
      <VListItem v-if="userData.phone" class="info-item px-1 min-h-auto!">
        <template #prepend>
          <VIcon icon="mdi-phone" size="small" class="info-icon mr-2" color="primary" />
        </template>
        <VListItemTitle class="text-body-2 truncate">
          {{ userData.phone }}
        </VListItemTitle>
      </VListItem>

      <VListItem v-if="userData.address?.fullPath" class="info-item mt-2 px-1 min-h-auto!">
        <template #prepend>
          <VIcon icon="mdi-map-marker" size="small" class="info-icon mr-2" color="primary" />
        </template>
        <VListItemTitle class="text-body-2 truncate">
          {{ userData.address?.fullPath }}
        </VListItemTitle>
      </VListItem>
    </VList>
  </VCardText>

  <!-- Card Actions -->
  <VCardActions class="mt-auto shrink-0 pa-4 pt-2">
    <VSpacer />
    <VBtn
      variant="tonal"
      size="small"
      color="primary"
      class="action-btn mr-2"
      @click="handleDetails(userData.id)"
    >
      <VIcon icon="mdi-card-account-details-outline" size="small" class="mr-1" />
      详情
      <VTooltip activator="parent" text="用户详情" location="top" />
    </VBtn>

    <VBtn
      variant="tonal"
      size="small"
      color="secondary"
      class="action-btn"
      @click="handleModify(userData.id)"
    >
      <VIcon icon="mdi-account-edit" size="small" class="mr-1" />
      修改
      <VTooltip activator="parent" text="修改信息" location="top" />
    </VBtn>
  </VCardActions>
</VCard>
</template>

<style lang="scss" scoped>
.user-info-card {
  position: relative;
  min-height: 280px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 12px rgba(var(--v-theme-primary), 0.1);
  border: 1px solid rgba(var(--v-theme-surface-variant), 0.12);
  overflow: hidden;

  &:hover {
    box-shadow: 0 8px 24px rgba(var(--v-theme-primary), 0.15);

    .action-btn {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .card-header-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 40px;
    background: linear-gradient(90deg, rgba(var(--v-theme-primary), 0.7), rgba(var(--v-theme-secondary), 0.5));
    opacity: 0.7;
  }

  .card-title {
    border-bottom: 1px solid rgba(var(--v-theme-primary), 0.1);

    &::after {
      content: '';
      position: absolute;
      left: 0;
      bottom: -1px;
      width: 40px;
      height: 3px;
      background-color: rgb(var(--v-theme-primary));
    }
  }

  .status-icon {
    transition: transform 0.2s ease;

    &:hover {
      transform: scale(1.2);
    }
  }

  .menu-btn {
    opacity: 0.7;
    transition:
      opacity 0.2s ease,
      background-color 0.2s ease;

    &:hover {
      opacity: 1;
      background-color: rgba(var(--v-theme-surface-variant), 0.1);
    }
  }

  .menu-item {
    transition: background-color 0.2s ease;
  }

  .user-attributes {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .contact-info {
    .info-item {
      transition: background-color 0.2s ease;
      border-radius: 8px;
      margin-bottom: 4px;

      &:hover {
        background-color: rgba(var(--v-theme-surface-variant), 0.08);
      }

      .info-icon {
        opacity: 0.8;
      }
    }
  }

  .action-btn {
    transition: all 0.3s ease;
    opacity: 0.8;
    transform: translateY(4px);
    min-width: 72px;

    &:hover {
      transform: translateY(-2px) !important;
      opacity: 1 !important;
    }
  }
}

:deep(.v-list-item--density-compact.min-h-auto) {
  min-height: auto !important;
}
</style>
