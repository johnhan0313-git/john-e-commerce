<template>
  <div>
    <div class="page-header">
      <div>
        <h2>仪表盘</h2>
        <p class="desc">租户经营概览 · GMV 与订单分布</p>
      </div>
    </div>

    <el-alert
      v-if="error"
      :title="error"
      type="warning"
      show-icon
      :closable="false"
      class="mb"
    />

    <div v-loading="loading" class="stat-grid">
      <div class="stat-card accent">
        <div class="label">GMV</div>
        <div class="value">¥{{ formatMoney(stats?.gmv) }}</div>
        <div class="hint">累计成交金额</div>
      </div>
      <div class="stat-card">
        <div class="label">订单状态数</div>
        <div class="value">{{ statusRows.length }}</div>
        <div class="hint">当前有数据的状态种类</div>
      </div>
      <div class="stat-card">
        <div class="label">热销 SKU</div>
        <div class="value">{{ (stats?.topSkus || []).length }}</div>
        <div class="hint">Top 列表条目</div>
      </div>
    </div>

    <div class="dash-grid">
      <div class="panel">
        <div class="panel-body">
          <h3 class="section-title">订单状态分布</h3>
          <el-empty v-if="!statusRows.length" description="暂无数据" :image-size="56" />
          <div v-else class="bar-list">
            <div v-for="row in statusRows" :key="row.status" class="bar-row">
              <span class="name" :title="row.label">{{ row.label }}</span>
              <div class="bar-track">
                <div class="bar-fill" :style="{ width: barWidth(row.count) }" />
              </div>
              <span class="count">{{ row.count }}</span>
            </div>
          </div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-pad">
          <h3 class="section-title">热销 SKU</h3>
        </div>
        <el-table :data="stats?.topSkus || []" empty-text="暂无数据">
          <el-table-column prop="skuId" label="SKU ID" width="120" />
          <el-table-column prop="skuName" label="名称" min-width="160" />
          <el-table-column prop="totalQty" label="销量" width="100" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import client from '@/api/client'
import type { R, StatsOverview } from '@/types'
import { formatCents } from '@john/fe-shared/money'

const ORDER_STATUS_LABEL: Record<string, string> = {
  '0': '待支付',
  '1': '已支付',
  '2': '已发货',
  '3': '已送达',
  '4': '已完成',
  '5': '已取消',
  '6': '退款中',
  '7': '已退款',
  '8': '部分发货',
}

const stats = ref<StatsOverview | null>(null)
const loading = ref(false)
const error = ref('')

const statusRows = computed(() =>
  Object.entries(stats.value?.orderCountByStatus || {}).map(([status, count]) => ({
    status,
    label: ORDER_STATUS_LABEL[status] || `状态 ${status}`,
    count,
  }))
)

const maxCount = computed(() =>
  Math.max(1, ...statusRows.value.map((r) => Number(r.count) || 0))
)

function barWidth(count: number | string) {
  const n = Number(count) || 0
  return `${Math.max(4, Math.round((n / maxCount.value) * 100))}%`
}

function formatMoney(v: unknown) {
  return formatCents(v as number)
}

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
.mb {
  margin-bottom: 16px;
}

.stat-card.accent {
  background:
    linear-gradient(135deg, rgba(14, 165, 233, 0.1), transparent 60%),
    var(--color-surface);
  border-color: rgba(14, 165, 233, 0.25);
}

.dash-grid {
  display: grid;
  grid-template-columns: minmax(260px, 0.9fr) minmax(320px, 1.3fr);
  gap: 16px;
}

@media (max-width: 960px) {
  .dash-grid {
    grid-template-columns: 1fr;
  }
}
</style>
