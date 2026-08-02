export interface R<T> {
  code: number
  message?: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

export interface Merchant {
  id: number | string
  userId: number | string
  name: string
  logo?: string
  licenseNo?: string
  status: number
  statusLabel?: string
  contactName?: string
  contactPhone?: string
}

export interface Shop {
  id: number | string
  merchantId: number | string
  name: string
  logo?: string
  status: number
  statusLabel?: string
}

export interface MerchantMe {
  merchant: Merchant
  shops?: Shop[]
  currentShop?: Shop | null
}

export interface SalesAttr {
  name: string
  values: string[]
}

export interface Spu {
  id: number | string
  name: string
  subtitle?: string
  mainImages?: string[]
  detail?: string
  status: number
  shopId?: number | string
  merchantId?: number | string
  salesAttrs?: SalesAttr[]
}

export interface Sku {
  id: number | string
  spuId: number | string
  skuName: string
  skuCode?: string
  price: number
  status: number
  available?: number
  specValues?: Record<string, string>
}

export interface Order {
  id: number | string
  orderNo: string
  status: number
  statusLabel?: string
  payStatus?: number
  payStatusLabel?: string
  payAmount?: number
  paidAmount?: number
  shopId?: number | string
  merchantId?: number | string
  items?: Array<{ id: number | string; skuName: string; quantity: number; price: number }>
}
