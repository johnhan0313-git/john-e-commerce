import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useMerchantStore } from '@/stores/merchant'

vi.mock('@/api/client', () => ({
  default: {
    get: vi.fn(),
  },
}))

import client from '@/api/client'

describe('merchant store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.mocked(client.get).mockReset()
  })

  it('isApproved requires merchant status 1 and an open shop', async () => {
    const store = useMerchantStore()
    vi.mocked(client.get).mockResolvedValue({
      code: 200,
      data: {
        merchant: { id: '1', userId: '1', name: 'A', status: 1 },
        shops: [{ id: '2', merchantId: '1', name: 'Shop', status: 1 }],
        currentShop: { id: '2', merchantId: '1', name: 'Shop', status: 1 },
      },
    })
    await store.fetchMe()
    expect(store.isApproved()).toBe(true)
    expect(store.activeShopId).toBe('2')
  })

  it('keeps snowflake shop ids as strings without Number() corruption', async () => {
    const store = useMerchantStore()
    const shopA = '2083558440918659074'
    const shopB = '2083576307844198402'
    vi.mocked(client.get).mockResolvedValue({
      code: 200,
      data: {
        merchant: { id: '2083558440918659000', userId: '1', name: 'A', status: 1 },
        shops: [
          { id: shopA, merchantId: '1', name: '雨田优选', status: 1 },
          { id: shopB, merchantId: '1', name: '雨田百货', status: 1 },
        ],
        currentShop: { id: shopA, merchantId: '1', name: '雨田优选', status: 1 },
      },
    })
    await store.fetchMe()
    expect(store.activeShopId).toBe(shopA)

    store.setActiveShop(shopB)
    expect(store.activeShopId).toBe(shopB)
    expect(localStorage.getItem('merchant_active_shop_id')).toBe(shopB)
    expect(store.currentShop?.name).toBe('雨田百货')

    // reload from storage must not coerce via Number (precision loss)
    setActivePinia(createPinia())
    const again = useMerchantStore()
    expect(again.activeShopId).toBe(shopB)
    await again.fetchMe()
    expect(again.activeShopId).toBe(shopB)
  })

  it('pending merchant is not approved', async () => {
    const store = useMerchantStore()
    vi.mocked(client.get).mockResolvedValue({
      code: 200,
      data: {
        merchant: { id: '1', userId: '1', name: 'A', status: 0 },
        shops: [],
        currentShop: null,
      },
    })
    await store.fetchMe()
    expect(store.isApproved()).toBe(false)
  })

  it('null me is not approved', async () => {
    const store = useMerchantStore()
    vi.mocked(client.get).mockResolvedValue({ code: 200, data: null })
    await store.fetchMe()
    expect(store.me).toBeNull()
    expect(store.isApproved()).toBe(false)
  })
})
