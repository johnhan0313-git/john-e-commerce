<template>
  <div>
    <h1>模块配置</h1>
    <div v-for="m in modules" :key="m.moduleCode">
      <span>{{ m.moduleName }} ({{ m.moduleCode }})</span>
      <span> — {{ m.status === 1 ? '已开通' : '未开通' }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import client from '@/api/client'

const modules = ref<any[]>([])
onMounted(async () => {
  const res: any = await client.get('/tenant/modules')
  modules.value = res.data || []
})
</script>
