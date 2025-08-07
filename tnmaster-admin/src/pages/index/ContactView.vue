<template>
<section id="contact" class="py-16">
  <VContainer>
    <h2 class="font-weight-bold text-h4 mb-12 text-center">
      联系我们
    </h2>
    <VRow justify="center">
      <VCol cols="12" md="8" lg="6">
        <VCard class="pa-6">
          <VForm>
            <VTextField label="姓名" variant="outlined" class="mb-4" />
            <VTextField label="邮箱" variant="outlined" class="mb-4" />
            <VTextField label="电话" variant="outlined" class="mb-4" />
            <VTextarea label="留言" variant="outlined" class="mb-6" />
            <VBtn color="primary" size="large" block>
              提交
            </VBtn>
          </VForm>
        </VCard>
      </VCol>
    </VRow>
  </VContainer>
</section>
</template>
