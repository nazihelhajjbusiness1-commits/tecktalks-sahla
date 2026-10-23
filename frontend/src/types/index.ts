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

/** Backend Role enum values (used by the admin user-management screen). */
export type BackendRole =
  | 'ADMIN'
  | 'MANAGER'
  | 'RECEIVING_EMPLOYEE'
  | 'ACCOUNTANT'
  | 'WAREHOUSE_EMPLOYEE'
  | 'INSPECTOR'

/** Mirrors the backend UserResponse DTO. */
export interface StaffUser {
  id: number
  firstname: string
  lastname: string
  username: string
  email: string
  phoneNumber?: string | null
  role: BackendRole
  createdAt: string
}

/** Mirrors the backend RegisterRequest DTO (admin creates staff accounts). */
export interface CreateStaffUserRequest {
  firstname: string
  lastname: string
  username: string
  email: string
  phoneNumber?: string
  password: string
  role: BackendRole
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
/* Sprint 3 — Delivery intake (backend REST contracts)                 */
/* ------------------------------------------------------------------ */

/**
 * Backend delivery lifecycle, owned by Spring Boot. Distinct from the legacy
 * lowercase `DeliveryStatus` above, which still backs the mock dashboard.
 */
export type IntakeStatus =
  | 'PENDING'
  | 'WEIGHED'
  | 'GRADED'
  | 'CONFIRMED'
  | 'DELIVERED'
  | 'REJECTED'
  | 'CANCELLED'

/** Mirrors the backend DeliveryResponse DTO. */
export interface DeliveryResponse {
  id: number
  farmerCode: string
  farmerName: string
  productName: string
  quantity: number
  unit: string
  deliveryDate: string // ISO date
  createdBy: string | null
  notes: string | null
  status: IntakeStatus
  totalPrice: number | null
  totalPriceCurrency: Currency | null
  createdAt: string // ISO date-time
  updatedAt: string // ISO date-time
}

export interface CreateDeliveryRequest {
  farmerId: number
  productId: number
  quantity: number
  deliveryDate: string
  notes?: string
}

export interface CaptureWeightRequest {
  grossWeight: number
  tareWeight: number
}

/** Mirrors the backend DeliveryWeightResponse DTO. */
export interface DeliveryWeightResponse {
  id: number
  deliveryId: number
  grossWeight: number
  tareWeight: number
  netWeight: number
  unit: string
  weighedBy: string | null
  weighedAt: string
  createdAt: string
  updatedAt: string
}

export interface CaptureGradeRequest {
  /** Grade code matching a product GradeDefinition, e.g. "GRADE_A". */
  grade: string
  /** Optional inspector note / reason (max 500 chars). */
  notes?: string
}

/** One row of the quality grade distribution. */
export interface GradeCount {
  gradeCode: string
  gradeName: string
  count: number
}

/** Mirrors the backend QualitySummaryResponse DTO (Inspector dashboard). */
export interface QualitySummary {
  totalGraded: number
  rejectCount: number
  /** Fraction 0..1 of graded deliveries marked REJECT. */
  rejectRate: number
  distribution: GradeCount[]
}

/** Mirrors the backend DeliveryGradeResponse DTO. */
export interface DeliveryGradeResponse {
  id: number
  deliveryId: number
  gradeDefinitionId: number
  gradeCode: string
  gradeName: string
  gradedBy: string | null
  gradedAt: string
  notes: string | null
  resultingStatus: IntakeStatus
  totalPrice: number | null
  totalPriceCurrency: Currency | null
  createdAt: string
  updatedAt: string
}

/** Mirrors the backend DeliveryAuditLogResponse DTO (DT-57). */
export interface DeliveryAuditEntry {
  id: number
  action: string
  previousStatus: string | null
  newStatus: string
  performedBy: string
  notes: string | null
  createdAt: string
}

/** Query params for the paginated + filtered delivery list endpoint. */
export interface DeliveryListParams {
  farmerId?: number
  productId?: number
  status?: IntakeStatus
  from?: string
  to?: string
  page?: number
  size?: number
}

/* ------------------------------------------------------------------ */
/* Sprint 4 — Settlement, ledger & payments (backend REST contracts)   */
/* ------------------------------------------------------------------ */

export type SettlementStatus = 'DRAFT' | 'CALCULATED' | 'CONFIRMED' | 'VOIDED'
export type DeductionType = 'TRANSPORT' | 'PACKAGING' | 'SERVICE_FEE' | 'OTHER'
export type PaymentMethod = 'CASH' | 'BANK_TRANSFER' | 'CHECK' | 'OTHER'
export type FarmerPaymentStatus = 'RECORDED' | 'REVERSED'

/** POST /deliveries/{id}/settlement/preview */
export interface SettlementPreview {
  deliveryId: number
  grade: string | null
  acceptedWeight: number | null
  unitPrice: number | null
  currency: Currency | null
  priceRuleId: number | null
}

export interface DeductionLine {
  type: DeductionType
  description: string
  amount: number
}

/** POST /deliveries/{id}/settlement/calculate */
export interface SettlementCalculationRequest {
  currency: Currency
  commissionRate: number
  deductions: DeductionLine[]
}

export interface SettlementCalculation {
  settlementId: number
  deliveryId: number
  grade: string | null
  acceptedWeight: number
  unitPrice: number
  currency: Currency
  grossAmount: number
  commissionRate: number
  commissionAmount: number
  deductions: DeductionLine[]
  deductionsTotal: number
  netAmount: number
  status: SettlementStatus
  calculatedAt: string | null
  sourcePriceRuleId: number | null
}

/** GET /deliveries/{id}/settlement — the stored FarmerSettlement. */
export interface FarmerSettlement {
  id: number
  deliveryId: number
  farmerId: number
  unitPriceSnapshot: number | null
  currency: Currency | null
  acceptedWeightSnapshot: number | null
  grossAmount: number | null
  commissionAmount: number | null
  deductionsTotal: number | null
  netAmount: number | null
  sourcePriceRuleId: number | null
  status: SettlementStatus
  calculatedAt: string | null
  confirmedAt: string | null
  confirmedBy: string | null
  createdAt: string
  updatedAt: string
}

export interface CurrencyBalance {
  currency: Currency
  outstanding: number
}

export interface FarmerBalances {
  farmerId: number
  balances: CurrencyBalance[]
}

export interface OutstandingFarmer {
  farmerId: number
  farmerCode: string | null
  farmerName: string | null
  balances: CurrencyBalance[]
}

export interface StatementLine {
  id: number
  occurredAt: string
  entryType: string
  description: string | null
  settlementId: number | null
  paymentId: number | null
  currency: Currency
  debit: number | null
  credit: number | null
  runningBalance: number
}

export interface RecordPaymentRequest {
  farmerId: number
  amount: number
  currency: Currency
  paymentMethod: PaymentMethod
  referenceNumber?: string
  notes?: string
  paidAt?: string
}

export interface FarmerPayment {
  id: number
  farmerId: number
  amount: number
  currency: Currency
  paymentMethod: PaymentMethod
  referenceNumber: string | null
  notes: string | null
  paidAt: string
  recordedBy: string | null
  status: FarmerPaymentStatus
  createdAt: string
  outstandingAfter: number | null
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

/** A money amount tagged with its currency (USD/LBP never combined). */
export interface DashboardCurrencyAmount {
  currency: Currency
  amount: number
}

export interface DashboardRecentDelivery {
  id: number
  farmerName: string | null
  productName: string | null
  quantity: number
  unit: string
  status: string
  deliveryDate: string
}

export interface DashboardRecentPayment {
  id: number
  farmerName: string | null
  amount: number
  currency: Currency
  paidAt: string
}

/** Mirrors the backend DashboardStatsResponse DTO (DT-82). */
export interface DashboardData {
  todaysDeliveries: number
  produceReceivedTodayKg: number
  activeFarmers: number
  currentInventoryKg: number
  amountOwedToFarmers: DashboardCurrencyAmount[]
  paymentsToday: DashboardCurrencyAmount[]
  recentDeliveries: DashboardRecentDelivery[]
  recentPayments: DashboardRecentPayment[]
}

/* ------------------------------------------------------------------ */
/* Reports (Sprint 5, DT-83/87)                                        */
/* ------------------------------------------------------------------ */

/** Mirrors the backend DailyCollectionRow DTO. */
export interface DailyCollectionRow {
  deliveryDate: string
  farmerId: number
  farmerCode: string | null
  farmerName: string | null
  productId: number
  productName: string | null
  gradeId: number | null
  gradeCode: string | null
  gradeName: string | null
  netWeight: number
  unit: string
  unitPrice: number
  deliveryValue: number
  currency: Currency
}

/** Mirrors the backend InventoryReportRow DTO. */
export interface InventoryReportRow {
  productId: number
  productName: string | null
  gradeId: number | null
  gradeCode: string | null
  gradeName: string | null
  quantity: number
  unit: string
}

/** Mirrors the backend OutstandingPaymentsRow DTO. */
export interface OutstandingPaymentsRow {
  farmerId: number
  farmerCode: string | null
  farmerName: string | null
  currency: Currency
  amountOwed: number
  amountPaid: number
  remaining: number
  lastPaymentAt: string | null
  lastPaymentAmount: number | null
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
