<script setup lang="ts">
//
</script>

<template>
<div class="w-full">
  <VTextField label="手机号（国内 +86）" placeholder="仅支持中国大陆 +86 区段" />
  <VTextField label="短信验证码">
    <template #append-inner>
      <div>
        <VBtn color="primary" variant="text">
          获取验证码
        </VBtn>
      </div>
    </template>
  </VTextField>
</div>
</template>
