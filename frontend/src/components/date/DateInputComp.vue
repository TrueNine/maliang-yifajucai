<script setup lang="ts">
import type { timestamp } from '@compose/types'
import { dateMillis, formatDate, formatTime, getOffsetMillis, timeMillis } from '@compose/external/dayjs'

type Type = 'date' | 'time' | 'datetime'

export interface Props {
  type?: Type
  date?: timestamp | string
  time?: timestamp | string
  datetime?: timestamp | string
  label?: string
  placeholder?: string
}

export type Emits = (e: 'update:date' | 'update:time' | 'update:datetime', v?: timestamp | string) => void

const props = withDefaults(defineProps<Props>(), {
  type: 'datetime',
  date: void 0,
  time: void 0,
  datetime: void 0,
  label: void 0,
  placeholder: void 0,
})
const emits = defineEmits<Emits>()

const _date = useVModel(props, 'date', emits, { passive: true })
const _time = useVModel(props, 'time', emits, { passive: true })
const _datetime = useVModel(props, 'datetime', emits, { passive: true })
const showDate = computed(() => props.type === 'date' || props.type === 'datetime')
const showTime = computed(() => props.type === 'time' || props.type === 'datetime')

const isDatetime = computed(() => props.type === 'datetime' && showDate.value && showTime.value)

watch([() => _date.value, () => _time.value], ([d, t]) => {
  if (isDatetime.value && d !== void 0 && t !== void 0) {
    _datetime.value = Number(d) + Number(t)
  } else if (props.type === 'date' && d !== void 0) {
    _datetime.value = d
  } else if (props.type === 'time' && t !== void 0) {
    _datetime.value = t
  } else {
    _datetime.value = void 0
  }
})

const _updateDate = computed({
  get: () => {
    return _date.value ? formatDate(_date.value) : void 0
  },
  set: (v) => {
    if (v) {
      _date.value = dateMillis(v)
    } else {
      _date.value = void 0
    }
  },
})

const _updateTime = computed({
  get: () => {
    return _time.value ? formatTime(Number(_time.value) - Number(getOffsetMillis()), { format: 'HH:mm' }) : void 0
  },
  set: (v) => {
    _time.value = v ? timeMillis(v, { format: 'HH:mm' }) : void 0
  },
})

function dateFocus() {
  _date.value = void 0
}

function timeFocus() {
  _time.value = void 0
}
</script>

<template>
<div flex rounded-0 space-x-1>
  <VTextField v-if="showDate" v-model="_updateDate" clearable :label="props.label" :placeholder="props.placeholder" type="date" @focus="dateFocus" />
  <VTextField v-if="showTime" v-model="_updateTime" clearable :label="props.label" :placeholder="props.placeholder" type="time" @focus="timeFocus" />
</div>
</template>
