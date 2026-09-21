/**
 * Domain types for Sahla.
 * These mirror the shapes we expect the Spring Boot REST API to return,
 * so services can swap mock data for real HTTP responses without changing
 * component code.
 */

/* ------------------------------------------------------------------ */
/* Users & auth                                                        */
/* ------------------------------------------------------------------ */

export type UserRole =
  | 'admin'
  | 'manager'
  | 'receiving'
  | 'inspector'
  | 'accountant'
  | 'warehouse'

export interface User {
  id: string
  name: string
  email: string
  role: UserRole
  /** Cooperative / collection center the user belongs to. */
  cooperative: string
  avatarUrl?: string
}

export interface Credentials {
  identifier: string // email or username
  password: string
}

export interface AuthSession {
  user: User
  token: string
}

/* ------------------------------------------------------------------ */
/* Products & grading                                                  */
/* ------------------------------------------------------------------ */

export type ProductGrade = 'A' | 'B' | 'C' | 'Ungraded'

/** Sellable units supported by the backend product catalog. */
export type ProductUnit = 'KG' | 'TON' | 'BOX' | 'CRATE'

/** Mirrors the backend ProductResponse DTO. */
export interface Product {
  id: number
  name: string
  variety: string
  unit: ProductUnit
  active: boolean
  createdAt: string
  updatedAt: string
}

export interface CreateProductRequest {
  name: string
  variety: string
  unit: ProductUnit
}

export interface UpdateProductRequest {
  name: string
  variety: string
  unit: ProductUnit
  active: boolean
}

/* ------------------------------------------------------------------ */
/* Grade definitions (per product)                                     */
/* ------------------------------------------------------------------ */

/** Mirrors the backend GradeDefinitionResponse DTO. */
export interface GradeDefinition {
  id: number
  productId: number
  gradeCode: string
  name: string
  description?: string
  displayOrder: number
  active: boolean
}

export interface CreateGradeRequest {
  gradeCode: string
  name: string
  description?: string
  displayOrder: number
  active?: boolean
}

export interface UpdateGradeRequest {
  name: string
  description?: string
  displayOrder: number
  active: boolean
}

/* ------------------------------------------------------------------ */
/* Grade-based pricing                                                  */
/* ------------------------------------------------------------------ */

export type Currency = 'USD' | 'LBP'

/** Mirrors the backend PriceRuleResponse DTO. */
export interface PriceRule {
  id: number
  productId: number
  gradeId: number
  /** Monetary amount (BigDecimal on the backend). */
  amount: number
  currency: Currency
  effectiveFrom: string // ISO date-time
  effectiveTo: string | null // ISO date-time, open-ended when null
  active: boolean
}

export interface CreatePriceRequest {
  gradeId: number
  amount: number
  currency: Currency
  effectiveFrom: string
  effectiveTo?: string | null
  active?: boolean
}

export interface UpdatePriceRequest {
  amount: number
  currency: Currency
  effectiveFrom: string
  effectiveTo?: string | null
  active: boolean
}

/* ------------------------------------------------------------------ */
/* Farmers                                                             */
/* ------------------------------------------------------------------ */

export type FarmerStatus = 'ACTIVE' | 'INACTIVE'

/** Mirrors the backend FarmerResponse DTO. */
export interface Farmer {
  id: number
  farmerCode: string
  name: string
  phone: string
  village: string
  status: FarmerStatus
  createdAt: string // ISO date-time
  updatedAt: string // ISO date-time
}

export interface CreateFarmerRequest {
  /** Optional — the backend auto-generates a readable code when omitted. */
  farmerCode?: string
  name: string
  phone: string
  village: string
}

export interface UpdateFarmerRequest {
  name: string
  phone: string
  village: string
  status?: FarmerStatus
}

/* ------------------------------------------------------------------ */
/* Deliveries                                                          */
/* ------------------------------------------------------------------ */

export type DeliveryStatus =
  | 'draft'
  | 'weighed'
  | 'grading'
  | 'confirmed'
  | 'completed'
  | 'rejected'

export interface Delivery {
  id: string
  farmerId: string
  farmerName: string
  product: string
  /** Net weight in kilograms. */
  netWeight: number
  grade: ProductGrade
  date: string // ISO date
  status: DeliveryStatus
  /** Unit price applied at pricing step, in USD/kg (optional pre-pricing). */
  unitPrice?: number
}

/* ------------------------------------------------------------------ */
/* Inventory                                                           */
/* ------------------------------------------------------------------ */

export type InventoryStatus = 'in_stock' | 'low_stock' | 'out_of_stock'

export interface InventoryItem {
  id: string
  product: string
  grade: ProductGrade
  /** Available quantity in kilograms. */
  quantity: number
  lastUpdated: string // ISO date-time
  status: InventoryStatus
}

/* ------------------------------------------------------------------ */
/* Payments                                                            */
/* ------------------------------------------------------------------ */

export type PaymentStatus = 'paid' | 'partial' | 'pending'

export interface Payment {
  id: string
  farmerId: string
  farmerName: string
  amountOwed: number
  amountPaid: number
  /** Derived: amountOwed - amountPaid, but stored for API parity. */
  remaining: number
  status: PaymentStatus
  lastPaymentDate?: string // ISO date
}

/* ------------------------------------------------------------------ */
/* Dashboard                                                           */
/* ------------------------------------------------------------------ */

export interface DashboardStats {
  todaysDeliveries: number
  produceReceivedKg: number
  activeFarmers: number
  paymentsDueUsd: number
  inventoryKg: number
}

export interface InventorySummaryRow {
  label: string
  quantityKg: number
}

export interface PaymentsBreakdown {
  paid: number
  partial: number
  pending: number
}

/* ------------------------------------------------------------------ */
/* Generic API helpers                                                 */
/* ------------------------------------------------------------------ */

export interface Paginated<T> {
  items: T[]
  total: number
  page: number
  pageSize: number
}

/** Mirrors Spring Data's Page<T> JSON shape returned by list endpoints. */
export interface PagedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  /** Zero-based current page index. */
  number: number
  size: number
}

/** Common query params for paginated + searchable list endpoints. */
export interface ListParams {
  search?: string
  page?: number
  size?: number
}
