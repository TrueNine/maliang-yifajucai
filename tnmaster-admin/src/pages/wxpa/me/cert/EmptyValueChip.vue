<script setup lang="ts">
interface Props {
  name?: string
}

const props = defineProps<Props>()
</script>

<template>
<template v-if="name">
  {{ props.name }}
</template>
<VChip v-else disabled color="error" size="mini">
  未填写
</VChip>
</template>
