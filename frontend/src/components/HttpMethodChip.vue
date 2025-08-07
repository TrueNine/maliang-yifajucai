<script setup lang="ts">
interface Props {
  method?: string
}
const { method = 'NON' } = defineProps<Props>()
const httpMethod = computed(() => {
  switch (method.toUpperCase()) {
    case 'GET':
      return 'GET'
    case 'PUT':
      return 'PUT'
    case 'HEAD':
      return 'HEAD'
    case 'POST':
      return 'POST'
    case 'DELETE':
      return 'DEL'
    case 'PATCH':
      return 'PAT'
    case 'OPTIONS':
      return 'OPT'
    default:
      return 'NON'
  }
})
const color = computed(() => {
  switch (httpMethod.value) {
    case 'GET':
      return 'green'
    case 'POST':
      return 'orange'
    case 'PUT':
      return 'yellow'
    case 'DEL':
      return 'red'
    case 'PAT':
      return 'pink'
    default:
      return 'info'
  }
})
</script>

<template>
<VChip :color="color">
  {{ httpMethod }}
</VChip>
</template>
