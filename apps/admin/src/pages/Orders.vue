<template>
  <div>
    <div class="page-header">
      <div>
        <h2>订单管理</h2>
        <p class="desc">全域订单查询与状态维护，可按卖家、店铺筛选</p>
      </div>
      <div class="page-header-actions">
        <el-input
          v-model="merchantIdFilter"
          placeholder="卖家 ID"
          clearable
          style="width: 140px"
          @change="onFilterChange"
        />
        <el-input
          v-model="shopIdFilter"
          placeholder="店铺 ID"
          clearable
          style="width: 140px"
          @change="onFilterChange"
        />
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="list">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="orderNo" label="订单号" min-width="160" />
        <el-table-column prop="merchantId" label="卖家" width="90" />
        <el-table-column prop="shopId" label="店铺" width="90" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag size="small" effect="light">{{ row.statusLabel || row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="支付" width="120">
          <template #default="{ row }">
            <el-tag
              size="small"
              effect="light"
              :type="payTagType(row.payStatusLabel || row.payStatus)"
            >
              {{ row.payStatusLabel || row.payStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="应付" width="120">
          <template #default="{ row }">¥{{ row.payAmount }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
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

    <el-drawer v-model="detailVisible" title="订单详情" size="520px">
      <template v-if="detail">
        <el-descriptions :column="1" class="detail-desc">
          <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.statusLabel || detail.status }}</el-descriptions-item>
          <el-descriptions-item label="支付">{{ detail.payStatusLabel || detail.payStatus }}</el-descriptions-item>
          <el-descriptions-item label="应付">¥{{ detail.payAmount }}</el-descriptions-item>
          <el-descriptions-item label="已付">¥{{ detail.paidAmount }}</el-descriptions-item>
          <el-descriptions-item label="订单组">{{ detail.orderGroupNo }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="drawer-h">改状态</h4>
        <el-space>
          <el-input-number v-model="nextStatus" :min="0" :max="99" />
          <el-button type="primary" @click="updateStatus">更新状态</el-button>
        </el-space>

        <h4 class="drawer-h">明细</h4>
        <el-table :data="detail.items || []" size="small">
          <el-table-column prop="skuName" label="SKU" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="price" label="单价" width="100" />
        </el-table>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import client from '@/api/client'
import type { Order, PageResult, R } from '@/types'

const list = ref<Order[]>([])
const loading = ref(false)
const page = ref(1)
const size = 20
const total = ref(0)
const merchantIdFilter = ref('')
const shopIdFilter = ref('')
const detailVisible = ref(false)
const detail = ref<Order | null>(null)
const nextStatus = ref(0)

function payTagType(label: unknown) {
  const s = String(label || '')
  if (s.includes('已') || s.includes('PAID') || s.includes('成功')) return 'success'
  if (s.includes('待') || s.includes('UNPAID')) return 'warning'
  return 'info'
}

function onFilterChange() {
  page.value = 1
  load()
}

async function load() {
  loading.value = true
  try {
    const res = await client.get('/order', {
      params: {
        page: page.value,
        size,
        buyerScoped: false,
        merchantId: merchantIdFilter.value.trim() ? Number(merchantIdFilter.value) : undefined,
        shopId: shopIdFilter.value.trim() ? Number(shopIdFilter.value) : undefined,
      },
    }) as R<PageResult<Order>>
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function openDetail(id: number) {
  const res = await client.get(`/order/${id}`) as R<Order>
  detail.value = res.data
  nextStatus.value = res.data?.status ?? 0
  detailVisible.value = true
}

async function updateStatus() {
  if (!detail.value) return
  await client.put(`/order/${detail.value.id}/status`, null, {
    params: { status: nextStatus.value },
  })
  ElMessage.success('状态已更新')
  await openDetail(detail.value.id)
  await load()
}

onMounted(load)
</script>

<style scoped>
.drawer-h {
  margin: 24px 0 12px;
  font-size: 14px;
  font-weight: 600;
}

.detail-desc {
  background: #f8fafc;
  border-radius: 10px;
  padding: 12px 16px;
}
</style>
