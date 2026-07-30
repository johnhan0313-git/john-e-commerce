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

export interface UserInfo {
  id: number
  phone: string
  email?: string
  nickname?: string
  avatar?: string
  userType?: number
  status?: number
}

export interface LoginVO {
  token: string
  user: UserInfo
}

export interface Spu {
  id: number
  name: string
  subtitle?: string
  mainImages?: string[]
  detail?: string
  categoryId?: number
  brandId?: number
  status?: number
  sales?: number
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
}

export interface CartItem {
  id: number
  skuId: number
  skuName?: string
  spuId?: number
  spuName?: string
  quantity: number
  selected: number
  price?: number
}

export interface OrderItem {
  id: number
  spuId: number
  skuId: number
  skuName?: string
  skuImage?: string
  price: number
  quantity: number
  subtotal?: number
  payAmount?: number
}

export interface Order {
  id: number
  orderGroupNo?: string
  orderNo?: string
  status?: number
  statusLabel?: string
  totalAmount?: number
  payAmount?: number
  paidAmount?: number
  payStatus?: number
  payStatusLabel?: string
  items?: OrderItem[]
  createdAt?: number
}

export interface OrderGroup {
  orderGroupNo: string
  orderCount: number
  totalAmount: number
  discountAmount?: number
  payAmount: number
  groupStatus?: string
  orders: Order[]
}

export interface PayMethod {
  methodCode: string
  name: string
  iconUrl?: string
  sortOrder?: number
  status?: number
}

export interface Payment {
  id: number
  payNo: string
  methodCode: string
  amount: number
  status: number | string
  channelTradeNo?: string
}

export interface Banner {
  id: number
  title?: string
  imageUrl?: string
  linkUrl?: string
  position?: string
}
