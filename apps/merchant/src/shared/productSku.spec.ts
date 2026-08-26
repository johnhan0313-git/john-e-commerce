import { describe, expect, it } from 'vitest'
import { cartesian, regenerateSkuDrafts, yuanToCents, formatCents } from '@john/fe-shared'

describe('productSku', () => {
  it('cartesian expands attrs', () => {
    const combos = cartesian([
      { name: '颜色', values: ['红', '蓝'] },
      { name: '尺码', values: ['S'] },
    ])
    expect(combos).toHaveLength(2)
    expect(combos[0]).toEqual({ 颜色: '红', 尺码: 'S' })
  })

  it('regenerate keeps previous price by spec', () => {
    const next = regenerateSkuDrafts({
      salesAttrs: [{ name: '颜色', values: ['红'] }],
      previous: [{ skuName: '旧', skuCode: 'a', price: 12.5, initStock: 3, specValues: { 颜色: '红' } }],
      productName: 'T',
      defaultPrice: 99,
      defaultStock: 0,
    })
    expect(next[0].price).toBe(12.5)
  })
})

describe('money', () => {
  it('rounds yuan to cents', () => {
    expect(yuanToCents(99)).toBe(9900)
    expect(formatCents(9900)).toBe('99.00')
  })
})
