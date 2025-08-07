<script setup lang="ts">
interface Props {
  zIndex?: number
  modelValue?: boolean
  dialog?: boolean
  delay?: number
}
interface Emits {
  modelValue: [boolean]
  ok: []
  cancel: []
}
const props = withDefaults(defineProps<Props>(), {
  zIndex: 3,
  modelValue: false,
  dialog: false,
  delay: void 0,
})
const emit = defineEmits<Emits>()
const accept = useVModel(props, 'modelValue', emit, { passive: true })
const dialogShow = useVModel(props, 'dialog', emit, { passive: true })

const isDelay = ref(false)
const delaySeconds = ref(0)
let intervalId: number | null = null

watch(dialogShow, (show) => {
  if (!show) {
    if (intervalId) {
      clearInterval(intervalId)
      intervalId = null
    }
    return
  }
  if (props.delay === void 0 || props.delay <= 0) {
    return
  }
  isDelay.value = true
  delaySeconds.value = Math.ceil(props.delay / 1000)
  intervalId = setInterval(() => {
    delaySeconds.value = (delaySeconds.value - 0.1)
    if (delaySeconds.value >= 0) {
      return
    }
    isDelay.value = false
    if (!intervalId) {
      return
    }
    clearInterval(intervalId as number)
    intervalId = null
  }, 100) as unknown as number
})
</script>

<template>
<VDialog v-model="dialogShow" :zIndex="props.zIndex">
  <VCard>
    <VCardTitle>郑重提示</VCardTitle>
    <VCardText>
      <slot name="default">
        <p class="indent-8">
          为有效维护平台的安全秩序，保障所有用户的合法权益，请您在上传个人证件信息时，务必秉持
          <b class="c-red">审慎态度</b> ，对各项信息进行
          <b class="c-red">仔细核对</b>。上传成功后，所提交的证件信息将与您当前使用的账号进行绑定操作，绑定完成后，
          <b class="c-red">
            相关信息无法进行更改
          </b>
          。
          同时，严格禁止冒用或顶替使用他人证件的行为。此类行为不仅违反平台使用规定，更可能触犯法律法规。一旦经平台监测或被举报证实存在上述违规行为，平台将立即对涉事账号执行封禁处理，涉事用户终身禁止使用本平台服务。此外，平台会将相关线索和证据一并移交至公安等有关执法单位，由执法单位依法进行处理
          。
        </p>
      </slot>
      <VDivider class="my-4" />
      <VCheckbox v-model="accept" color="primary" label="我明确知晓冒用他人证件所需承担的法律后果" />
    </VCardText>
    <VCardActions>
      <VSpacer />
      <VBtn @click="() => emit(`cancel`)">
        取消
      </VBtn>
      <VBtn color="primary" :disabled="!accept || isDelay" @click="() => emit(`ok`)">
        确定
        <span v-if="isDelay">
          （{{ Math.ceil(delaySeconds) }}）
        </span>
      </VBtn>
    </VCardActions>
  </VCard>
</VDialog>
</template>
