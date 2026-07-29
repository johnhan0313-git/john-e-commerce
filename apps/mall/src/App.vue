<template>
  <div id="mall-app">
    <nav v-if="modules.loaded">
      <router-link to="/">首页</router-link>
      <router-link to="/products">商品</router-link>
      <router-link to="/cart">购物车</router-link>
      <router-link to="/orders">订单</router-link>
    </nav>
    <router-view />
  </div>
</template>

<script setup lang="ts">
import { useModulesStore } from '@/stores/modules'

const modules = useModulesStore()
modules.fetch()
</script>
