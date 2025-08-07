<script setup lang="ts">
//
const md = 3
const sm = 4
const defaultCol = 6
</script>

<template>
<VRow class="transition-all duration-300" dense>
  <VCol :cols="defaultCol" :sm="sm" :md="md">
    <VCard elevation="2" class="c-white from-red to-purple bg-gradient-to-r">
      <VCardTitle>123</VCardTitle>
    </VCard>
  </VCol>
  <VCol :cols="defaultCol" :sm="sm" :md="md">
    <VCard class="c-white from-black to-purple bg-gradient-to-r">
      <VCardTitle>123</VCardTitle>
    </VCard>
  </VCol>
</VRow>
</template>
