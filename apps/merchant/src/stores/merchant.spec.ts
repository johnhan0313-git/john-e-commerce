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
        merchant: { id: 1, userId: 1, name: 'A', status: 1 },
        shops: [{ id: 2, merchantId: 1, name: 'Shop', status: 1 }],
        currentShop: { id: 2, merchantId: 1, name: 'Shop', status: 1 },
      },
    })
    await store.fetchMe()
    expect(store.isApproved()).toBe(true)
    expect(store.activeShopId).toBe(2)
  })

  it('pending merchant is not approved', async () => {
    const store = useMerchantStore()
    vi.mocked(client.get).mockResolvedValue({
      code: 200,
      data: {
        merchant: { id: 1, userId: 1, name: 'A', status: 0 },
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
