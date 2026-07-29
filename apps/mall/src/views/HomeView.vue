<template>
  <div>
    <h2>首页</h2>
    <div v-for="b in banners" :key="b.id">
      <a :href="b.linkUrl"><img :src="b.imageUrl" :alt="b.title" style="max-width:300px" /></a>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import client from '../api/client'

const banners = ref<any[]>([])

onMounted(async () => {
  const res = await client.get('/public/content/banner')
  banners.value = res.data.data || []
})
</script>
