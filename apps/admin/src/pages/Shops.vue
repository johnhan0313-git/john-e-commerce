<template>
  <div>
    <div class="page-header">
      <div>
        <h2>店铺列表</h2>
        <p class="desc">审核开店申请（与主体入驻审核独立），按卖家 / 状态筛选</p>
      </div>
      <div class="page-header-actions">
        <el-select
          v-model="statusFilter"
          clearable
          placeholder="状态"
          style="width: 140px"
          @change="onFilterChange"
        >
          <el-option :value="0" label="待审核" />
          <el-option :value="1" label="营业" />
          <el-option :value="2" label="已拒绝" />
          <el-option :value="3" label="停用" />
        </el-select>
        <el-input
          v-model="merchantId"
          placeholder="卖家 ID"
          clearable
          style="width: 140px"
          @change="onFilterChange"
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
            <el-tag size="small" effect="light" :type="statusTagType(row.status)">
              {{ row.statusLabel || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button link type="primary" @click="audit(row.id, true)">通过</el-button>
              <el-button link type="danger" @click="audit(row.id, false)">拒绝</el-button>
            </template>
            <span v-else class="muted">—</span>
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
import { ElMessage } from 'element-plus'
import client from '@/api/client'
import type { PageResult, R } from '@/types'

interface ShopRow {
  id: number
  merchantId: number
  name: string
  status?: number
  statusLabel?: string
  createdAt?: number
}

const list = ref<ShopRow[]>([])
const loading = ref(false)
const page = ref(1)
const size = 20
const total = ref(0)
const merchantId = ref('')
const statusFilter = ref<number | undefined>(undefined)

function formatTime(ts?: number) {
  if (!ts) return '—'
  const d = new Date(ts)
  if (Number.isNaN(d.getTime())) return String(ts)
  return d.toLocaleString('zh-CN', { hour12: false })
}

function statusTagType(status?: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  if (status === 0) return 'warning'
  return 'info'
}

function onFilterChange() {
  page.value = 1
  load()
}

async function load() {
  loading.value = true
  try {
    const mid = merchantId.value.trim() ? Number(merchantId.value) : undefined
    const res = await client.get('/shop', {
      params: { page: page.value, size, merchantId: mid, status: statusFilter.value },
    }) as R<PageResult<ShopRow>>
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function audit(id: number, approved: boolean) {
  await client.put(`/shop/${id}/audit`, { approved })
  ElMessage.success(approved ? '已通过' : '已拒绝')
  await load()
}

onMounted(load)
</script>

<style scoped>
.muted {
  color: var(--color-muted);
  font-size: 13px;
}
</style>
