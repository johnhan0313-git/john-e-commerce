<template>
  <div>
    <div class="page-header">
      <div>
        <h2>卖家管理</h2>
        <p class="desc">审核入驻申请，通过后自动创建默认店铺</p>
      </div>
      <div class="page-header-actions">
        <el-select v-model="status" clearable placeholder="全部状态" style="width: 140px" @change="load">
          <el-option :value="0" label="待审核" />
          <el-option :value="1" label="已通过" />
          <el-option :value="2" label="已拒绝" />
        </el-select>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="list">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="name" label="名称" min-width="140" />
        <el-table-column prop="contactName" label="联系人" width="120" />
        <el-table-column prop="contactPhone" label="电话" width="140" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small" effect="light">
              {{ row.statusLabel || statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button link type="success" @click="audit(row.id, true)">通过</el-button>
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

interface MerchantRow {
  id: number
  name: string
  contactName?: string
  contactPhone?: string
  status: number
  statusLabel?: string
}

const list = ref<MerchantRow[]>([])
const loading = ref(false)
const page = ref(1)
const size = 20
const total = ref(0)
const status = ref<number | undefined>(undefined)

function statusType(s: number) {
  if (s === 1) return 'success'
  if (s === 2) return 'danger'
  return 'warning'
}

function statusText(s: number) {
  if (s === 1) return '已通过'
  if (s === 2) return '已拒绝'
  return '待审核'
}

async function load() {
  loading.value = true
  try {
    const res = await client.get('/merchant', {
      params: { page: page.value, size, status: status.value },
    }) as R<PageResult<MerchantRow>>
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function audit(id: number, approved: boolean) {
  await client.put(`/merchant/${id}/audit`, { approved })
  ElMessage.success(approved ? '已通过并创建默认店铺' : '已拒绝')
  await load()
}

onMounted(load)
</script>
