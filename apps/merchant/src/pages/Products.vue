<template>
  <div>
    <div class="page-header">
      <div>
        <h2>本店商品</h2>
        <p class="desc">管理本店 SPU / SKU 上下架</p>
      </div>
      <div class="page-header-actions">
        <el-button type="primary" @click="openCreate">创建 SPU</el-button>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="list">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="name" label="名称" min-width="160" />
        <el-table-column prop="subtitle" label="副标题" min-width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="light">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openSku(row)">SKU</el-button>
            <el-button link type="warning" @click="toggleStatus(row)">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
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

    <el-dialog v-model="createVisible" title="创建 SPU" width="520px">
      <el-form label-width="90px">
        <el-form-item label="名称" required>
          <el-input v-model="createForm.name" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="createForm.subtitle" />
        </el-form-item>
        <el-form-item label="主图">
          <ImageUpload v-model="createForm.imageUrl" folder="product" :aspect-ratio="1" hint="上传主图" />
        </el-form-item>
        <el-form-item label="详情">
          <el-input v-model="createForm.detail" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="createSpu">创建</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="skuVisible" :title="`SKU · ${currentSpu?.name || ''}`" size="560px">
      <div class="drawer-toolbar">
        <span class="muted">SPU #{{ currentSpu?.id }}</span>
        <el-button type="primary" size="small" @click="openSkuCreate">新增 SKU</el-button>
      </div>
      <el-table :data="skus" size="small" v-loading="skuLoading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="skuName" label="名称" />
        <el-table-column prop="price" label="价格" width="100" />
        <el-table-column prop="available" label="可售" width="80" />
        <el-table-column prop="status" label="状态" width="80" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button link type="danger" @click="removeSku(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <el-dialog v-model="skuCreateVisible" title="新增 SKU" width="480px">
      <el-form label-width="90px">
        <el-form-item label="名称" required>
          <el-input v-model="skuForm.skuName" />
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model="skuForm.skuCode" />
        </el-form-item>
        <el-form-item label="价格" required>
          <el-input-number v-model="skuForm.price" :min="0" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="初始库存">
          <el-input-number v-model="skuForm.initStock" :min="0" :step="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="skuCreateVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="createSku">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import client from '@/api/client'
import ImageUpload from '@/components/ImageUpload.vue'
import type { PageResult, R, Sku, Spu } from '@/types'

const list = ref<Spu[]>([])
const loading = ref(false)
const page = ref(1)
const size = 20
const total = ref(0)
const createVisible = ref(false)
const saving = ref(false)
const createForm = reactive({ name: '', subtitle: '', imageUrl: '', detail: '' })
const skuVisible = ref(false)
const skuCreateVisible = ref(false)
const skuLoading = ref(false)
const currentSpu = ref<Spu | null>(null)
const skus = ref<Sku[]>([])
const skuForm = reactive({ skuName: '', skuCode: '', price: 99, initStock: 0 })

async function load() {
  loading.value = true
  try {
    const res = await client.get('/shop/products', {
      params: { page: page.value, size },
    }) as R<PageResult<Spu>>
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(createForm, { name: '', subtitle: '', imageUrl: '', detail: '' })
  createVisible.value = true
}

async function createSpu() {
  if (!createForm.name.trim()) {
    ElMessage.warning('请填写名称')
    return
  }
  saving.value = true
  try {
    const res = await client.post('/shop/products', {
      name: createForm.name.trim(),
      subtitle: createForm.subtitle || undefined,
      detail: createForm.detail || undefined,
      mainImages: createForm.imageUrl ? [createForm.imageUrl] : undefined,
    }) as R<Spu>
    if (res.data?.id) {
      await client.put(`/shop/products/${res.data.id}/status`, null, { params: { status: 1 } })
    }
    ElMessage.success('已创建并上架')
    createVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: Spu) {
  const next = row.status === 1 ? 0 : 1
  await client.put(`/shop/products/${row.id}/status`, null, { params: { status: next } })
  row.status = next
  ElMessage.success(next === 1 ? '已上架' : '已下架')
}

async function openSku(row: Spu) {
  currentSpu.value = row
  skuVisible.value = true
  await loadSkus()
}

async function loadSkus() {
  if (!currentSpu.value) return
  skuLoading.value = true
  try {
    const res = await client.get(`/shop/products/${currentSpu.value.id}/skus`) as R<Sku[]>
    skus.value = res.data || []
  } finally {
    skuLoading.value = false
  }
}

function openSkuCreate() {
  skuForm.skuName = `${currentSpu.value?.name || 'SKU'}-默认`
  skuForm.skuCode = ''
  skuForm.price = 99
  skuForm.initStock = 0
  skuCreateVisible.value = true
}

async function createSku() {
  if (!currentSpu.value) return
  saving.value = true
  try {
    await client.post(`/shop/products/${currentSpu.value.id}/skus`, {
      spuId: currentSpu.value.id,
      skuName: skuForm.skuName,
      skuCode: skuForm.skuCode || undefined,
      price: skuForm.price,
      initStock: skuForm.initStock ?? 0,
      status: 1,
    })
    ElMessage.success('SKU 已创建')
    skuCreateVisible.value = false
    await loadSkus()
  } finally {
    saving.value = false
  }
}

async function removeSku(id: number | string) {
  await ElMessageBox.confirm('确认删除该 SKU？', '提示')
  await client.delete(`/shop/skus/${id}`)
  ElMessage.success('已删除')
  await loadSkus()
}

onMounted(load)
</script>

<style scoped>
.drawer-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>
