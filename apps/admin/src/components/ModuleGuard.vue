<template>
  <slot v-if="modulesStore.has(code)" />
  <div v-else>
    <p>该模块暂未开通</p>
  </div>
</template>

<script setup lang="ts">
import { modulesStore } from '../stores/modules'

defineProps<{ code: string }>()
</script>
