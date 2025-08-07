<script setup lang="ts">
const services = [
  {
    icon: 'i-mdi:account-search',
    title: '人才招聘',
    description: '为企业提供专业的人才招聘解决方案，精准匹配合适人选',
  },
  {
    icon: 'i-mdi:chart-timeline-variant',
    title: '人力资源管理',
    description: '提供全方位的人力资源管理服务，助力企业高效运营',
  },
  {
    icon: 'i-mdi:school',
    title: '培训发展',
    description: '定制化的员工培训计划，促进团队能力提升',
  },
  {
    icon: 'i-mdi:chart-box',
    title: '绩效管理',
    description: '科学的绩效评估体系，激励员工持续进步',
  },
]
</script>

<template>
<section id="services" class="py-16">
  <VContainer>
    <h2 class="font-weight-bold text-h4 mb-12 text-center">
      我们的服务
    </h2>
    <VRow>
      <VCol v-for="service in services" :key="service.title" cols="12" sm="6" md="3">
        <VCard class="h-100 pa-6 transition-shadow duration-100" variant="flat" hover>
          <div class="mb-4 text-6xl text-primary" :class="service.icon" />
          <h3 class="text-h6 font-weight-bold mb-4">
            {{ service.title }}
          </h3>
          <p class="text-body-1 text-medium-emphasis">
            {{ service.description }}
          </p>
        </VCard>
      </VCol>
    </VRow>
  </VContainer>
</section>
</template>
