<template>
  <div>
    <h2>仪表盘</h2>
    <el-alert
      v-if="error"
      :title="error"
      type="warning"
      show-icon
      :closable="false"
      class="mb"
    />
    <el-row v-loading="loading" :gutter="16">
      <el-col :span="8">
        <el-card shadow="hover">
          <el-statistic title="GMV" :value="Number(stats?.gmv || 0)" :precision="2" prefix="¥" />
        </el-card>
      </el-col>
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>订单状态分布</template>
          <el-empty v-if="!statusRows.length" description="暂无数据" :image-size="60" />
          <el-table v-else :data="statusRows" size="small">
            <el-table-column prop="status" label="状态" />
            <el-table-column prop="count" label="数量" width="120" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="mt" shadow="never">
      <template #header>热销 SKU</template>
      <el-table :data="stats?.topSkus || []" empty-text="暂无数据">
        <el-table-column prop="skuId" label="SKU ID" width="120" />
        <el-table-column prop="skuName" label="名称" />
        <el-table-column prop="totalQty" label="销量" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import client from '@/api/client'
import type { R, StatsOverview } from '@/types'

const stats = ref<StatsOverview | null>(null)
const loading = ref(false)
const error = ref('')

const statusRows = computed(() =>
  Object.entries(stats.value?.orderCountByStatus || {}).map(([status, count]) => ({
    status,
    count,
  }))
)

onMounted(async () => {
  loading.value = true
  try {
    const res = await client.get('/statistics/overview') as R<StatsOverview>
    stats.value = res.data
  } catch (e: any) {
    error.value = e.response?.data?.message || '统计模块可能未开通'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.mb { margin-bottom: 16px; }
.mt { margin-top: 16px; }
h2 { margin: 0 0 16px; }
</style>
