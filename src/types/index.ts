/**
 * Domain types for Mawsim.
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

export interface Product {
  id: string
  name: string
  /** e.g. "kg" — kept flexible for future crates/boxes. */
  unit: string
  /** Optional produce category for grouping. */
  category?: string
}

/* ------------------------------------------------------------------ */
/* Farmers                                                             */
/* ------------------------------------------------------------------ */

export type FarmerStatus = 'active' | 'inactive'

export interface Farmer {
  id: string
  name: string
  village: string
  region: string
  phone: string
  mainCrop: string
  totalDeliveries: number
  /** Outstanding balance owed to the farmer, in USD. */
  balance: number
  status: FarmerStatus
  joinedAt: string // ISO date
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
