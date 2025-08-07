<script setup lang="ts">
import type { asyncable, dynamic, Pq, Pr, RefId } from '@compose/types'
import type { ModelValueEmits, ModelValueProps } from '@compose/ui'
import { Pw } from '@compose/shared'
import { useSnackbar } from '@/components/TnSnackbar'
import { __internalSymbol } from '@/pages/a/sys/AclBar'

interface Props extends ModelValueProps<dynamic[]> {
  parent?: dynamic
  selected?: dynamic
  selectedId?: RefId
  pr?: Pr<dynamic>
  idKey?: string
  parentIdKey?: string
  docKey?: string
  parentDocKey?: string
  nameKey?: string
  parentNameKey?: string
  title?: string
  parentTitle?: string
  color?: string
  maxHeight?: number
  minHeight?: number
  getAllFn?: (pq: Pq, parent?: dynamic) => asyncable<Pr<dynamic> | undefined>
  getAllParentFn?: (pq: Pq, parent?: dynamic) => asyncable<Pr<dynamic> | undefined>
  putByIdFn?: (it: dynamic) => asyncable
  postFn?: (it: dynamic) => asyncable
  linkFn?: (child: dynamic, parent: dynamic) => asyncable<void>
  unlinkFn?: (child: dynamic, parent: dynamic) => asyncable<void>
  deleteByIdFn?: (id?: RefId) => asyncable<void>
}

interface Emits extends ModelValueEmits<dynamic[]> {
  'update:pq': [pq: Pq]
  'update:parent': [it: dynamic]
  'update:selected': [it: dynamic]
  'update:selectedId': [it: dynamic]
}

const props = withDefaults(defineProps<Props>(), {
  idKey: 'id',
  parentIdKey: 'id',
  postFn: void 0,
  linkFn: void 0,
  nameKey: 'name',
  pr: void 0,
  getAllParentFn: void 0,
  getAllFn: void 0,
  selectedId: void 0,
  parentNameKey: 'name',
  docKey: 'doc',
  parentDocKey: 'doc',
  selected: () => ({}),
  unlinkFn: void 0,
  title: '类型',
  deleteByIdFn: void 0,
  putByIdFn: void 0,
  parentTitle: '',
  color: 'primary',
  maxHeight: 340,
  minHeight: 340,
  modelValue: () => [],
  parent: () => ({ _isDefaultInternalPropsDefine: __internalSymbol }),
})
const emits = defineEmits<Emits>()

const openedShowListItems = shallowRef<dynamic[]>([])

const _parent = useVModel(props, 'parent', emits)
const hasParent = shallowRef(_parent.value?._isDefaultInternalPropsDefine !== __internalSymbol && _parent.value != null && !!Object.keys(_parent.value).length)

const _showItems = useVModel(props, 'modelValue', emits, { passive: true })
const _allItems = ref<dynamic[]>([])

const _selected = useVModel(props, 'selected', emits, { passive: true })
const _selectedId = useVModel(props, 'selectedId', emits, { passive: true })

const _showPq = ref<Pq>({ ...Pw.DEFAULT_MAX })
const _allPq = shallowRef<Pq>({ ...Pw.DEFAULT_MAX })
const _allPr = ref<Pr<dynamic>>(Pw.empty())
const _showPr = ref<Pr<dynamic>>(Pw.empty())

async function changeAllPr(pq: Pq) {
  _allPq.value = pq
  if (props.getAllFn) {
    _allPr.value = (await props.getAllFn(pq, hasParent.value ? _parent.value : void 0)) ?? Pw.empty()
    _allItems.value = _allPr.value.d ?? []
  }
}

async function changeShowPr(pq: Pq) {
  _showPq.value = pq
  if (hasParent.value && props.getAllParentFn) {
    _showPr.value = (await props.getAllParentFn(pq, _parent.value)) ?? Pw.empty()
    _showItems.value = _showPr.value.d ?? []
  }
}
async function getList() {
  await changeShowPr({ ...Pw.DEFAULT_MAX })
  await changeAllPr({ ...Pw.DEFAULT_MAX })
}

onMounted(getList)

function clear() {
  _showItems.value = []
  _allItems.value = []
  _selected.value = void 0 as unknown as dynamic
  _selectedId.value = void 0 as unknown as dynamic
}

watch(openedShowListItems, (v) => {
  _selected.value = v[0]
  _selectedId.value = _selected.value ? _selected.value[props.idKey] : void 0
})

const popup = useSnackbar()
const postItemValue = ref<dynamic>({})

function postItem(active: Ref<boolean>) {
  void popup?.loading(async () => {
    if (props.postFn && postItemValue.value?.name) {
      await props.postFn(postItemValue.value)
      await getList()
      active.value = false
    }
  })
}

const putItemValue = ref<dynamic>({})
const putDialogActive = ref(false)

function activeAndReadyDialog(idx: number) {
  putDialogActive.value = true
  putItemValue.value = _allItems.value[idx]
}

function putById() {
  void popup?.loading(async () => {
    if (putItemValue.value?.id && props.putByIdFn) {
      await props.putByIdFn(putItemValue.value)
      await getList()
      putDialogActive.value = false
    }
  })
}

function deleteById(id: RefId) {
  void popup?.loading(async () => {
    if (!id || !props.deleteByIdFn) {
      return
    }
    await props.deleteByIdFn(id as string)
    _allPq.value = { ...Pw.DEFAULT_MAX }
    await getList()
  })
}

function isDisabledState(id: RefId) {
  if (!id) {
    return true
  }
  return id.length <= 3
}

// window tab
const currentTab = ref('allList')
watch(_parent, async (v) => {
  if (!v) {
    clear()
    return
  }
  hasParent.value = v._isDefaultInternalPropsDefine !== __internalSymbol
  _allPq.value = { ...Pw.DEFAULT_MAX }
  _showPq.value = { ...Pw.DEFAULT_MAX }
  clear()
  currentTab.value = hasParent.value ? 'showList' : 'allList'
  await getList()
})

function link(child: dynamic, parent: dynamic) {
  void popup?.loading(async () => {
    if (props.linkFn) {
      await props.linkFn(child, parent)
    }
    currentTab.value = 'showList'
    await getList()
  })
}

function unlink(child: dynamic, parent: dynamic) {
  void popup?.loading(async () => {
    if (props.unlinkFn) {
      await props.unlinkFn(child, parent)
    }
    await getList()
  })
}
</script>

<template>
<VCard :maxHeight="maxHeight + 200" :minHeight="minHeight + 130">
  <div class="f-y-c p1 space-x-8">
    <h3>{{ _parent?.[parentNameKey] ? `${_parent[parentNameKey]} > ` : '' }}{{ title }}列表</h3>

    <slot name="bar" />
    <VSpacer />
    <VTabs v-model="currentTab" :color="color">
      <VTab v-if="hasParent" value="showList">
        {{ _parent?.[parentNameKey] ? `${_parent[parentNameKey]} > ` : '' }} {{ title }}
      </VTab>
      <VTab value="allList">
        全部{{ title }}
      </VTab>
    </VTabs>
  </div>

  <div class="p1">
    <VWindow v-model="currentTab">
      <VWindowItem value="showList">
        <VList v-model="_showItems" v-model:selected="openedShowListItems" class="scrollbar-list" :maxHeight="maxHeight" :minHeght="minHeight" :color="color">
          <VListItem v-for="(it, i) in _showItems" :key="i" class="mb-2" :color="color" :value="it">
            <VListItemTitle>
              <div class="f-y-c space-x-1">
                <slot name="label" :title="title" :item="it">
                  <VChip :color="color">
                    <div class="f-y-c">
                      <slot name="icon">
                        <YIco class="i-mdi:account-group text-4" />
                      </slot>
                      <div>{{ title }}</div>
                    </div>
                  </VChip>
                </slot>
                <slot name="name" :item="it">
                  <div>{{ it[nameKey] }}</div>
                </slot>
              </div>
            </VListItemTitle>

            <VListItemSubtitle>
              <slot name="subtitle" :item="it">
                {{ it[docKey] || '-' }}
              </slot>
            </VListItemSubtitle>
            <template #append>
              <VBtn
                v-if="!!props.unlinkFn && hasParent"
                :disabled="isDisabledState(it[idKey])"
                variant="text"
                color="error"
                @click="() => unlink(it, _parent!)"
              >
                <YIco class="i-mdi:link-variant-minus" />
              </VBtn>
            </template>
          </VListItem>
          <VListItem v-if="!_showItems?.length">
            <VEmptyState :title="`${title} 为空`">
              <template #media>
                <YIco class="i-mdi:border-none-variant text-16" />
              </template>
            </VEmptyState>
          </VListItem>
        </VList>
        <YPager v-if="_showItems?.length" v-model="_showPq" v-model:pr="_showPr" @change="changeShowPr" />
      </VWindowItem>

      <!-- 所有列表 -->
      <VWindowItem value="allList">
        <VList v-model="_allItems" v-model:selected="openedShowListItems" class="scrollbar-list" :maxHeight="maxHeight" :minHeght="minHeight" :color="color">
          <VListItem v-for="(it, i) in _allItems" :key="i" class="mb-2" :color="color" :value="it">
            <VListItemTitle>
              <div class="f-y-c space-x-1">
                <slot name="label" :title="title" :item="it">
                  <VChip :color="color">
                    <div class="f-y-c">
                      <slot name="icon">
                        <YIco class="i-mdi:account-group text-4" />
                      </slot>
                      <div>{{ title }}</div>
                    </div>
                  </VChip>
                </slot>
                <slot name="name" :item="it">
                  <div>{{ it[nameKey] }}</div>
                </slot>
              </div>
            </VListItemTitle>
            <VListItemSubtitle>
              <slot name="subtitle" :item="it">
                {{ it[docKey] || '-' }}
              </slot>
            </VListItemSubtitle>
            <template #append>
              <VBtn v-if="!!props.linkFn && hasParent" variant="text" :color="color" @click="() => link(it, _parent)">
                <YIco class="i-mdi:link-variant-plus" />
              </VBtn>
              <VBtn v-if="!!props.putByIdFn" :disabled="isDisabledState(it[idKey])" :color="color" variant="plain" @click="() => activeAndReadyDialog(i)">
                <YIco class="i-mdi:application-edit" />
              </VBtn>
              <VBtn
                v-if="!!props.deleteByIdFn"
                :disabled="isDisabledState(it[idKey] as unknown as RefId)"
                color="error"
                variant="plain"
                @click="() => deleteById(it[idKey])"
              >
                <YIco class="i-mdi:delete" />
              </VBtn>
            </template>
          </VListItem>
        </VList>

        <div>
          <VDialog v-if="!!props.postFn && currentTab === `allList`" width="auto" minWidth="300">
            <template #activator="{ props: p }">
              <VBtn v-bind="p" variant="text" :color="color">
                <YIco class="i-mdi:plus text-6" />
                添加{{ title }}
              </VBtn>
            </template>
            <template #default="{ isActive }">
              <VCard>
                <VCardTitle>添加{{ title }}</VCardTitle>
                <VCardText>
                  <VTextField v-model="postItemValue[nameKey]" :label="`${title}名称`" />
                  <VTextarea v-model="postItemValue[docKey]" :label="`${title}描述`" />
                </VCardText>
                <VCardActions>
                  <VBtn block :color="color" @click="postItem(isActive)">
                    添加{{ title }}
                  </VBtn>
                </VCardActions>
              </VCard>
            </template>
          </VDialog>
        </div>
        <YPager v-if="_allItems?.length" v-model="_allPq" v-model:pr="_allPr" @change="changeAllPr" />
      </VWindowItem>
    </VWindow>
  </div>
</VCard>

<VDialog v-if="props.putByIdFn" v-model="putDialogActive" width="auto" minWidth="300">
  <VCard>
    <VCardTitle>修改 {{ title }} 中的 {{ putItemValue[nameKey] }}</VCardTitle>
    <div p1>
      <VTextField v-model="putItemValue[nameKey]" :label="`${title}名称`" />
      <VTextarea v-model="putItemValue[docKey]" :label="`${title}描述`" />
    </div>
    <VCardActions>
      <VBtn block :color="color" @click="putById()">
        修改
      </VBtn>
    </VCardActions>
  </VCard>
</VDialog>
</template>

<style scoped lang="scss">
.scrollbar-list {
  scrollbar-width: thin;
}
</style>
