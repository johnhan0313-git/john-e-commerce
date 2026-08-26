export type SalesAttr = { name: string; values: string[] }

export type SkuDraft = {
  id?: number | string
  skuName: string
  skuCode: string
  price: number
  initStock: number
  specValues: Record<string, string>
}

export function cartesian(attrs: SalesAttr[]): Record<string, string>[] {
  const valid = attrs
    .map((a) => ({
      name: a.name.trim(),
      values: a.values.map((v) => String(v).trim()).filter(Boolean),
    }))
    .filter((a) => a.name && a.values.length)
  if (!valid.length) return [{}]
  return valid.reduce<Record<string, string>[]>((acc, attr) => {
    const next: Record<string, string>[] = []
    for (const row of acc) {
      for (const v of attr.values) {
        next.push({ ...row, [attr.name]: v })
      }
    }
    return next
  }, [{}])
}

export function specKey(spec: Record<string, string>) {
  return Object.keys(spec)
    .sort()
    .map((k) => `${k}=${spec[k]}`)
    .join('|')
}

export function buildSkuName(productName: string, spec: Record<string, string>) {
  const parts = Object.values(spec).filter(Boolean)
  if (!parts.length) return productName || '默认规格'
  return `${productName || '商品'}-${parts.join('-')}`
}

export function regenerateSkuDrafts(input: {
  salesAttrs: SalesAttr[]
  previous: SkuDraft[]
  productName: string
  defaultPrice: number
  defaultStock: number
}): SkuDraft[] {
  const combos = cartesian(input.salesAttrs)
  const prev = new Map(input.previous.map((s) => [specKey(s.specValues), s]))
  return combos.map((spec) => {
    const old = prev.get(specKey(spec))
    return {
      id: old?.id,
      skuName: old?.skuName || buildSkuName(input.productName, spec),
      skuCode: old?.skuCode || '',
      price: old?.price ?? input.defaultPrice,
      initStock: old?.initStock ?? input.defaultStock,
      specValues: { ...spec },
    }
  })
}

export function attrsFromSkuSpecs(
  skus: { specValues?: Record<string, string> }[],
): SalesAttr[] {
  const map = new Map<string, Set<string>>()
  for (const s of skus) {
    const spec = s.specValues || {}
    for (const [k, v] of Object.entries(spec)) {
      if (!k || !v) continue
      if (!map.has(k)) map.set(k, new Set())
      map.get(k)!.add(String(v))
    }
  }
  return [...map.entries()].map(([name, values]) => ({
    name,
    values: [...values],
  }))
}
