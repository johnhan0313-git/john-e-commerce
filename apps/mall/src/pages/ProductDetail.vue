<template>
  <section v-if="loading" class="empty">加载中…</section>
  <section v-else-if="!spu" class="empty card">商品不存在</section>
  <section v-else class="detail">
    <div class="gallery card">
      <img v-if="cover" :src="cover" :alt="spu.name" />
      <div v-else class="placeholder">暂无图片</div>
    </div>
    <div class="info">
      <h1>{{ spu.name }}</h1>
      <p class="muted">{{ spu.subtitle || '精选商品' }}</p>
      <p class="price">¥{{ displayPrice }}</p>

      <router-link
        v-if="shop"
        class="shop-entry card"
        :to="`/shops/${shop.id}`"
      >
        <img v-if="shop.logo" :src="shop.logo" :alt="shop.name" class="shop-logo" />
        <div v-else class="shop-mark">{{ shopMark }}</div>
        <div class="shop-meta">
          <strong>{{ shop.name }}</strong>
          <span class="muted">进店逛逛</span>
        </div>
        <span class="shop-arrow">→</span>
      </router-link>

      <div v-if="skus.length" class="sku-block">
        <h3>规格</h3>
        <div class="sku-list">
          <button
            v-for="s in skus"
            :key="s.id"
            type="button"
            class="sku-chip"
            :class="{ active: selectedSkuId === s.id }"
            @click="selectedSkuId = s.id"
          >
            {{ s.skuName || s.skuCode || `SKU ${s.id}` }} · ¥{{ s.price }}
          </button>
        </div>
      </div>
      <div v-else class="empty card">该商品暂无 SKU，请在后台创建</div>

      <label class="field qty">
        <span>数量</span>
        <input v-model.number="quantity" type="number" min="1" />
      </label>

      <div class="actions">
        <button class="btn" type="button" :disabled="!selectedSkuId || adding" @click="addToCart">
          {{ adding ? '加入中…' : '加入购物车' }}
        </button>
        <router-link class="btn btn-ghost" to="/cart">去购物车</router-link>
      </div>

      <div v-if="spu.detail" class="desc card">
        <h3>详情</h3>
        <p>{{ spu.detail }}</p>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import client from '@/api/client'
import { useAuthStore } from '@/stores/auth'
import { toast } from '@/utils/toast'
import type { R, Shop, Sku, Spu } from '@/types'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const spu = ref<Spu | null>(null)
const shop = ref<Shop | null>(null)
const skus = ref<Sku[]>([])
const selectedSkuId = ref<number | string | null>(null)
const quantity = ref(1)
const loading = ref(true)
const adding = ref(false)

const selectedSku = computed(() => skus.value.find((s) => s.id === selectedSkuId.value) || null)
const displayPrice = computed(() => selectedSku.value?.price ?? skus.value[0]?.price ?? '—')
const cover = computed(() => spu.value?.mainImages?.[0])
const shopMark = computed(() => (shop.value?.name?.trim().charAt(0) || '店').toUpperCase())

onMounted(async () => {
  const id = route.params.id
  try {
    const [spuRes, skuRes] = await Promise.all([
      client.get(`/public/product/${id}`) as Promise<R<Spu>>,
      client.get(`/public/product/${id}/skus`) as Promise<R<Sku[]>>,
    ])
    spu.value = spuRes.data
    skus.value = skuRes.data || []
    selectedSkuId.value = skus.value[0]?.id ?? null
    if (spu.value?.shopId) {
      try {
        const shopRes = (await client.get(`/public/shop/${spu.value.shopId}`)) as R<Shop>
        shop.value = shopRes.data
      } catch {
        shop.value = null
      }
    }
  } finally {
    loading.value = false
  }
})

async function addToCart() {
  if (!selectedSkuId.value) return
  if (!auth.isLoggedIn) {
    router.push({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  adding.value = true
  try {
    await client.post('/cart', {
      skuId: selectedSkuId.value,
      quantity: Math.max(1, quantity.value || 1),
    })
    toast('已加入购物车', 'success')
  } finally {
    adding.value = false
  }
}
</script>

<style scoped>
.detail {
  display: grid;
  grid-template-columns: minmax(0, 1.1fr) minmax(0, 1fr);
  gap: var(--space-5);
}

.gallery {
  overflow: hidden;
  min-height: 320px;
}

.gallery img,
.placeholder {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  background: #efeae3;
}

.placeholder {
  display: grid;
  place-items: center;
  color: var(--color-muted);
}

.price {
  font-size: 1.5rem;
  margin: var(--space-4) 0;
}

.shop-entry {
  display: flex;
  align-items: center;
  gap: var(--space-3);
  padding: var(--space-3);
  margin-bottom: var(--space-4);
  text-decoration: none;
  color: inherit;
  transition: border-color 0.15s ease;
}

.shop-entry:hover {
  border-color: var(--color-accent);
}

.shop-logo,
.shop-mark {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
}

.shop-mark {
  display: grid;
  place-items: center;
  font-weight: 700;
  font-size: 14px;
  color: #fff;
  background: linear-gradient(135deg, var(--color-accent), #8b4513);
}

.shop-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
  flex: 1;
}

.shop-meta strong {
  font-size: 14px;
}

.shop-meta .muted {
  font-size: 12px;
}

.shop-arrow {
  color: var(--color-muted);
  font-size: 16px;
}

.sku-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
  margin-bottom: var(--space-4);
}

.sku-chip {
  border: 1px solid var(--color-border);
  background: #fff;
  border-radius: 999px;
  padding: 8px 12px;
  cursor: pointer;
}

.sku-chip.active {
  border-color: var(--color-accent);
  color: var(--color-accent);
  background: rgba(196, 92, 38, 0.08);
}

.qty {
  max-width: 140px;
}

.actions {
  display: flex;
  gap: var(--space-3);
  margin: var(--space-4) 0 var(--space-5);
}

.desc {
  padding: var(--space-4);
}

@media (max-width: 800px) {
  .detail {
    grid-template-columns: 1fr;
  }
}
</style>
