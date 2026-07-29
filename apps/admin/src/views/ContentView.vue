<template>
  <div>
    <h2>内容管理</h2>
    <h3>Banner列表</h3>
    <ul>
      <li v-for="b in banners" :key="b.id">{{ b.title }} - 排序:{{ b.sortOrder }} - {{ b.status === 1 ? '启用' : '禁用' }}</li>
    </ul>
    <h3>导航列表</h3>
    <ul>
      <li v-for="n in navigations" :key="n.id">{{ n.name }}</li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import client from '../api/client'

const banners = ref<any[]>([])
const navigations = ref<any[]>([])

onMounted(async () => {
  try {
    const res = await client.get('/content/banner')
    banners.value = res.data.data?.records || []
  } catch { /* ignore */ }
  try {
    const res = await client.get('/content/navigation/tree')
    navigations.value = res.data.data || []
  } catch { /* ignore */ }
})
</script>
