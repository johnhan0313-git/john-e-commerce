<template>
  <div>
    <h2>营销活动</h2>
    <ul>
      <li v-for="a in activities" :key="a.id">{{ a.name }} - {{ a.type }} - {{ a.status }}</li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import client from '../api/client'

const activities = ref<any[]>([])

onMounted(async () => {
  try {
    const res = await client.get('/activity')
    activities.value = res.data.data?.records || []
  } catch { /* ignore */ }
})
</script>
