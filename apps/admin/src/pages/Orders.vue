<template>
  <div>
    <h2>订单管理</h2>
    <el-table v-loading="loading" :data="list" stripe>
      <el-table-column prop="id" label="ID" width="90" />
      <el-table-column prop="orderNo" label="订单号" min-width="160" />
      <el-table-column prop="statusLabel" label="状态" width="120">
        <template #default="{ row }">{{ row.statusLabel || row.status }}</template>
      </el-table-column>
      <el-table-column prop="payStatusLabel" label="支付" width="120">
        <template #default="{ row }">{{ row.payStatusLabel || row.payStatus }}</template>
      </el-table-column>
      <el-table-column prop="payAmount" label="应付" width="120" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
        </template>
      </el-table-column>
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

    <el-drawer v-model="detailVisible" title="订单详情" size="520px">
      <template v-if="detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.statusLabel || detail.status }}</el-descriptions-item>
          <el-descriptions-item label="支付">{{ detail.payStatusLabel || detail.payStatus }}</el-descriptions-item>
          <el-descriptions-item label="应付">¥{{ detail.payAmount }}</el-descriptions-item>
          <el-descriptions-item label="已付">¥{{ detail.paidAmount }}</el-descriptions-item>
          <el-descriptions-item label="订单组">{{ detail.orderGroupNo }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 20px 0 8px">改状态</h4>
        <el-space>
          <el-input-number v-model="nextStatus" :min="0" :max="99" />
          <el-button type="primary" @click="updateStatus">更新状态</el-button>
        </el-space>

        <h4 style="margin: 20px 0 8px">明细</h4>
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
const detailVisible = ref(false)
const detail = ref<Order | null>(null)
const nextStatus = ref(0)

async function load() {
  loading.value = true
  try {
    const res = await client.get('/order', {
      params: { page: page.value, size },
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
h2 { margin: 0 0 16px; }
.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
