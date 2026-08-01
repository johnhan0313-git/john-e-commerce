<template>
  <div>
    <div class="page-header">
      <div>
        <h2>本店订单</h2>
        <p class="desc">查看订单并完成发货</p>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="list">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="orderNo" label="订单号" min-width="160" />
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

    <el-drawer v-model="detailVisible" title="订单详情 / 发货" size="560px">
      <template v-if="detail">
        <el-descriptions :column="1" class="detail-desc">
          <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="状态">{{ detail.statusLabel || detail.status }}</el-descriptions-item>
          <el-descriptions-item label="支付">{{ detail.payStatusLabel || detail.payStatus }}</el-descriptions-item>
          <el-descriptions-item label="应付">¥{{ detail.payAmount }}</el-descriptions-item>
        </el-descriptions>

        <h4 class="drawer-h">明细</h4>
        <el-table :data="detail.items || []" size="small">
          <el-table-column prop="skuName" label="SKU" />
          <el-table-column prop="quantity" label="数量" width="80" />
          <el-table-column prop="price" label="单价" width="100" />
        </el-table>

        <h4 class="drawer-h">发货</h4>
        <el-form label-position="top" class="ship-form">
          <el-form-item label="物流商">
            <el-input v-model="shipForm.provider" placeholder="如 SF" />
          </el-form-item>
          <el-form-item label="运单号">
            <el-input v-model="shipForm.trackingNo" />
          </el-form-item>
          <el-button type="primary" :loading="shipping" @click="ship">确认发货</el-button>
        </el-form>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
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
const shipping = ref(false)
const shipForm = reactive({ provider: '', trackingNo: '' })

function payTagType(label: unknown) {
  const s = String(label || '')
  if (s.includes('已') || s.includes('PAID') || s.includes('成功')) return 'success'
  if (s.includes('待') || s.includes('UNPAID')) return 'warning'
  return 'info'
}

async function load() {
  loading.value = true
  try {
    const res = await client.get('/shop/orders', {
      params: { page: page.value, size },
    }) as R<PageResult<Order>>
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function openDetail(id: number | string) {
  const res = await client.get(`/shop/orders/${id}`) as R<Order>
  detail.value = res.data
  shipForm.provider = ''
  shipForm.trackingNo = ''
  detailVisible.value = true
}

async function ship() {
  if (!detail.value) return
  const items = (detail.value.items || []).map((it) => ({
    orderItemId: it.id,
    qty: it.quantity,
  }))
  if (!items.length) {
    ElMessage.warning('订单无明细')
    return
  }
  shipping.value = true
  try {
    await client.post(`/shop/orders/${detail.value.id}/ship`, {
      orderId: detail.value.id,
      provider: shipForm.provider || undefined,
      trackingNo: shipForm.trackingNo || undefined,
      items,
    })
    ElMessage.success('已发货')
    detailVisible.value = false
    await load()
  } finally {
    shipping.value = false
  }
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

.ship-form {
  max-width: 360px;
}
</style>
