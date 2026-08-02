<template>
  <div>
    <div class="page-header">
      <div>
        <h2>商品管理</h2>
        <p class="desc">创建/编辑同一页：规格笛卡尔积生成 SKU</p>
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
        <el-button type="primary" @click="openCreate">创建商品</el-button>
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
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
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
      v-model="editorVisible"
      :title="form.id ? '编辑商品' : '创建商品'"
      width="780px"
      top="4vh"
      class="product-editor-dialog"
      destroy-on-close
    >
      <el-form class="product-form" label-width="72px" v-loading="editorLoading">
        <div class="section-title">基本信息</div>
        <el-row :gutter="16">
          <el-col :span="14">
            <el-form-item label="名称" required>
              <el-input v-model="form.name" placeholder="商品名称" maxlength="80" show-word-limit />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="类目">
              <el-tree-select
                v-model="form.categoryId"
                :data="categoryTree"
                :props="{ label: 'name', value: 'id', children: 'children' }"
                check-strictly
                clearable
                filterable
                placeholder="选择类目"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="副标题">
          <el-input v-model="form.subtitle" placeholder="一句话卖点" maxlength="120" show-word-limit />
        </el-form-item>
        <el-form-item label="主图">
          <ImageUpload v-model="form.imageUrl" folder="product" :aspect-ratio="1" hint="上传主图" />
        </el-form-item>
        <el-form-item label="详情" class="detail-item">
          <RichTextEditor v-model="form.detail" folder="product" :height="240" />
        </el-form-item>

        <div class="section-title">
          销售规格
          <span class="section-hint">填写属性后自动笛卡尔积生成 SKU；无规格则生成 1 个默认 SKU</span>
        </div>
        <div class="spec-list">
          <div v-for="(attr, idx) in form.salesAttrs" :key="idx" class="spec-row">
            <el-input
              v-model="attr.name"
              placeholder="规格名，如 颜色"
              class="spec-name"
              @change="regenSkus"
            />
            <el-select
              v-model="attr.values"
              multiple
              filterable
              allow-create
              default-first-option
              placeholder="回车添加规格值"
              class="spec-values"
              @change="regenSkus"
            />
            <el-button link type="danger" @click="removeAttr(idx)">删除</el-button>
          </div>
          <el-button type="primary" link @click="addAttr">+ 添加规格</el-button>
        </div>

        <div class="section-title">
          SKU 列表
          <span class="section-hint">共 {{ form.skus.length }} 行</span>
        </div>
        <div class="sku-batch">
          <el-input-number v-model="batchPrice" :min="0" :precision="2" :step="1" />
          <el-button size="small" @click="applyBatchPrice">统一价格</el-button>
          <el-input-number v-model="batchStock" :min="0" :step="1" />
          <el-button size="small" @click="applyBatchStock">统一库存</el-button>
        </div>
        <el-table :data="form.skus" size="small" max-height="280" border>
          <el-table-column
            v-for="attr in activeAttrNames"
            :key="attr"
            :label="attr"
            min-width="90"
          >
            <template #default="{ row }">{{ row.specValues[attr] }}</template>
          </el-table-column>
          <el-table-column label="SKU 名称" min-width="140">
            <template #default="{ row }">
              <el-input v-model="row.skuName" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="编码" min-width="110">
            <template #default="{ row }">
              <el-input v-model="row.skuCode" size="small" placeholder="可选" />
            </template>
          </el-table-column>
          <el-table-column label="价格" width="130">
            <template #default="{ row }">
              <el-input-number v-model="row.price" :min="0" :precision="2" :step="1" size="small" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column label="库存" width="120">
            <template #default="{ row }">
              <el-input-number v-model="row.initStock" :min="0" :step="1" size="small" controls-position="right" />
            </template>
          </el-table-column>
          <el-table-column label="" width="70" fixed="right">
            <template #default="{ $index }">
              <el-button
                link
                type="danger"
                size="small"
                :disabled="form.skus.length <= 1"
                @click="form.skus.splice($index, 1)"
              >移除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-form>
      <template #footer>
        <el-button @click="editorVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">
          {{ form.id ? '保存' : '创建并上架' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import client from '@/api/client'
import ImageUpload from '@/components/ImageUpload.vue'
import RichTextEditor from '@/components/RichTextEditor.vue'
import type { Category, PageResult, R, SalesAttr as SalesAttrType, Sku, Spu } from '@/types'

interface SalesAttr {
  name: string
  values: string[]
}

interface SkuDraft {
  id?: number | string
  skuName: string
  skuCode: string
  price: number
  initStock: number
  specValues: Record<string, string>
}

const list = ref<Spu[]>([])
const loading = ref(false)
const page = ref(1)
const size = 20
const total = ref(0)
const merchantIdFilter = ref('')
const shopIdFilter = ref('')
const saving = ref(false)
const editorVisible = ref(false)
const editorLoading = ref(false)

const form = reactive({
  id: null as number | string | null,
  name: '',
  subtitle: '',
  imageUrl: '',
  detail: '',
  categoryId: null as number | string | null,
  salesAttrs: [] as SalesAttr[],
  skus: [] as SkuDraft[],
})
const batchPrice = ref(99)
const batchStock = ref(0)
const categoryTree = ref<Category[]>([])

const activeAttrNames = computed(() =>
  form.salesAttrs
    .filter((a) => a.name.trim() && a.values.some((v) => String(v).trim()))
    .map((a) => a.name.trim()),
)

function onFilterChange() {
  page.value = 1
  load()
}

function cartesian(attrs: SalesAttr[]): Record<string, string>[] {
  const valid = attrs
    .map((a) => ({
      name: a.name.trim(),
      values: a.values.map((v) => String(v).trim()).filter(Boolean),
    }))
    .filter((a) => a.name && a.values.length)
  if (!valid.length) return [{}]
  return valid.reduce<Record<string, string>[]>((acc, attr) => {
    const next: Record<string, string>[] = []
    for (const row of acc) {
      for (const v of attr.values) {
        next.push({ ...row, [attr.name]: v })
      }
    }
    return next
  }, [{}])
}

function specKey(spec: Record<string, string>) {
  return Object.keys(spec)
    .sort()
    .map((k) => `${k}=${spec[k]}`)
    .join('|')
}

function buildSkuName(productName: string, spec: Record<string, string>) {
  const parts = Object.values(spec).filter(Boolean)
  if (!parts.length) return productName || '默认规格'
  return `${productName || '商品'}-${parts.join('-')}`
}

function regenSkus() {
  const combos = cartesian(form.salesAttrs)
  const prev = new Map(form.skus.map((s) => [specKey(s.specValues), s]))
  const productName = form.name.trim()
  form.skus = combos.map((spec) => {
    const old = prev.get(specKey(spec))
    return {
      id: old?.id,
      skuName: old?.skuName || buildSkuName(productName, spec),
      skuCode: old?.skuCode || '',
      price: old?.price ?? batchPrice.value ?? 99,
      initStock: old?.initStock ?? batchStock.value ?? 0,
      specValues: { ...spec },
    }
  })
}

function addAttr() {
  form.salesAttrs.push({ name: '', values: [] })
}

function removeAttr(idx: number) {
  form.salesAttrs.splice(idx, 1)
  regenSkus()
}

function applyBatchPrice() {
  if (batchPrice.value == null) return
  form.skus.forEach((s) => {
    s.price = batchPrice.value
  })
}

function applyBatchStock() {
  if (batchStock.value == null) return
  form.skus.forEach((s) => {
    s.initStock = batchStock.value
  })
}

function attrsFromSkus(skus: Sku[]): SalesAttr[] {
  const map = new Map<string, Set<string>>()
  for (const s of skus) {
    const spec = s.specValues || {}
    for (const [k, v] of Object.entries(spec)) {
      if (!k || !v) continue
      if (!map.has(k)) map.set(k, new Set())
      map.get(k)!.add(String(v))
    }
  }
  return [...map.entries()].map(([name, values]) => ({
    name,
    values: [...values],
  }))
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

function resetForm() {
  Object.assign(form, {
    id: null,
    name: '',
    subtitle: '',
    imageUrl: '',
    detail: '',
    categoryId: null,
    salesAttrs: [{ name: '颜色', values: [] }] as SalesAttr[],
    skus: [] as SkuDraft[],
  })
  batchPrice.value = 99
  batchStock.value = 0
  regenSkus()
}

async function loadCategories() {
  const res = (await client.get('/category/tree')) as R<Category[]>
  categoryTree.value = res.data || []
}

function openCreate() {
  resetForm()
  loadCategories()
  editorVisible.value = true
}

async function openEdit(row: Spu) {
  editorVisible.value = true
  editorLoading.value = true
  try {
    await loadCategories()
    const detail = (await client.get(`/product/${row.id}`)) as R<Spu>
    const spu = detail.data || row
    const skuRes = (await client.get('/sku', { params: { spuId: row.id } })) as R<Sku[]>
    const skuList = skuRes.data || []
    const salesAttrs: SalesAttr[] =
      (spu.salesAttrs as SalesAttrType[] | undefined)?.length
        ? spu.salesAttrs!.map((a) => ({
            name: a.name,
            values: [...(a.values || [])],
          }))
        : attrsFromSkus(skuList)
    Object.assign(form, {
      id: spu.id,
      name: spu.name || '',
      subtitle: spu.subtitle || '',
      imageUrl: spu.mainImages?.[0] || '',
      detail: spu.detail || '',
      categoryId: spu.categoryId != null ? String(spu.categoryId) : null,
      salesAttrs: salesAttrs.length ? salesAttrs : [{ name: '颜色', values: [] }],
      skus: skuList.map((s) => ({
        id: s.id,
        skuName: s.skuName || '',
        skuCode: s.skuCode || '',
        price: Number(s.price) || 0,
        initStock: s.available ?? 0,
        specValues: { ...(s.specValues || {}) },
      })),
    })
    if (!form.skus.length) regenSkus()
  } finally {
    editorLoading.value = false
  }
}

function buildPayload() {
  const salesAttrs = form.salesAttrs
    .map((a) => ({
      name: a.name.trim(),
      values: a.values.map((v) => String(v).trim()).filter(Boolean),
    }))
    .filter((a) => a.name && a.values.length)

  return {
    name: form.name.trim(),
    subtitle: form.subtitle || undefined,
    detail: form.detail || undefined,
    categoryId: form.categoryId,
    mainImages: form.imageUrl ? [form.imageUrl] : [],
    salesAttrs,
    skus: form.skus.map((s) => ({
      id: s.id || undefined,
      skuName: s.skuName.trim() || buildSkuName(form.name.trim(), s.specValues),
      skuCode: s.skuCode || undefined,
      price: s.price,
      initStock: s.initStock ?? 0,
      status: 1,
      specValues: Object.keys(s.specValues).length ? s.specValues : undefined,
    })),
  }
}

async function save() {
  if (!form.name.trim()) {
    ElMessage.warning('请填写名称')
    return
  }
  if (!form.skus.length) {
    ElMessage.warning('请至少保留一个 SKU')
    return
  }
  for (const s of form.skus) {
    if (s.price == null || s.price < 0) {
      ElMessage.warning('请为每个 SKU 填写价格')
      return
    }
  }
  const payload = buildPayload()
  saving.value = true
  try {
    if (form.id) {
      await client.put(`/product/${form.id}`, payload)
      ElMessage.success('已保存')
    } else {
      const res = (await client.post('/product', payload)) as R<Spu>
      if (res.data?.id) {
        await client.put(`/product/${res.data.id}/status`, null, { params: { status: 1 } })
      }
      ElMessage.success(`已创建 ${payload.skus.length} 个 SKU 并上架`)
    }
    editorVisible.value = false
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

onMounted(load)
</script>

<style scoped>
.product-form {
  max-width: 720px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  margin: 4px 0 12px;
  display: flex;
  align-items: baseline;
  gap: 10px;
}
.section-hint {
  font-size: 12px;
  font-weight: 400;
  color: var(--el-text-color-secondary);
}
.detail-item :deep(.el-form-item__content) {
  line-height: normal;
}
.spec-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
  padding: 0 0 0 72px;
}
.spec-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.spec-name {
  width: 120px;
  flex-shrink: 0;
}
.spec-values {
  flex: 1;
  min-width: 0;
  max-width: 420px;
}
.sku-batch {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0 0 12px 72px;
  flex-wrap: wrap;
}
</style>
