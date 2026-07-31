import { defineStore } from 'pinia'
import { ref } from 'vue'
import client from '@/api/client'
import type { MerchantMe, R } from '@/types'

export const useMerchantStore = defineStore('merchant', () => {
  const me = ref<MerchantMe | null>(null)
  const loaded = ref(false)

  async function fetchMe() {
    const res = await client.get('/merchant/me') as R<MerchantMe | null>
    me.value = res.data ?? null
    loaded.value = true
    return me.value
  }

  function clear() {
    me.value = null
    loaded.value = false
  }

  const isApproved = () => me.value?.merchant?.status === 1 && !!me.value?.shop

  return { me, loaded, fetchMe, clear, isApproved }
})
