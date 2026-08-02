<template>
  <div>
    <div class="page-header">
      <div>
        <h2>商品管理</h2>
        <p class="desc">全域 SPU / SKU，可按卖家、店铺筛选</p>
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
        <el-button type="primary" @click="openCreate">创建 SPU</el-button>
      </div>
    </div>

    <div class="panel">
      <el-table v-loading="loading" :data="list">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="name" label="名称" min-width="160" />
        <el-table-column prop="merchantId" label="卖家" width="90" />
        <el-table-column prop="shopId" label="店铺" width="90" />
        <el-table-column prop="subtitle" label="副标题" min-width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small" effect="light">
              {{ row.status === 1 ? '上架' : '下架' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
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

    <el-dialog
      v-model="spuVisible"
      :title="spuForm.id ? '编辑 SPU' : '创建 SPU'"
      width="520px"
      destroy-on-close
    >
      <el-form label-width="90px">
        <el-form-item label="名称" required>
          <el-input v-model="spuForm.name" />
        </el-form-item>
        <el-form-item label="副标题">
          <el-input v-model="spuForm.subtitle" />
        </el-form-item>
        <el-form-item label="主图">
          <ImageUpload v-model="spuForm.imageUrl" folder="product" :aspect-ratio="1" hint="上传主图" />
        </el-form-item>
        <el-form-item label="详情">
          <el-input v-model="spuForm.detail" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="spuVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveSpu">
          {{ spuForm.id ? '保存' : '创建' }}
        </el-button>
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
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" @click="openSkuEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="removeSku(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <el-dialog
      v-model="skuDialogVisible"
      :title="skuForm.id ? '编辑 SKU' : '新增 SKU'"
      width="480px"
      destroy-on-close
    >
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
        <el-form-item v-if="!skuForm.id" label="初始库存">
          <el-input-number v-model="skuForm.initStock" :min="0" :step="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="skuDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveSku">
          {{ skuForm.id ? '保存' : '创建' }}
        </el-button>
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
const merchantIdFilter = ref('')
const shopIdFilter = ref('')

const spuVisible = ref(false)
const saving = ref(false)
const spuForm = reactive({
  id: null as number | null,
  name: '',
  subtitle: '',
  imageUrl: '',
  detail: '',
})

const skuVisible = ref(false)
const skuDialogVisible = ref(false)
const skuLoading = ref(false)
const currentSpu = ref<Spu | null>(null)
const skus = ref<Sku[]>([])
const skuForm = reactive({
  id: null as number | null,
  skuName: '',
  skuCode: '',
  price: 99,
  initStock: 0,
})

function onFilterChange() {
  page.value = 1
  load()
}

async function load() {
  loading.value = true
  try {
    const res = await client.get('/product', {
      params: {
        page: page.value,
        size,
        merchantId: merchantIdFilter.value.trim() ? Number(merchantIdFilter.value) : undefined,
        shopId: shopIdFilter.value.trim() ? Number(shopIdFilter.value) : undefined,
      },
    }) as R<PageResult<Spu>>
    list.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function resetSpuForm() {
  Object.assign(spuForm, { id: null, name: '', subtitle: '', imageUrl: '', detail: '' })
}

function openCreate() {
  resetSpuForm()
  spuVisible.value = true
}

function openEdit(row: Spu) {
  spuForm.id = row.id
  spuForm.name = row.name || ''
  spuForm.subtitle = row.subtitle || ''
  spuForm.imageUrl = row.mainImages?.[0] || ''
  spuForm.detail = row.detail || ''
  spuVisible.value = true
}

async function saveSpu() {
  if (!spuForm.name.trim()) {
    ElMessage.warning('请填写名称')
    return
  }
  const payload = {
    name: spuForm.name.trim(),
    subtitle: spuForm.subtitle || undefined,
    detail: spuForm.detail || undefined,
    mainImages: spuForm.imageUrl ? [spuForm.imageUrl] : [],
  }
  saving.value = true
  try {
    if (spuForm.id) {
      await client.put(`/product/${spuForm.id}`, payload)
      ElMessage.success('已保存')
    } else {
      const res = await client.post('/product', payload) as R<Spu>
      if (res.data?.id) {
        await client.put(`/product/${res.data.id}/status`, null, { params: { status: 1 } })
      }
      ElMessage.success('已创建并上架')
    }
    spuVisible.value = false
    await load()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: Spu) {
  const next = row.status === 1 ? 0 : 1
  await client.put(`/product/${row.id}/status`, null, { params: { status: next } })
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
    const res = await client.get('/sku', { params: { spuId: currentSpu.value.id } }) as R<Sku[]>
    skus.value = res.data || []
  } finally {
    skuLoading.value = false
  }
}

function openSkuCreate() {
  Object.assign(skuForm, {
    id: null,
    skuName: `${currentSpu.value?.name || 'SKU'}-默认`,
    skuCode: '',
    price: 99,
    initStock: 0,
  })
  skuDialogVisible.value = true
}

function openSkuEdit(row: Sku) {
  Object.assign(skuForm, {
    id: row.id,
    skuName: row.skuName || '',
    skuCode: row.skuCode || '',
    price: Number(row.price) || 0,
    initStock: 0,
  })
  skuDialogVisible.value = true
}

async function saveSku() {
  if (!currentSpu.value) return
  if (!skuForm.skuName.trim()) {
    ElMessage.warning('请填写名称')
    return
  }
  if (skuForm.price == null) {
    ElMessage.warning('请填写价格')
    return
  }
  saving.value = true
  try {
    const payload = {
      spuId: currentSpu.value.id,
      skuName: skuForm.skuName.trim(),
      skuCode: skuForm.skuCode || undefined,
      price: skuForm.price,
      status: 1,
    }
    if (skuForm.id) {
      await client.put(`/sku/${skuForm.id}`, payload)
      ElMessage.success('SKU 已保存')
    } else {
      await client.post('/sku', {
        ...payload,
        initStock: skuForm.initStock ?? 0,
      })
      ElMessage.success('SKU 已创建')
    }
    skuDialogVisible.value = false
    await loadSkus()
  } finally {
    saving.value = false
  }
}

async function removeSku(id: number) {
  await ElMessageBox.confirm('确认删除该 SKU？', '提示')
  await client.delete(`/sku/${id}`)
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
