<template>
  <div>
    <h2>采购管理</h2>
    <p v-if="!modulesStore.has('purchase')">采购模块未开通</p>
    <p v-else>采购列表（待实现）</p>
  </div>
</template>

<script setup lang="ts">
import { modulesStore } from '../stores/modules'
</script>
