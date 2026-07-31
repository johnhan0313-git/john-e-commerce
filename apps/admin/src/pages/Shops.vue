<template>
  <div>
    <div class="toolbar">
      <h2>店铺列表</h2>
      <el-input
        v-model="merchantId"
        placeholder="卖家 ID 筛选"
        clearable
        style="width: 180px"
        @change="load"
      />
    </div>

    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column prop="id" label="店铺ID" width="100" />
      <el-table-column prop="merchantId" label="卖家ID" width="100" />
      <el-table-column prop="name" label="店名" min-width="160" />
      <el-table-column prop="statusLabel" label="状态" width="100" />
      <el-table-column prop="createdAt" label="创建时间" width="160" />
    </el-table>

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

<style scoped>
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.pager { margin-top: 16px; display: flex; justify-content: flex-end; }
h2 { margin: 0; }
</style>
