<script setup lang="ts">
const sections = [
  {
    title: '易发聚才',
    content: '专业的人力资源服务提供商',
  },
  {
    title: '联系方式',
    content: [
      '电话：+86 186 7719 2235',
      '邮箱：18677192235@163.com',
    ],
  },
  {
    title: '关注我们',
    social: [
      { icon: 'i-mdi:wechat', color: '#07C160' },
      { icon: 'i-mdi:linkedin', color: '#0A66C2' },
      { icon: 'i-mdi:twitter', color: '#1DA1F2' },
    ],
  },
]
</script>

<template>
<VFooter class="pa-6">
  <VContainer>
    <VRow>
      <template v-for="(section, index) in sections" :key="index">
        <VCol
          cols="12" md="4" class="text-center" :class="[
            index === 2 ? 'text-md-right' : index === 1 ? 'text-center' : 'text-md-left',
          ]"
        >
          <h3 class="text-h6 mb-4">
            {{ section.title }}
          </h3>
          <template v-if="section.content">
            <template v-if="Array.isArray(section.content)">
              <p v-for="(item, i) in section.content" :key="i" class="text-body-2 text-medium-emphasis" :class="{ 'mb-2': i === 0 }">
                {{ item }}
              </p>
            </template>
            <p v-else class="text-body-2 text-medium-emphasis">
              {{ section.content }}
            </p>
          </template>
          <div v-if="section.social" class="d-flex justify-md-end justify-center gap-4">
            <VBtn
              v-for="(item, i) in section.social"
              :key="i"
              :icon="item.icon"
              variant="text"
              :style="{ color: item.color }"
            />
          </div>
        </VCol>
      </template>
    </VRow>
    <VDivider class="my-4" />
    <div class="text-body-2 text-medium-emphasis text-center">
      © {{ new Date().getFullYear() }} HR Solutions. All rights reserved.
    </div>
  </VContainer>
</VFooter>
</template>
