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
  id: number
  userId: number
  name: string
  logo?: string
  status: number
  statusLabel?: string
  contactName?: string
  contactPhone?: string
}

export interface Shop {
  id: number
  merchantId: number
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

export interface Spu {
  id: number
  name: string
  subtitle?: string
  status: number
  shopId?: number
  merchantId?: number
}

export interface Sku {
  id: number
  spuId: number
  skuName: string
  price: number
  status: number
}

export interface Order {
  id: number
  orderNo: string
  status: number
  statusLabel?: string
  payStatus?: number
  payStatusLabel?: string
  payAmount?: number
  paidAmount?: number
  shopId?: number
  merchantId?: number
  items?: Array<{ id: number; skuName: string; quantity: number; price: number }>
}
