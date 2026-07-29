<template>
  <div>
    <h2>租户模块管理</h2>
    <ul>
      <li v-for="m in modules" :key="m.code">
        {{ m.code }} - {{ m.enabled ? '已开通' : '未开通' }}
        <button @click="toggle(m)">{{ m.enabled ? '关闭' : '开通' }}</button>
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import client from '../api/client'

const modules = ref<any[]>([])

onMounted(async () => {
  try {
    const res = await client.get('/tenant/modules/all')
    modules.value = res.data.data || []
  } catch { /* ignore */ }
})

async function toggle(m: any) {
  try {
    if (m.enabled) {
      await client.post(`/tenant/modules/${m.code}/disable`)
    } else {
      await client.post(`/tenant/modules/${m.code}/enable`)
    }
    m.enabled = !m.enabled
  } catch { /* ignore */ }
}
</script>
