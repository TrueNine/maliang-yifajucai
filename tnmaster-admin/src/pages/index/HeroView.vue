<script setup lang="ts">
const stats = [
  { value: '500+', label: '合作企业' },
  { value: '98%', label: '客户满意度' },
  { value: '10年+', label: '服务经验' },
]
</script>

<template>
<section id="home" class="relative overflow-hidden bg-[url('https://images.unsplash.com/photo-1497366811353-6870744d04b2?q=80&w=2069&auto=format&fit=crop&ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D')] bg-cover bg-center bg-no-repeat">
  <div class="absolute inset-0 z-1 from-[rgba(0,0,0,0.8)] to-[rgba(0,0,0,0.4)] bg-gradient-to-br" />
  <VContainer class="relative z-2">
    <VRow align="center" class="flex-column-reverse flex-md-row min-h-[85vh]">
      <VCol cols="12" md="8" class="animate-fade-in-up animate-duration-100 animate-ease-out">
        <!-- Hero 内容 -->
        <VChip color="primary" class="font-weight-medium mb-4">
          专业人力资源服务商
        </VChip>

        <h1 class="font-weight-bold text-h2 mb-6 text-white">
          赋能企业人才管理<br />
          <span class="text-primary-lighten-3">创造卓越价值</span>
        </h1>

        <p class="text-h6 mb-8 text-white opacity-80">
          我们致力于为企业提供全方位的人力资源解决方案<br />
          助力企业实现可持续发展
        </p>

        <!-- 按钮组 -->
        <div class="d-flex flex-wrap gap-4">
          <VBtn size="x-large" color="white" variant="flat" class="px-8 text-primary">
            <VIcon left class="i-mdi:rocket-launch mr-2" />开始咨询
          </VBtn>
          <VBtn size="x-large" variant="outlined" color="white" class="px-8">
            <VIcon left class="i-mdi:play-circle mr-2" />观看介绍
          </VBtn>
        </div>

        <!-- 数据统计 -->
        <div class="d-flex mt-16 flex-wrap items-center gap-8">
          <template v-for="(stat, index) in stats" :key="index">
            <div class="stat-item">
              <div class="font-weight-bold text-primary-lighten-3 text-h3 mb-2">
                {{ stat.value }}
              </div>
              <div class="text-body-1 text-white">
                {{ stat.label }}
              </div>
            </div>
            <VDivider
              v-if="index < 2"
              vertical
              inset
              length="40"
              class="mx-4 border-white/20 !hidden sm:!block"
            />
          </template>
        </div>
      </VCol>
    </VRow>
  </VContainer>
</section>
</template>
