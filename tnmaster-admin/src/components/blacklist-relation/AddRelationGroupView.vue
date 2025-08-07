<script setup lang="ts">
import type { i32, RefId } from '@compose/types'
import type { BlackListRelation } from './AddRelationGroup.ts'
import { RelationItemTyping_OPTIONS, RelationTyping_OPTIONS } from '@/api'

interface UserSelection {
  userId?: RefId
  infoId?: RefId
  displayName?: string
}

export interface Props {
  modelValue?: BlackListRelation[]
}

export type Emits = (e: 'update:modelValue', v?: BlackListRelation[]) => void

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => [],
})
const emits = defineEmits<Emits>()
const g = useVModel(props, 'modelValue', emits, { passive: true })

function addItem() {
  if (!g.value.length) {
    g.value = []
  }
  g.value = [...g.value, {}]
}

function updateUsr(idx: i32, e?: UserSelection) {
  g.value[idx].refId = e?.userId
  g.value[idx].userInfoId = e?.infoId
  g.value[idx].reItemType = 'USER'
}

function removeItem(idx: i32) {
  g.value.splice(idx, 1)
}
</script>

<template>
<template v-if="g">
  <section v-for="(_, idx) in g" :key="idx">
    <div class="h-12" />
    <VBtn variant="text" color="error" @click="removeItem(idx)">
      <YIco class="i-mdi:delete" />
      删除
    </VBtn>
    <UserSearchView @update:modelValue="e => updateUsr(idx, e as UserSelection)" />
    <div class="f-y-c flex space-x-1">
      <VSelect v-model="g[idx].reType" label="与此次事件关系" itemTitle="k" itemValue="v" :items="RelationTyping_OPTIONS" />
      <VSelect v-model="g[idx].reItemType" label="关系人类型" itemTitle="k" itemValue="v" :items="RelationItemTyping_OPTIONS" />
    </div>
    <VTextarea v-model="g[idx].eventDoc" label="与此次事件关系的描述" />
  </section>
</template>
<VBtn :block="true" variant="text" color="primary" @click="addItem">
  <YIco class="i-mdi:add" />
  添加与此次事件相关人员
</VBtn>
</template>
