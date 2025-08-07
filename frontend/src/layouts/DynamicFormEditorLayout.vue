<script setup lang="ts"></script>

<template>
<VRow dense>
  <VCol :cols="2">
    <VRow noGutters>
      <!-- 小组件区 -->
      <VCol :cols="12">
        <slot name="components" />
      </VCol>
      <!-- 小组件操作区 -->
      <VCol class="min-h-80" :cols="12">
        <slot name="componentsFooterPanel" />
      </VCol>
    </VRow>
  </VCol>
  <!-- 拖拽目标区域 -->
  <VCol :cols="8">
    <VCard>
      <slot name="main" />
    </VCard>
  </VCol>
  <!-- 组件选项区 -->
  <VCol :cols="2">
    <div min-h-70vh flex-col justify-between>
      <slot name="componentsOptions" />
    </div>
  </VCol>
</VRow>
</template>
