<script setup lang="ts">
import type { dynamic } from '@compose/types'
import type { PlatformType, RequestOf } from '@/api'
import { Pw } from '@compose/shared'
import { boolean, object, string } from 'yup'
import { api, PlatformType_CONSTANTS } from '@/api'

interface Props {
  path?: string
  title?: string
  icon?: string
  color?: string
  platform?: PlatformType
  platformTitle?: string
}

const props = withDefaults(defineProps<Props>(), {
  path: void 0,
  title: void 0,
  icon: void 0,
  color: void 0,
  platform: void 0,
  platformTitle: void 0,
})

type RoleView = RequestOf<typeof api.aclV2Api.postMenuByPatternAsAdmin>['body']['roles'][number]
type PermissionsView = RequestOf<typeof api.aclV2Api.postMenuByPatternAsAdmin>['body']['permissions'][number]

async function putMenus(dto?: RequestOf<typeof api.aclV2Api.postMenuByPatternAsAdmin>['body']) {
  if (!dto || !props.path) {
    return
  }

  await api.aclV2Api.postMenuByPatternAsAdmin({
    body: dto,
  })
}

const patternIsVoidComputed = computed(() => {
  return props.path === '' || props.path === void 0
})

const schema = object({
  platformType: string().oneOf(PlatformType_CONSTANTS).required(),
  pattern: string().required(),
  title: string().optional(),
  doc: string(),
  requireLogin: boolean().required().default(() => false),
  role: object({
    id: string().required(),
    name: string().required(),
  }).required().default(() => []),
  permissions: object({
    id: string().required(),
    name: string().required(),
  }).required().default(() => []),
})

const roles = ref<RoleView[]>()
const permissions = ref<PermissionsView[]>()

async function getAllRoleAndPermissions() {
  roles.value = (await api.aclV2Api.getRolesAsAdmin({ spec: Pw.DEFAULT_MAX })).d.filter((e): e is RoleView => Boolean(e.name))
  permissions.value = (await api.aclV2Api.getAllPermissionsAsAdmin({ spec: Pw.DEFAULT_MAX })).d.filter((e): e is PermissionsView => Boolean(e.name))
}

onMounted(getAllRoleAndPermissions)
const defaultValue = computed(() => {
  return {
    platformType: props.platform,
    pattern: props.path,
    title: props.title,
    requireLogin: false,
    menuRoles: [],
    menuPermissions: [],
    roles: [],
    permissions: [],
  }
})

function handleError(ctx: dynamic) {
  console.error(ctx)
}
</script>

<template>
<VCard class="wh-full">
  <!-- 空状态 -->
  <VEmptyState v-if="patternIsVoidComputed">
    <template #title>
      未选择菜单
    </template>
    <template #media>
      <YIco class="i-mdi:delete-empty-outline text-24" />
    </template>
  </VEmptyState>
  <VCardItem v-else>
    <YForm :initValue="defaultValue" :schema="schema" @error="handleError" @submit="putMenus">
      <VCard>
        <!-- 标题 -->
        <VCardTitle>
          <div class="flex justify-around">
            <div class="f-y-c space-x-2">
              <VChip :color="props.color">
                <template #prepend>
                  <YIco class="text-6" :class="[props.icon]" />
                </template>
                {{ props.platformTitle }}
              </VChip>
              <h2>{{ props?.path }}</h2>
            </div>
            <div>
              {{ props.title }}
            </div>
          </div>
        </VCardTitle>
        <VCardItem>
          <!-- 更改表单 -->
          <YField label="平台类型" name="platformType">
            <VTextField disabled readonly />
          </YField>
          <YField name="pattern" label="名称">
            <VTextField readonly disabled />
          </YField>
          <YField name="title" label="名称">
            <VTextField />
          </YField>
          <YField label="是否需要登录" name="requireLogin">
            <VSwitch color="primary" />
          </YField>
          <template v-if="roles && permissions">
            <YField name="menuRoles" label="所需角色">
              <VCombobox chips color="primary" multiple itemTitle="role.name" returnObject :items="roles" />
            </YField>
            <YField name="menuPermissions" label="所需权限">
              <VCombobox chips color="primary" multiple itemTitle="permissions.name" returnObject :items="permissions" />
            </YField>
          </template>
          <YField name="doc" label="接口描述">
            <VTextarea />
          </YField>
        </VCardItem>
        <VCardActions>
          <VSpacer />
          <slot name="actions" />
          <VBtn color="primary" type="submit">
            提交
          </VBtn>
        </VCardActions>
      </VCard>
    </YForm>
  </VCardItem>
</VCard>
</template>
