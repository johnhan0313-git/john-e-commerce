<template>
  <section class="home">
    <div class="hero card">
      <div>
        <p class="eyebrow">John Mall</p>
        <h1>发现好物</h1>
        <p class="muted">多业态演示商城 · 登录后即可下单支付</p>
        <router-link class="btn" to="/products">去逛逛</router-link>
      </div>
    </div>

    <h2>精选 Banner</h2>
    <div v-if="loading" class="empty">加载中…</div>
    <div v-else-if="!banners.length" class="empty card">暂无 Banner，可在后台内容模块配置</div>
    <div v-else class="banner-grid">
      <a
        v-for="b in banners"
        :key="b.id"
        class="card banner-item"
        :href="b.linkUrl || '/products'"
      >
        <img v-if="b.imageUrl" :src="b.imageUrl" :alt="b.title || 'banner'" />
        <div class="banner-meta">
          <strong>{{ b.title || '活动' }}</strong>
        </div>
      </a>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import client from '@/api/client'
import type { Banner, R } from '@/types'

const banners = ref<Banner[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await client.get('/public/content/banner') as R<Banner[]>
    banners.value = res.data || []
  } catch {
    banners.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.hero {
  padding: var(--space-6);
  margin-bottom: var(--space-6);
  background:
    linear-gradient(135deg, rgba(196, 92, 38, 0.12), transparent 50%),
    var(--color-surface);
}

.eyebrow {
  font-size: 13px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--color-accent);
  margin: 0 0 var(--space-2);
}

.hero h1 {
  font-size: clamp(2rem, 4vw, 2.75rem);
  margin-bottom: var(--space-3);
}

.hero .btn {
  margin-top: var(--space-4);
}

.banner-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: var(--space-4);
}

.banner-item {
  overflow: hidden;
  transition: transform 0.15s ease;
}

.banner-item:hover {
  transform: translateY(-2px);
}

.banner-item img {
  width: 100%;
  aspect-ratio: 16 / 9;
  object-fit: cover;
  background: #eee;
}

.banner-meta {
  padding: var(--space-3) var(--space-4);
}
</style>
