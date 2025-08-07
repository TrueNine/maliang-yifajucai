<script setup lang="ts">
interface Props {
  title?: string
  desc?: string
  icon?: string
  iconColor?: string
  divider?: boolean
  route?: string
}

// @unocss-include
const props = withDefaults(defineProps<Props>(), {
  title: 'DEV',
  desc: '正在开发中',
  iconColor: 'c-yellow',
  icon: 'i-mdi:construction',
  divider: false,
})
const router = useRouter()
function routeTo() {
  if (!props.route) {
    return
  }
  setTimeout(() => {
    if (!props.route) {
      return
    }
    void router.push(props.route)
  }, 400)
}
</script>

<template>
<VCard @click="routeTo">
  <VCardTitle>
    <strong class="text-5">{{ props.title }}</strong>
  </VCardTitle>
  <VDivider v-if="props.divider" />
  <VCardText class="min-h-12">
    <slot name="desc">
      <div class="flex items-center justify-between space-x-1">
        <slot name="desc-icon">
          <template v-if="props.icon">
            <YIco class="inline-block text-12" :class="[props.icon, props.iconColor]" />
          </template>
        </slot>
        <slot name="desc-text">
          <p class="text-3">
            {{ props.desc }}
          </p>
        </slot>
      </div>
    </slot>
  </VCardText>
</VCard>
</template>
