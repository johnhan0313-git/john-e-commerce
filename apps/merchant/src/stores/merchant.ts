import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import client from '@/api/client'
import type { MerchantMe, R, Shop } from '@/types'

const ACTIVE_SHOP_KEY = 'merchant_active_shop_id'

/** Keep snowflake ids as string — Number() loses precision above 2^53-1. */
function toId(id: unknown): string | null {
  if (id == null || id === '') return null
  const s = String(id).trim()
  return s || null
}

export const useMerchantStore = defineStore('merchant', () => {
  const me = ref<MerchantMe | null>(null)
  const loaded = ref(false)
  const activeShopId = ref<string | null>(readStoredShopId())

  const currentShop = computed(() => me.value?.currentShop ?? null)
  const shops = computed(() => me.value?.shops ?? [])
  const openShops = computed(() => shops.value.filter((s) => s.status === 1))

  function readStoredShopId(): string | null {
    return toId(localStorage.getItem(ACTIVE_SHOP_KEY))
  }

  function persistActiveShop(id: string | null) {
    activeShopId.value = id
    if (id != null) localStorage.setItem(ACTIVE_SHOP_KEY, id)
    else localStorage.removeItem(ACTIVE_SHOP_KEY)
  }

  function syncActiveShopFromMe() {
    const list = me.value?.shops || []
    const open = list.filter((s) => s.status === 1)
    if (activeShopId.value != null) {
      const stillValid = open.some((s) => toId(s.id) === activeShopId.value)
      if (stillValid) return
    }
    const preferred = me.value?.currentShop?.status === 1
      ? me.value.currentShop
      : open[0]
    persistActiveShop(toId(preferred?.id))
  }

  async function fetchMe() {
    const res = await client.get('/merchant/me') as R<MerchantMe | null>
    me.value = res.data ?? null
    loaded.value = true
    syncActiveShopFromMe()
    return me.value
  }

  function setActiveShop(shop: Shop | string | number) {
    const id = typeof shop === 'object' ? toId(shop.id) : toId(shop)
    persistActiveShop(id)
    if (me.value && id != null) {
      const found = me.value.shops?.find((s) => toId(s.id) === id)
      if (found) me.value.currentShop = found
    }
  }

  function clear() {
    me.value = null
    loaded.value = false
    persistActiveShop(null)
  }

  const isApproved = () =>
    me.value?.merchant?.status === 1 && openShops.value.length > 0

  return {
    me,
    loaded,
    activeShopId,
    currentShop,
    shops,
    openShops,
    fetchMe,
    setActiveShop,
    clear,
    isApproved,
  }
})
