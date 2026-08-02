export interface R<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

export interface LoginVO {
  token: string
  user: {
    id: number
    phone: string
    nickname?: string
    userType?: number
    identities?: string[]
  }
}

export interface Spu {
  id: number
  name: string
  subtitle?: string
  mainImages?: string[]
  detail?: string
  categoryId?: number
  brandId?: number
  merchantId?: number
  shopId?: number
  productCode?: string
  status?: number
  sales?: number
  createdAt?: number
}

export interface Sku {
  id: number
  spuId: number
  skuCode?: string
  skuName?: string
  specValues?: Record<string, string>
  price: number
  costPrice?: number
  status?: number
  available?: number
}

export interface Order {
  id: number
  orderNo?: string
  orderGroupNo?: string
  merchantId?: number
  shopId?: number
  status?: number
  statusLabel?: string
  payStatus?: number
  payStatusLabel?: string
  totalAmount?: number
  payAmount?: number
  paidAmount?: number
  items?: Array<{
    id: number
    skuId: number
    skuName?: string
    quantity: number
    price: number
    payAmount?: number
  }>
  createdAt?: number
}

export interface ModuleDef {
  id: number
  moduleCode: string
  moduleName: string
  description?: string
  status?: number
  sortOrder?: number
}

export interface TenantModule {
  id?: number
  moduleCode: string
  moduleName?: string
  status?: number
  expireAt?: number
}

export interface StatsOverview {
  gmv?: number
  orderCountByStatus?: Record<string, number>
  topSkus?: Array<{ skuId: number; skuName?: string; totalQty?: number }>
}
