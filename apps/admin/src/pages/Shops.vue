<template>
  <div>
    <div class="page-header">
      <div>
        <h2>店铺列表</h2>
        <p class="desc">查看租户下全部店铺，可按卖家 ID 筛选</p>
      </div>
      <div class="page-header-actions">
        <el-input
          v-model="merchantId"
          placeholder="卖家 ID 筛选"
          clearable
          style="width: 180px"
          @change="load"
        />
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="list">
        <el-table-column prop="id" label="店铺ID" width="100" />
        <el-table-column prop="merchantId" label="卖家ID" width="100" />
        <el-table-column prop="name" label="店名" min-width="160" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag size="small" effect="light" type="success">
              {{ row.statusLabel || '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="pager">
      <el-pagination
        v-model:current-page="page"
        :page-size="size"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="load"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import client from '@/api/client'
import type { PageResult, R } from '@/types'

interface ShopRow {
  id: number
  merchantId: number
  name: string
  statusLabel?: string
  createdAt?: number
}

const list = ref<ShopRow[]>([])
const loading = ref(false)
const page = ref(1)
const size = 20
const total = ref(0)
const merchantId = ref('')

function formatTime(ts?: number) {
  if (!ts) return '—'
  const d = new Date(ts)
  if (Number.isNaN(d.getTime())) return String(ts)
  return d.toLocaleString('zh-CN', { hour12: false })
}

async function load() {
  loading.value = true
  try {
    const mid = merchantId.value.trim() ? Number(merchantId.value) : undefined
    const res = await client.get('/shop', {
      params: { page: page.value, size, merchantId: mid },
    }) as R<PageResult<ShopRow>>
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>
