<script setup lang="tsx">
import type { Pq, RefId } from '@compose/types'
import type { UserInfoAdminSpec } from '@/__generated/model/static/UserInfoAdminSpec'
import type { UserInfoAdminView } from '@/__generated/model/static/UserInfoAdminView'
import { Pw } from '@compose/shared'
import { onClickOutside, useDebounceFn, useVModels } from '@vueuse/core'

import { useRouter } from 'vue-router'
import { api } from '@/api'
import { useSnackbar } from '@/components'
import PaginatorComp from '@/components/paginator/PaginatorComp.vue'
import { splitName } from '@/components/TnNameInput'

interface UserSelection {
  userId?: RefId
  infoId?: RefId
  displayName?: string
}

interface Props {
  modelValue?: UserSelection
  displayName?: string
  userId?: RefId
  infoId?: RefId
  label?: string
  placeholder?: string
  errorMessages?: string[]
  hint?: string
  persistentHint?: boolean
}

interface Emits {
  'update:modelValue': [selection?: UserSelection]
  'update:userId': [userId?: RefId]
  'update:infoId': [infoId?: RefId]
  'update:displayName': [displayName?: string]
}

const props = withDefaults(defineProps<Props>(), {
  displayName: void 0,
  userId: void 0,
  infoId: void 0,
  label: void 0,
  placeholder: '搜索用户...',
  errorMessages: void 0,
  hint: void 0,
  persistentHint: void 0,
})

const emit = defineEmits<Emits>()

const {
  modelValue: mv,
  userId: _userId,
  infoId: _infoId,
  displayName: _displayName,
} = useVModels(props, emit, { passive: true })

const loading = ref(false)
const showSearchPanel = ref(false)
const searchSpec = ref<UserInfoAdminSpec>({})
const searchInput = ref('')
const containerRef = ref<HTMLElement>()
const isMouseInPanel = ref(false)

// 分页相关
const pq = ref<Pq>({ ...Pw.DEFAULT_MAX, s: 10 })
const data = ref<{
  d?: UserInfoAdminView[]
  t?: number
  p?: number
}>()

const dataList = computed(() => data.value?.d || [])

// 表格列配置
const headers = [
  { title: '用户', key: 'user', sortable: false },
  { title: '账号', key: 'account', sortable: false },
  { title: '地址', key: 'address', sortable: false },
  { title: '最后登录', key: 'lastLogin', sortable: true },
  { title: '状态', key: 'status', sortable: false },
  { title: '操作', key: 'actions', sortable: false },
]

const router = useRouter()
const snackbar = useSnackbar()

// 处理点击外部
onClickOutside(containerRef, (event) => {
  // 检查点击事件是否来自数据表格组件
  const target = event.target as HTMLElement
  if (target.closest('.v-data-table-footer')) {
    return
  }
  showSearchPanel.value = false
})

const debouncedSearch = useDebounceFn((value: string) => {
  const trimmed = value.trim()
  searchSpec.value = {}

  if (trimmed) {
    if (/^\d{11}$/.test(trimmed)) {
      searchSpec.value = { phone: trimmed }
    } else if (trimmed.length <= 5) {
      const { firstName, lastName } = splitName(trimmed)
      searchSpec.value = { firstName, lastName }
    } else {
      searchSpec.value = { name: trimmed }
    }
  }

  void handleSearch()
  // 有搜索内容时自动显示面板
  if (trimmed) {
    showSearchPanel.value = true
  }
}, 300)

// 处理面板鼠标事件
function handlePanelMouseEnter() {
  isMouseInPanel.value = true
}

function handlePanelMouseLeave() {
  isMouseInPanel.value = false
}

// 处理输入框聚焦
function handleFocus() {
  showSearchPanel.value = true
  // 如果还没有数据，获取初始列表
  if (!data.value?.d) {
    void handleSearch()
  }
}

// 处理输入框失焦
function handleBlur() {
  // 给一点延迟，让点击事件可以先触发
  setTimeout(() => {
    if (!isMouseInPanel.value) {
      showSearchPanel.value = false
    }
  }, 100)
}

// 搜索逻辑
async function handleSearch() {
  try {
    loading.value = true
    const result = await api.userInfoV2Api.getUserInfosAsAdmin({
      spec: {
        ...searchSpec.value,
        s: pq.value.s,
        o: pq.value.o,
      },
    })
    data.value = result
  } catch (error) {
    console.error('搜索用户失败:', error)
    snackbar.error('搜索用户失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 清除所有搜索条件
function clearSearch() {
  searchSpec.value = {}
  searchInput.value = ''
  data.value = { d: [] }
  showSearchPanel.value = false
  snackbar.success('已清空搜索条件')
}

// 用户选择
function selectUser(index: number) {
  const selectedUser = data.value?.d?.[index]
  if (!selectedUser) {
    return
  }

  const selection: UserSelection = {
    userId: selectedUser.account?.id,
    infoId: selectedUser.id,
    displayName: selectedUser.name || selectedUser.account?.nickName || '-',
  }

  mv.value = selection
  _userId.value = selection.userId
  _infoId.value = selection.infoId
  _displayName.value = selection.displayName
  showSearchPanel.value = false

  // 添加选择成功提示
  if (selection.displayName !== '-') {
    snackbar.success(`已选择用户：${selection.displayName}`)
  }
}

function routeToAddUserInfo() {
  void router.push('/a/user_info/add')
  snackbar.success('正在跳转到添加用户页面...')
}
</script>

<template>
<div
  ref="containerRef"
  class="user-search-container"
>
  <!-- 选中展示/快速搜索输入框 -->
  <VTextField
    v-model="searchInput"
    :placeholder="placeholder"
    :label="label"
    :errorMessages="errorMessages"
    :hint="hint"
    :persistentHint="persistentHint"
    clearable
    @click:clear="clearSearch"
    @update:modelValue="debouncedSearch"
    @focus="handleFocus"
    @blur="handleBlur"
  >
    <template #prepend>
      <VIcon>i-mdi-magnify</VIcon>
    </template>

    <template #append v-if="_userId || _infoId">
      <VChip
        color="primary"
        variant="flat"
        closable
        @click:close="clearSearch"
      >
        {{ _displayName }}
      </VChip>
    </template>
  </VTextField>

  <!-- 搜索结果面板 -->
  <VExpandTransition>
    <VCard
      v-if="showSearchPanel"
      class="search-panel mt-2"
      elevation="4"
      @mouseenter="handlePanelMouseEnter"
      @mouseleave="handlePanelMouseLeave"
    >
      <!-- 有搜索结果时显示列表 -->
      <VDataTable
        v-if="dataList.length > 0"
        :headers="headers"
        :items="dataList"
        :loading="loading"
        hover
        class="elevation-0"
        @click.stop
      >
        <template #item="{ item }">
          <tr>
            <td>
              <div class="d-flex align-center">
                <VAvatar
                  :color="item.account ? 'primary' : 'error'"
                  size="32"
                  class="mr-2"
                >
                  {{ item.name?.[0]?.toUpperCase() || 'N' }}
                </VAvatar>
                <div>
                  <div class="text-subtitle-2">
                    {{ item.name || '-' }}
                  </div>
                  <div class="text-medium-emphasis text-caption">
                    ID: {{ item.id }}
                  </div>
                </div>
              </div>
            </td>
            <td>
              <div v-if="item.account">
                <div class="text-subtitle-2">
                  {{ item.account.account }}
                </div>
                <div class="text-caption text-medium-emphasis">
                  {{ item.account.nickName || '-' }}
                </div>
              </div>
              <VChip
                v-else
                color="error"
                size="small"
                variant="flat"
              >
                未绑定账号
              </VChip>
            </td>
            <td>
              <div class="text-truncate" style="max-width: 200px">
                {{ item.address?.fullPath || '-' }}
              </div>
            </td>
            <td>
              {{ item.account?.lastLoginTime || '-' }}
            </td>
            <td>
              <VChip
                :color="item.isBlacked ? 'error' : 'success'"
                size="small"
                variant="flat"
              >
                {{ item.isBlacked ? '已拉黑' : '正常' }}
              </VChip>
            </td>
            <td>
              <VBtn
                variant="text"
                size="small"
                color="primary"
                @click="selectUser(dataList.indexOf(item))"
              >
                选择
              </VBtn>
            </td>
          </tr>
        </template>

        <template #bottom>
          <div class="v-data-table-footer">
            <VDivider />
            <div class="d-flex align-center justify-space-between pa-4">
              <div class="text-caption text-medium-emphasis">
                共 {{ data?.t || 0 }} 条记录
              </div>
              <PaginatorComp
                v-model:pq="pq"
                :pr="data"
                @change="handleSearch"
              />
            </div>
          </div>
        </template>
      </VDataTable>

      <!-- 无数据状态 -->
      <VCardText v-else class="pa-4">
        <VEmptyState
          v-if="loading"
          title="搜索中..."
          :loading="true"
        />
        <VEmptyState
          v-else-if="data?.d !== void 0"
          title="未找到用户"
          subtitle="您可以添加新用户信息"
        >
          <template #actions>
            <VBtn
              color="primary"
              block
              prependIcon="i-mdi-plus"
              @click="routeToAddUserInfo"
            >
              添加用户信息
            </VBtn>
          </template>
        </VEmptyState>
        <VEmptyState
          v-else
          title="请输入搜索内容"
          subtitle="支持姓名、手机号搜索"
        />
      </VCardText>
    </VCard>
  </VExpandTransition>
</div>
</template>

<style lang="scss" scoped>
.user-search-container {
  position: relative;
  width: 100%;

  .search-panel {
    position: absolute;
    width: 100%;
    z-index: 100;
    border-radius: 8px;
    max-height: 80vh;
    overflow-y: auto;
  }

  :deep(.items-per-page-select) {
    width: 80px;

    .v-field__input {
      min-height: 32px;
      padding-top: 0;
      padding-bottom: 0;
    }
  }

  :deep(.v-pagination) {
    .v-pagination__list {
      background-color: rgb(var(--v-theme-surface));
      border-radius: 4px;
    }
  }

  :deep(.v-data-table-footer) {
    cursor: default;
    user-select: none;
  }
}
</style>
