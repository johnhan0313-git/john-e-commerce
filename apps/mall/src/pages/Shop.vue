<template>
  <section v-if="loadingShop" class="empty">加载中…</section>
  <section v-else-if="!shop" class="empty card">店铺不存在或未营业</section>
  <section v-else>
    <div class="shop-hero card">
      <img v-if="shop.logo" :src="shop.logo" :alt="shop.name" class="shop-logo" />
      <div v-else class="shop-mark">{{ mark }}</div>
      <div>
        <h1>{{ shop.name }}</h1>
        <p class="muted">本店在售商品</p>
      </div>
    </div>

    <div v-if="loading && !products.length" class="empty">加载商品…</div>
    <div v-else-if="!products.length" class="empty card">店铺暂无上架商品</div>
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
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import client from '@/api/client'
import type { PageResult, R, Shop, Spu } from '@/types'

const route = useRoute()
const shop = ref<Shop | null>(null)
const products = ref<Spu[]>([])
const page = ref(1)
const total = ref(0)
const loadingShop = ref(true)
const loading = ref(false)
const hasMore = ref(false)

const mark = computed(() => (shop.value?.name?.trim().charAt(0) || '店').toUpperCase())

async function loadShop() {
  loadingShop.value = true
  shop.value = null
  products.value = []
  page.value = 1
  try {
    const res = (await client.get(`/public/shop/${route.params.id}`)) as R<Shop>
    shop.value = res.data
    if (shop.value) await loadProducts(true)
  } catch {
    shop.value = null
  } finally {
    loadingShop.value = false
  }
}

async function loadProducts(reset = false) {
  if (!shop.value) return
  loading.value = true
  try {
    const res = (await client.get(`/public/shop/${shop.value.id}/products`, {
      params: { page: page.value, size: 20 },
    })) as R<PageResult<Spu>>
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
  loadProducts()
}

watch(() => route.params.id, () => {
  loadShop()
})

onMounted(loadShop)
</script>

<style scoped>
.shop-hero {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-5);
  margin-bottom: var(--space-5);
}

.shop-logo,
.shop-mark {
  width: 64px;
  height: 64px;
  border-radius: 14px;
  object-fit: cover;
  flex-shrink: 0;
}

.shop-mark {
  display: grid;
  place-items: center;
  font-family: var(--font-display);
  font-size: 1.4rem;
  font-weight: 700;
  color: #fff;
  background: linear-gradient(135deg, var(--color-accent), #8b4513);
}

.shop-hero h1 {
  margin: 0 0 4px;
  font-size: 1.5rem;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: var(--space-4);
}

.product {
  overflow: hidden;
  text-decoration: none;
  color: inherit;
  transition: transform 0.15s ease;
}

.product:hover {
  transform: translateY(-2px);
}

.thumb,
.thumb-placeholder {
  aspect-ratio: 1;
  background: #efeae3;
}

.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.thumb-placeholder {
  display: grid;
  place-items: center;
  color: var(--color-muted);
  font-size: 13px;
}

.body {
  padding: var(--space-3);
}

.body h3 {
  margin: 0 0 4px;
  font-size: 1rem;
}

.more {
  margin-top: var(--space-5);
  text-align: center;
}
</style>
