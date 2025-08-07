<script setup lang="ts">
import { z } from 'zod'
import { api } from '@/api'

interface Emits {
  ok: []
}
const emit = defineEmits<Emits>()

const schema = z.object({
  newNickName: z.string({ message: '呢称不能为空' }).min(2, '呢称过短').max(52, '呢称过长'),
})
type _NickName = z.infer<typeof schema>
const closeFn = ref<() => void>()
async function submit(values: _NickName) {
  closeFn.value?.()
  await api.accountApi.patchNickNameAsMe(values)
  emit('ok')
}
</script>

<template>
<YForm class="wh-full" :schema="schema" @submit="submit">
  <FullScreenConfirmLayout>
    <YField name="newNickName" label="新的呢称">
      <VTextField />
    </YField>
    <template #ok="{ onClose }">
      <VBtn type="submit" variant="flat" color="primary" block @click="closeFn = onClose">
        提交
      </VBtn>
    </template>
  </FullScreenConfirmLayout>
</YForm>
</template>
