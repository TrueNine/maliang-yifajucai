<script setup lang="ts">
interface Props {
  addressCode?: string
  fullPaths?: { code: string, fullPath?: string }[]
}

const props = withDefaults(defineProps<Props>(), {
  addressCode: void 0,
  fullPaths: () => [],
})

const showPath = computed(() => props.fullPaths.find((e) => e.code === props.addressCode)?.fullPath)
</script>

<template>
<slot name="default" :path="showPath">
  {{ showPath }}
</slot>
</template>
