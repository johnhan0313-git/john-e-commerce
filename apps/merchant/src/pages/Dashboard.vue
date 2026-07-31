<template>
  <div>
    <h2>概览</h2>
    <el-descriptions v-if="me?.merchant" :column="1" border style="max-width: 560px; margin-top: 16px">
      <el-descriptions-item label="卖家">{{ me.merchant.name }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ me.merchant.statusLabel || me.merchant.status }}</el-descriptions-item>
      <el-descriptions-item label="店铺">{{ me.shop?.name || '尚未开店' }}</el-descriptions-item>
      <el-descriptions-item label="店铺状态">{{ me.shop?.statusLabel || '-' }}</el-descriptions-item>
    </el-descriptions>
    <el-empty v-else description="尚未入驻，请先提交申请" />
    <div style="margin-top: 16px">
      <el-button v-if="!approved" type="primary" @click="$router.push('/apply')">去入驻</el-button>
      <el-button v-else type="primary" @click="$router.push('/products')">管理商品</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useMerchantStore } from '@/stores/merchant'

const merchant = useMerchantStore()
const me = computed(() => merchant.me)
const approved = computed(() => merchant.isApproved())

onMounted(() => {
  if (!merchant.loaded) merchant.fetchMe()
})
</script>

<style scoped>
h2 { margin: 0; }
</style>
