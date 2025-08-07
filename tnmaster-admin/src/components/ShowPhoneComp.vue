<script setup lang="ts">
interface Props {
  phone?: string
}

const props = defineProps<Props>()

function formatPhone(phone?: string): string {
  if (!phone) {
    return '-'
  }
  return `${phone.slice(0, 3)}****${phone.slice(7)}`
}
</script>

<template>
<span class="show-phone">{{ formatPhone(props.phone) }}</span>
</template>

<style lang="scss" scoped>
.show-phone {
  font-family: monospace;
}
</style>
