<template>
  <div>
    <div class="page-header">
      <div>
        <h2>店铺结算</h2>
        <p class="desc">按店铺查看结算单与账单，可建账 / 轧差</p>
      </div>
      <div class="page-header-actions">
        <el-input
          v-model="shopIdFilter"
          placeholder="店铺 ID"
          clearable
          style="width: 140px"
          @change="onFilterChange"
        />
        <el-input
          v-model="merchantIdFilter"
          placeholder="卖家 ID"
          clearable
          style="width: 140px"
          @change="onFilterChange"
        />
        <el-button type="primary" :disabled="!shopIdFilter.trim()" @click="createBill">
          按店建账
        </el-button>
      </div>
    </div>

    <div class="panel mb">
      <div class="panel-pad">
        <h3 class="section-title">结算单</h3>
      </div>
      <el-table v-loading="orderLoading" :data="orders">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="settlementNo" label="单号" min-width="160" />
        <el-table-column prop="shopId" label="店铺" width="90" />
        <el-table-column prop="merchantId" label="卖家" width="90" />
        <el-table-column prop="orderId" label="订单" width="90" />
        <el-table-column label="金额(分)" width="110">
          <template #default="{ row }">{{ row.amount }}</template>
        </el-table-column>
        <el-table-column prop="billStatus" label="入账" width="80" />
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="orderPage"
          :page-size="size"
          layout="total, prev, pager, next"
          :total="orderTotal"
          @current-change="loadOrders"
        />
      </div>
    </div>

    <div class="panel">
      <div class="panel-pad">
        <h3 class="section-title">账单</h3>
      </div>
      <el-table v-loading="billLoading" :data="bills">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="billNo" label="账单号" min-width="160" />
        <el-table-column prop="shopId" label="店铺" width="90" />
        <el-table-column prop="merchantId" label="卖家" width="90" />
        <el-table-column prop="billAmount" label="金额(分)" width="110" />
        <el-table-column prop="status" label="状态" width="100" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.settleStatus === 0"
              link
              type="primary"
              @click="settleBill(row.id)"
            >
              轧差结算
            </el-button>
            <span v-else class="muted">已结算</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination
          v-model:current-page="billPage"
          :page-size="size"
          layout="total, prev, pager, next"
          :total="billTotal"
          @current-change="loadBills"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import client from '@/api/client'
import type { PageResult, R } from '@/types'

const size = 20
const shopIdFilter = ref('')
const merchantIdFilter = ref('')

const orders = ref<any[]>([])
const orderLoading = ref(false)
const orderPage = ref(1)
const orderTotal = ref(0)

const bills = ref<any[]>([])
const billLoading = ref(false)
const billPage = ref(1)
const billTotal = ref(0)

function filterParams() {
  return {
    shopId: shopIdFilter.value.trim() ? Number(shopIdFilter.value) : undefined,
    merchantId: merchantIdFilter.value.trim() ? Number(merchantIdFilter.value) : undefined,
  }
}

function onFilterChange() {
  orderPage.value = 1
  billPage.value = 1
  loadOrders()
  loadBills()
}

async function loadOrders() {
  orderLoading.value = true
  try {
    const res = await client.get('/settlement-order', {
      params: { page: orderPage.value, size, ...filterParams() },
    }) as R<PageResult<any>>
    orders.value = res.data?.records || []
    orderTotal.value = res.data?.total || 0
  } finally {
    orderLoading.value = false
  }
}

async function loadBills() {
  billLoading.value = true
  try {
    const res = await client.get('/settlement-bill', {
      params: { page: billPage.value, size, ...filterParams() },
    }) as R<PageResult<any>>
    bills.value = res.data?.records || []
    billTotal.value = res.data?.total || 0
  } finally {
    billLoading.value = false
  }
}

async function createBill() {
  const shopId = Number(shopIdFilter.value.trim())
  if (!shopId) {
    ElMessage.warning('请填写店铺 ID')
    return
  }
  await client.post('/settlement-bill', null, { params: { shopId } })
  ElMessage.success('已创建店铺账单')
  await loadBills()
}

async function settleBill(id: number) {
  await client.post(`/settlement-bill/${id}/settle`)
  ElMessage.success('已轧差结算并入店铺账')
  await loadBills()
}

onMounted(() => {
  loadOrders()
  loadBills()
})
</script>

<style scoped>
.mb {
  margin-bottom: 16px;
}
.muted {
  color: var(--color-muted);
  font-size: 13px;
}
</style>
