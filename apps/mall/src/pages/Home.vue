<template>
  <div>
    <h1>首页</h1>
    <div v-for="b in banners" :key="b.id">
      <img :src="b.imageUrl" :alt="b.title" style="max-width:300px" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import client from '@/api/client'

const banners = ref<any[]>([])
onMounted(async () => {
  const res: any = await client.get('/public/content/banner')
  banners.value = res.data || []
})
</script>
