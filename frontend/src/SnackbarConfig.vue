<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { eventBus } from '@/common/event-bus'
import { useSnackbar } from '@/components'

const snackbar = useSnackbar()

function handleNotification(notification: Parameters<typeof eventBus.emitNotification>[0]) {
  snackbar.error(notification.message)
}

onMounted(() => {
  eventBus.onNotification(handleNotification)
})

onUnmounted(() => {
  eventBus.offNotification(handleNotification)
})
</script>

<template>
<div class="none" />
</template>
