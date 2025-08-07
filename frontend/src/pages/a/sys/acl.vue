<script setup lang="ts">
import type { Pq, RefId } from '@compose/types'
import type { PermissionsAdminPostDto } from '@/__generated/model/static'
import type { entities } from '@/api'
import { Pw } from '@compose/shared'
import { api } from '@/api'
import RoleGroupSelectorView from '@/pages/a/sys/acl/RoleGroupSelectorView.vue'

const selectedRoleGroup = ref<entities.Dynamic_RoleGroup | undefined>(void 0)
const selectedRole = ref<entities.Dynamic_Role | undefined>(void 0)
const selectedPermissions = ref<entities.Dynamic_Permissions | undefined>(void 0)

async function getAllRoleByRoleGroup(pq: Pq, roleGroup?: entities.Dynamic_RoleGroup) {
  if (!roleGroup?.id) {
    return void 0
  }
  return api.aclV2Api.getAllRoleGroupRolesAsAdmin({
    spec: {
      ...pq,
      id: roleGroup.id,
    },
  })
}

async function getAllRole(pq?: Pq) {
  if (!pq) {
    return Pw.empty()
  }
  return await api.aclV2Api.getRolesAsAdmin({ spec: pq })
}

async function _bindRoleToRoleGroup(role: entities.Dynamic_Role, roleGroup: entities.Dynamic_RoleGroup) {
  if (!role.id || !roleGroup.id) {
    return
  }
  await api.aclV2Api.putRoleGroupRoles({
    body: {
      id: roleGroup.id,
      roles: [{ id: role.id }],
    },
  })
}

async function _unbindRoleToRoleGroup(role: entities.Dynamic_Role, roleGroup: entities.Dynamic_RoleGroup) {
  if (!role.id || !roleGroup.id) {
    return
  }
  await api.aclV2Api.deleteRoleGroupRoles({
    body: {
      id: roleGroup.id,
      roles: [{ id: role.id }],
    },
  })
}

async function getAllRoleGroup(pq?: Pq) {
  if (!pq) {
    return Pw.empty()
  }
  return await api.aclV2Api.getAllRoleGroupsAsAdmin({ spec: pq })
}

async function postRole(role?: entities.Dynamic_Role) {
  if (!role || role.name === void 0) {
    return
  }
  return await api.aclV2Api.postRoleAsAdmin({
    body: {
      name: role.name,
      doc: role.doc,
    },
  })
}

async function _getAllPermissionsByRole(pq: Pq, role?: entities.Dynamic_Role) {
  if (!role || !role.id) {
    return Pw.empty()
  }
  return await api.aclV2Api.getALlRolePermissionsAsAdmin({
    spec: {
      ...pq,
      id: role.id,
    },
  })
}

async function _deleteRoleGroupById(id?: RefId) {
  if (!id) {
    return
  }
  await api.aclV2Api.deleteRoleGroupByIdAsAdmin({
    id: id as unknown as number,
  })
}

async function _deleteRoleById(id?: RefId) {
  if (!id) {
    return
  }
  await api.aclV2Api.deleteRoleByIdAsAdmin({ id: id as unknown as number })
}

async function _getAllUserByRoleGroupId(pq?: Pq, roleGroup?: entities.Dynamic_RoleGroup) {
  if (!pq || !roleGroup?.id) {
    return void 0
  }
  return await api.aclV2Api.getRoleGroupUserAccounts({
    spec: {
      ...pq,
      roleGroupIds: [
        roleGroup.id,
      ],
    },
  })
}

async function _bindRoleGroupToUser(user: entities.Dynamic_UserAccount, roleGroup: entities.Dynamic_RoleGroup) {
  if (!roleGroup.id || !user.id) {
    return
  }
  await api.aclV2Api.putUserAccountRoleGroupsAsAdmin({
    body: {
      id: user.id,
      roleGroups: [{
        id: roleGroup.id,
      }],
    },
  })
}

async function _unbindUserFromRoleGroup(user: entities.Dynamic_UserAccount, roleGroup: entities.Dynamic_RoleGroup) {
  if (!user.id || !roleGroup.id) {
    return void 0
  }
  await api.aclV2Api.deleteUserAccountRoleGroupAsAdmin({
    body: {
      id: user.id,
      roleGroups: [{
        id: roleGroup.id,
      }],
    },
  })
}

function _bindPermissionsToRole(permissions: entities.Dynamic_Permissions, role: entities.Dynamic_Role) {
  if (!permissions.id || !role.id) {
    return
  }
  void api.aclV2Api.postRolePermissions({
    body: {
      id: role.id,
      permissions: [
        { id: permissions.id },
      ],
    },
  })
}

function _getAllApiByPermissionsId(pq?: Pq, permissions?: entities.Dynamic_Permissions) {
  if (!pq || !permissions?.id) {
    return Pw.empty()
  }
  return api.aclV2Api.getAllApisAsAdmin({
    spec: {
      ...pq,
      permissionsId: permissions.id,
    },
  })
}

async function _unbindPermissionsFromRole(permissions: entities.Dynamic_Permissions, role: entities.Dynamic_Role) {
  if (!permissions.id || !role.id) {
    return
  }
  await api.aclV2Api.deleteRolePermissions({
    body: {
      id: role.id,
      permissions: [
        { id: permissions.id },
      ],
    },
  })
}

async function _unbindPermissionsFromApi(a: entities.Dynamic_Api, permissions: entities.Dynamic_Permissions) {
  if (!a.id || !permissions.id) {
    return
  }
  await api.aclV2Api.patchApiPermissions({
    dto: {
      id: a.id,
      permissionsId: void 0,
    },
  })
}

async function _bindPermissionsToApi(a: entities.Dynamic_Api, permissions: entities.Dynamic_Permissions) {
  if (!a.id || !permissions.id) {
    return
  }
  await api.aclV2Api.patchApiPermissions({
    dto: {
      id: a.id,
      permissionsId: permissions.id,
    },
  })
}

async function _getAllApis(pq: Pq, parent?: entities.Dynamic_Permissions) {
  if (parent) {
    return api.aclV2Api.getAllApisAsAdmin({
      spec: {
        ...pq,
        permissionsId: parent.id,
        permissionsName: parent.name,
      },
    })
  } else {
    return await api.aclV2Api.getAllApisAsAdmin({ spec: pq })
  }
}

async function deletePermissionsById(id?: RefId) {
  if (!id) {
    return
  }
  await api.aclV2Api.deletePermissionsByIdAsAdmin({ id: id as unknown as number })
}

async function getAllPermissions(pq?: Pq) {
  if (!pq) {
    return Pw.empty()
  }
  return await api.aclV2Api.getAllPermissionsAsAdmin({ spec: pq })
}

async function postPermissions(dto?: PermissionsAdminPostDto) {
  if (!dto) {
    return
  }
  return await api.aclV2Api.postPermissions({ dto })
}

async function getAllUserAccount(pq?: Pq) {
  if (!pq) {
    return Pw.empty()
  }
  return await api.authApi.getUserAccountsAsAdmin({ spec: pq })
}

async function putRole(role?: entities.Dynamic_Role) {
  if (!role || role.name === void 0) {
    return
  }
  return await api.aclV2Api.putRoleAsAdmin({
    body: {
      id: role.id,
      name: role.name,
      doc: role.doc,
    },
  })
}

async function putRoleGroup(roleGroup?: entities.Dynamic_RoleGroup) {
  if (!roleGroup || roleGroup.name === void 0) {
    return
  }
  await api.aclV2Api.putRoleGroupAsAdmin({
    body: {
      id: roleGroup.id,
      name: roleGroup.name,
      doc: roleGroup.doc,
    },
  })
}

async function postRoleGroup(roleGroup?: entities.Dynamic_RoleGroup) {
  if (!roleGroup || roleGroup.name === void 0) {
    return
  }
  return await api.aclV2Api.postRoleGroupAsAdmin({
    body: {
      name: roleGroup.name,
      doc: roleGroup.doc,
    },
  })
}
</script>

<template>
<div class="space-y-4">
  <VRow dense>
    <VCol>
      <RoleGroupSelectorView
        v-model:selected="selectedRoleGroup" title="角色组" :getAllFn="getAllRoleGroup" :putByIdFn="putRoleGroup" :postFn="postRoleGroup"
        :deleteByIdFn="_deleteRoleGroupById"
      />
    </VCol>
    <VCol>
      <RoleGroupSelectorView
        v-model:parent="selectedRoleGroup" nameKey="nickName" title="用户" :getAllParentFn="_getAllUserByRoleGroupId"
        :getAllFn="getAllUserAccount" :linkFn="_bindRoleGroupToUser" :unlinkFn="_unbindUserFromRoleGroup"
      />
    </VCol>
  </VRow>

  <VRow dense>
    <VCol>
      <!-- 角色列表 -->
      <RoleGroupSelectorView
        v-model:parent="selectedRoleGroup"
        v-model:selected="selectedRole"
        color="secondary"
        title="角色"
        :postFn="postRole"
        :putByIdFn="putRole"
        :getAllFn="getAllRole"
        :deleteByIdFn="_deleteRoleById"
        :linkFn="_bindRoleToRoleGroup"
        :unlinkFn="_unbindRoleToRoleGroup"
        :getAllParentFn="getAllRoleByRoleGroup"
      />
    </VCol>

    <VCol>
      <!-- 权限列表 -->
      <RoleGroupSelectorView
        v-model:parent="selectedRole"
        v-model:selected="selectedPermissions"
        color="error"
        title="权限"
        :linkFn="_bindPermissionsToRole"
        :unlinkFn="_unbindPermissionsFromRole"
        :postFn="postPermissions"
        :deleteByIdFn="deletePermissionsById"
        :getAllFn="getAllPermissions"
        :getAllParentFn="_getAllPermissionsByRole"
      />
    </VCol>
  </VRow>

  <!-- 接口面板 -->
  <VRow dense>
    <VCol :cols="12">
      <RoleGroupSelectorView
        v-model:parent="selectedPermissions" :getAllParentFn="_getAllApiByPermissionsId" color="yellow" title="接口"
        :getAllFn="_getAllApis" nameKey="apiPath" docKey="apiMethod" :linkFn="_bindPermissionsToApi"
        :unlinkFn="_unbindPermissionsFromApi"
      >
        <template #label="{ item }">
          <HttpMethodChip :method="item.apiMethod" />
        </template>
        <template #name="{ item }">
          <div>{{ (item?.name || "") + (item?.doc ? ` - ${item?.doc}` : "") || item.apiPath }}</div>
        </template>
        <template #subtitle="{ item }">
          {{ item.apiPath }}
        </template>
      </RoleGroupSelectorView>
    </VCol>
  </VRow>
</div>
</template>
