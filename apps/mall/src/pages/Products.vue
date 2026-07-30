<template>
  <section>
    <div class="head">
      <h1>商品</h1>
      <p class="muted">浏览上架商品，进入详情选择 SKU 加购</p>
    </div>

    <div v-if="loading && !products.length" class="empty">加载中…</div>
    <div v-else-if="!products.length" class="empty card">暂无商品，请先在管理后台创建 SPU/SKU</div>
    <div v-else class="grid">
      <router-link
        v-for="p in products"
        :key="p.id"
        class="card product"
        :to="`/products/${p.id}`"
      >
        <div class="thumb">
          <img v-if="p.mainImages?.[0]" :src="p.mainImages[0]" :alt="p.name" />
          <div v-else class="thumb-placeholder">暂无图片</div>
        </div>
        <div class="body">
          <h3>{{ p.name }}</h3>
          <p class="muted">{{ p.subtitle || '查看详情' }}</p>
        </div>
      </router-link>
    </div>

    <div v-if="hasMore" class="more">
      <button class="btn btn-ghost" type="button" :disabled="loading" @click="loadMore">
        {{ loading ? '加载中…' : '加载更多' }}
      </button>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import client from '@/api/client'
import type { PageResult, R, Spu } from '@/types'

const products = ref<Spu[]>([])
const page = ref(1)
const total = ref(0)
const loading = ref(false)
const hasMore = ref(false)

async function load(reset = false) {
  loading.value = true
  try {
    const res = await client.get('/product', {
      params: { page: page.value, size: 20, status: 1 },
    }) as R<PageResult<Spu>>
    const records = res.data?.records || []
    total.value = res.data?.total || 0
    products.value = reset ? records : [...products.value, ...records]
    hasMore.value = products.value.length < total.value
  } finally {
    loading.value = false
  }
}

function loadMore() {
  page.value += 1
  load()
}

onMounted(() => load(true))
</script>

<style scoped>
.head {
  margin-bottom: var(--space-5);
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: var(--space-4);
}

.product {
  overflow: hidden;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.product:hover {
  transform: translateY(-3px);
}

.thumb {
  aspect-ratio: 1;
  background: #efeae3;
}

.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-placeholder {
  height: 100%;
  display: grid;
  place-items: center;
  color: var(--color-muted);
}

.body {
  padding: var(--space-4);
}

.body h3 {
  font-size: 1rem;
  margin-bottom: var(--space-2);
}

.more {
  margin-top: var(--space-5);
  text-align: center;
}
</style>
