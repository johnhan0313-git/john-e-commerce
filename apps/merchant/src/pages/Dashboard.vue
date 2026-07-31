<template>
  <div>
    <div class="page-header">
      <div>
        <h2>概览</h2>
        <p class="desc">店铺状态与快捷入口</p>
      </div>
    </div>

    <template v-if="me?.merchant">
      <div class="stat-grid">
        <div class="stat-card accent">
          <div class="label">卖家</div>
          <div class="value name">{{ me.merchant.name }}</div>
          <div class="hint">主体信息</div>
        </div>
        <div class="stat-card">
          <div class="label">入驻状态</div>
          <div class="value status">
            <el-tag :type="statusType" size="large" effect="light">
              {{ me.merchant.statusLabel || me.merchant.status }}
            </el-tag>
          </div>
          <div class="hint">审核结果</div>
        </div>
        <div class="stat-card">
          <div class="label">店铺</div>
          <div class="value name">{{ me.shop?.name || '尚未开店' }}</div>
          <div class="hint">{{ me.shop?.statusLabel || '提交入驻后开通' }}</div>
        </div>
      </div>

      <div class="panel">
        <div class="panel-body cta-row">
          <div>
            <h3 class="section-title">快捷操作</h3>
            <p class="muted">{{ approved ? '管理本店商品与订单履约' : '先完成入驻申请，审核通过后开放商品能力' }}</p>
          </div>
          <div class="cta-actions">
            <el-button v-if="!approved" type="primary" @click="$router.push('/apply')">去入驻</el-button>
            <template v-else>
              <el-button type="primary" @click="$router.push('/products')">管理商品</el-button>
              <el-button @click="$router.push('/orders')">查看订单</el-button>
            </template>
          </div>
        </div>
      </div>
    </template>

    <div v-else class="panel">
      <div class="panel-body empty-wrap">
        <el-empty description="尚未入驻，请先提交申请">
          <el-button type="primary" @click="$router.push('/apply')">去入驻</el-button>
        </el-empty>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useMerchantStore } from '@/stores/merchant'

const merchant = useMerchantStore()
const me = computed(() => merchant.me)
const approved = computed(() => merchant.isApproved())

const statusType = computed(() => {
  const s = me.value?.merchant?.status
  if (s === 1) return 'success'
  if (s === 2) return 'danger'
  return 'warning'
})

onMounted(() => {
  if (!merchant.loaded) merchant.fetchMe()
})
</script>

<style scoped>
.stat-card.accent {
  background:
    linear-gradient(135deg, rgba(20, 184, 166, 0.12), transparent 60%),
    var(--color-surface);
  border-color: rgba(20, 184, 166, 0.28);
}

.stat-card .value.name {
  font-size: 20px;
  word-break: break-word;
}

.stat-card .value.status {
  display: flex;
  align-items: center;
  min-height: 34px;
}

.cta-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.cta-row .section-title {
  margin-bottom: 6px;
}

.cta-row .muted {
  margin: 0;
  font-size: 13px;
}

.cta-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.empty-wrap {
  padding: 40px 24px;
}
</style>
