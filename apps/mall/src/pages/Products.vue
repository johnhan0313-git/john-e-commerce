<template>
  <section class="products-page">
    <aside class="cat-aside card">
      <h2>类目</h2>
      <button
        type="button"
        class="cat-item"
        :class="{ active: !selectedCategoryId }"
        @click="selectCategory(null)"
      >
        全部
      </button>
      <CategoryNodes
        :nodes="categoryTree"
        :selected-id="selectedCategoryId"
        @select="selectCategory"
      />
      <p v-if="!categoryTree.length" class="muted cat-empty">暂无类目</p>
    </aside>

    <div class="cat-main">
      <div class="head">
        <h1>{{ currentTitle }}</h1>
        <p class="muted">浏览上架商品，进入详情选择 SKU 加购</p>
      </div>

      <div v-if="loading && !products.length" class="empty">加载中…</div>
      <div v-else-if="!products.length" class="empty card">该类目暂无商品</div>
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
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import client from '@/api/client'
import type { Category, PageResult, R, Spu } from '@/types'

const CategoryNodes = defineComponent({
  name: 'CategoryNodes',
  props: {
    nodes: { type: Array as () => Category[], default: () => [] },
    selectedId: { type: [Number, String, null] as unknown as () => string | number | null, default: null },
    depth: { type: Number, default: 0 },
  },
  emits: ['select'],
  setup(props, { emit }) {
    return () =>
      props.nodes.map((node) =>
        h('div', { class: 'cat-branch', key: String(node.id) }, [
          h(
            'button',
            {
              type: 'button',
              class: [
                'cat-item',
                {
                  active: String(props.selectedId) === String(node.id),
                },
              ],
              style: { paddingLeft: `${12 + props.depth * 14}px` },
              onClick: () => emit('select', node.id),
            },
            node.name,
          ),
          node.children?.length
            ? h(CategoryNodes, {
                nodes: node.children,
                selectedId: props.selectedId,
                depth: props.depth + 1,
                onSelect: (id: string | number) => emit('select', id),
              })
            : null,
        ]),
      )
  },
})

const route = useRoute()
const router = useRouter()

const categoryTree = ref<Category[]>([])
const selectedCategoryId = ref<string | number | null>(null)
const products = ref<Spu[]>([])
const page = ref(1)
const total = ref(0)
const loading = ref(false)
const hasMore = ref(false)

const currentTitle = computed(() => {
  if (!selectedCategoryId.value) return '全部商品'
  const name = findName(categoryTree.value, selectedCategoryId.value)
  return name || '商品'
})

function findName(nodes: Category[], id: string | number): string | null {
  for (const n of nodes) {
    if (String(n.id) === String(id)) return n.name
    if (n.children?.length) {
      const hit = findName(n.children, id)
      if (hit) return hit
    }
  }
  return null
}

function selectCategory(id: string | number | null) {
  selectedCategoryId.value = id
  router.replace({
    path: '/products',
    query: id != null ? { categoryId: String(id) } : {},
  })
}

async function loadCategories() {
  try {
    const res = (await client.get('/public/category/tree')) as R<Category[]>
    categoryTree.value = res.data || []
  } catch {
    categoryTree.value = []
  }
}

async function load(reset = false) {
  loading.value = true
  try {
    const res = (await client.get('/public/product', {
      params: {
        page: page.value,
        size: 20,
        categoryId: selectedCategoryId.value || undefined,
      },
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
  load()
}

function syncFromRoute() {
  const q = route.query.categoryId
  selectedCategoryId.value = q ? String(q) : null
  page.value = 1
  load(true)
}

watch(() => route.query.categoryId, syncFromRoute)

onMounted(async () => {
  await loadCategories()
  syncFromRoute()
})
</script>

<style scoped>
.products-page {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: var(--space-5);
  align-items: start;
}

.cat-aside {
  padding: var(--space-4);
  position: sticky;
  top: calc(var(--nav-h) + 12px);
}

.cat-aside h2 {
  margin: 0 0 var(--space-3);
  font-size: 1rem;
}

.cat-item {
  display: block;
  width: 100%;
  text-align: left;
  border: 0;
  background: transparent;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  color: var(--color-ink);
  font-size: 14px;
}

.cat-item:hover {
  background: rgba(196, 92, 38, 0.06);
}

.cat-item.active {
  background: rgba(196, 92, 38, 0.12);
  color: var(--color-accent);
  font-weight: 600;
}

.cat-empty {
  margin: var(--space-3) 0 0;
  font-size: 13px;
}

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

@media (max-width: 800px) {
  .products-page {
    grid-template-columns: 1fr;
  }

  .cat-aside {
    position: static;
  }
}
</style>

<style>
/* recursive component buttons share class outside scoped */
.cat-branch .cat-item {
  display: block;
  width: 100%;
  text-align: left;
  border: 0;
  background: transparent;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  color: var(--color-ink);
  font-size: 14px;
}
.cat-branch .cat-item:hover {
  background: rgba(196, 92, 38, 0.06);
}
.cat-branch .cat-item.active {
  background: rgba(196, 92, 38, 0.12);
  color: var(--color-accent);
  font-weight: 600;
}
</style>
