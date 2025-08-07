<script setup lang="ts">
import logoSrc from '@/assets/logo.png'
</script>

<template>
<section id="about" class="py-16">
  <VContainer>
    <VRow justify="center" class="mb-8">
      <VCol cols="12" sm="8" md="6" lg="4" class="text-center">
        <VImg
          :src="logoSrc"
          class="mx-auto rounded-full transition-transform duration-100 hover:scale-105"
          :maxWidth="240"
          alt="HR Solutions Logo"
          :aspectRatio="1"
        />
      </VCol>
    </VRow>
    <VRow justify="center">
      <VCol cols="12" md="8" lg="6" class="text-center">
        <h2 class="font-weight-bold text-h4 mb-6">
          关于我们
        </h2>
        <p class="text-body-1 mb-4">
          作为专业的人力资源服务提供商，我们拥有丰富的行业经验和专业的服务团队。我们始终秉持"以人为本"的理念，为客户提供高质量的人力资源解决方案。
        </p>
        <p class="text-body-1 mb-8">
          我们的服务网络覆盖全国各地，与众多知名企业建立了长期稳定的合作关系。我们相信，通过专业的服务和持续的创新，能够帮助每一个企业实现人才管理的优化和升级。
        </p>
        <VBtn color="primary" variant="flat" size="large">
          了解更多
        </VBtn>
      </VCol>
    </VRow>
  </VContainer>
</section>
</template>
