/**
 * Mock Lebanese agricultural cooperative data for Week 1.
 * Replaced by real Spring Boot API responses later — services read from
 * here today and will read from HTTP tomorrow, keeping component code
 * unchanged.
 */
import type {
  Delivery,
  Farmer,
  InventoryItem,
  Payment,
  Product,
  User,
} from '@/types'

export const currentUser: User = {
  id: 'u-001',
  name: 'Rania Aoun',
  email: 'rania.aoun@sahla.app',
  role: 'manager',
  cooperative: 'Bekaa Valley Cooperative',
}

export const products: Product[] = [
  { id: 1, name: 'Apple', variety: 'Lebanese Red', unit: 'KG', active: true, createdAt: '2026-01-05T08:00:00Z', updatedAt: '2026-01-05T08:00:00Z' },
  { id: 2, name: 'Tomato', variety: 'Local', unit: 'KG', active: true, createdAt: '2026-01-05T08:00:00Z', updatedAt: '2026-01-05T08:00:00Z' },
  { id: 3, name: 'Potato', variety: 'Spunta', unit: 'KG', active: true, createdAt: '2026-01-05T08:00:00Z', updatedAt: '2026-01-05T08:00:00Z' },
  { id: 4, name: 'Orange', variety: 'Valencia', unit: 'KG', active: true, createdAt: '2026-01-05T08:00:00Z', updatedAt: '2026-01-05T08:00:00Z' },
]

export const farmers: Farmer[] = [
  { id: 1, farmerCode: 'F-00001', name: 'Ahmad Khalil', phone: '71234118', village: 'Kfarzabad', status: 'ACTIVE', createdAt: '2023-03-12T08:00:00Z', updatedAt: '2023-03-12T08:00:00Z' },
  { id: 2, farmerCode: 'F-00002', name: 'Joseph Hanna', phone: '70991233', village: 'Zahle', status: 'ACTIVE', createdAt: '2023-05-02T08:00:00Z', updatedAt: '2023-05-02T08:00:00Z' },
  { id: 3, farmerCode: 'F-00003', name: 'Maya Daher', phone: '76552907', village: 'Ablah', status: 'ACTIVE', createdAt: '2022-11-19T08:00:00Z', updatedAt: '2022-11-19T08:00:00Z' },
  { id: 4, farmerCode: 'F-00004', name: 'Elias Rahme', phone: '71800445', village: 'Bcharre', status: 'ACTIVE', createdAt: '2023-01-28T08:00:00Z', updatedAt: '2023-01-28T08:00:00Z' },
  { id: 5, farmerCode: 'F-00005', name: 'Fatima Zeaiter', phone: '78340662', village: 'Baalbek', status: 'ACTIVE', createdAt: '2023-06-15T08:00:00Z', updatedAt: '2023-06-15T08:00:00Z' },
  { id: 6, farmerCode: 'F-00006', name: 'Georges Nakhle', phone: '70118774', village: 'Batroun', status: 'ACTIVE', createdAt: '2022-08-04T08:00:00Z', updatedAt: '2022-08-04T08:00:00Z' },
  { id: 7, farmerCode: 'F-00007', name: 'Hassan Ismail', phone: '76245019', village: 'Halba', status: 'ACTIVE', createdAt: '2024-02-11T08:00:00Z', updatedAt: '2024-02-11T08:00:00Z' },
  { id: 8, farmerCode: 'F-00008', name: 'Nour Semaan', phone: '71667302', village: 'Jezzine', status: 'INACTIVE', createdAt: '2024-04-22T08:00:00Z', updatedAt: '2024-04-22T08:00:00Z' },
  { id: 9, farmerCode: 'F-00009', name: 'Khalil Abou Zeid', phone: '70559128', village: 'Qab Elias', status: 'ACTIVE', createdAt: '2023-02-09T08:00:00Z', updatedAt: '2023-02-09T08:00:00Z' },
  { id: 10, farmerCode: 'F-00010', name: 'Rita Khoury', phone: '78902351', village: 'Zahle', status: 'ACTIVE', createdAt: '2023-09-30T08:00:00Z', updatedAt: '2023-09-30T08:00:00Z' },
  { id: 11, farmerCode: 'F-00011', name: 'Samir Haddad', phone: '71443890', village: 'Bcharre', status: 'ACTIVE', createdAt: '2022-12-01T08:00:00Z', updatedAt: '2022-12-01T08:00:00Z' },
  { id: 12, farmerCode: 'F-00012', name: 'Layla Mansour', phone: '76771204', village: 'Rankous', status: 'INACTIVE', createdAt: '2024-01-17T08:00:00Z', updatedAt: '2024-01-17T08:00:00Z' },
]

export const deliveries: Delivery[] = [
  { id: 'D-5012', farmerId: 'F-1024', farmerName: 'Ahmad Khalil', product: 'Apples', netWeight: 500, grade: 'A', date: '2026-09-11', status: 'completed', unitPrice: 0.85 },
  { id: 'D-5013', farmerId: 'F-1025', farmerName: 'Joseph Hanna', product: 'Tomatoes', netWeight: 320, grade: 'B', date: '2026-09-11', status: 'grading' },
  { id: 'D-5014', farmerId: 'F-1026', farmerName: 'Maya Daher', product: 'Potatoes', netWeight: 750, grade: 'A', date: '2026-09-11', status: 'weighed' },
  { id: 'D-5015', farmerId: 'F-1027', farmerName: 'Elias Rahme', product: 'Apples', netWeight: 430, grade: 'A', date: '2026-09-11', status: 'confirmed', unitPrice: 0.9 },
  { id: 'D-5016', farmerId: 'F-1028', farmerName: 'Fatima Zeaiter', product: 'Cherries', netWeight: 180, grade: 'A', date: '2026-09-11', status: 'draft' },
  { id: 'D-5017', farmerId: 'F-1029', farmerName: 'Georges Nakhle', product: 'Grapes', netWeight: 610, grade: 'B', date: '2026-09-10', status: 'completed', unitPrice: 1.1 },
  { id: 'D-5018', farmerId: 'F-1032', farmerName: 'Khalil Abou Zeid', product: 'Tomatoes', netWeight: 290, grade: 'C', date: '2026-09-10', status: 'rejected' },
  { id: 'D-5019', farmerId: 'F-1030', farmerName: 'Hassan Ismail', product: 'Potatoes', netWeight: 820, grade: 'A', date: '2026-09-10', status: 'completed', unitPrice: 0.55 },
  { id: 'D-5020', farmerId: 'F-1033', farmerName: 'Rita Khoury', product: 'Cherries', netWeight: 145, grade: 'A', date: '2026-09-10', status: 'confirmed', unitPrice: 2.4 },
  { id: 'D-5021', farmerId: 'F-1034', farmerName: 'Samir Haddad', product: 'Apples', netWeight: 560, grade: 'B', date: '2026-09-09', status: 'completed', unitPrice: 0.7 },
  { id: 'D-5022', farmerId: 'F-1024', farmerName: 'Ahmad Khalil', product: 'Apples', netWeight: 470, grade: 'A', date: '2026-09-09', status: 'grading' },
  { id: 'D-5023', farmerId: 'F-1035', farmerName: 'Layla Mansour', product: 'Potatoes', netWeight: 300, grade: 'C', date: '2026-09-09', status: 'weighed' },
]

export const inventory: InventoryItem[] = [
  { id: 'I-01', product: 'Apples', grade: 'A', quantity: 4250, lastUpdated: '2026-09-11T09:20:00', status: 'in_stock' },
  { id: 'I-02', product: 'Apples', grade: 'B', quantity: 1180, lastUpdated: '2026-09-11T09:20:00', status: 'in_stock' },
  { id: 'I-03', product: 'Tomatoes', grade: 'A', quantity: 2140, lastUpdated: '2026-09-11T08:05:00', status: 'in_stock' },
  { id: 'I-04', product: 'Tomatoes', grade: 'B', quantity: 320, lastUpdated: '2026-09-11T08:05:00', status: 'low_stock' },
  { id: 'I-05', product: 'Potatoes', grade: 'A', quantity: 6830, lastUpdated: '2026-09-11T07:40:00', status: 'in_stock' },
  { id: 'I-06', product: 'Cherries', grade: 'A', quantity: 540, lastUpdated: '2026-09-10T16:30:00', status: 'low_stock' },
  { id: 'I-07', product: 'Grapes', grade: 'B', quantity: 1920, lastUpdated: '2026-09-10T15:10:00', status: 'in_stock' },
  { id: 'I-08', product: 'Olives', grade: 'A', quantity: 0, lastUpdated: '2026-09-08T11:00:00', status: 'out_of_stock' },
]

export const payments: Payment[] = [
  { id: 'PMT-301', farmerId: 'F-1024', farmerName: 'Ahmad Khalil', amountOwed: 1250, amountPaid: 0, remaining: 1250, status: 'pending' },
  { id: 'PMT-302', farmerId: 'F-1025', farmerName: 'Joseph Hanna', amountOwed: 980, amountPaid: 500, remaining: 480, status: 'partial', lastPaymentDate: '2026-09-05' },
  { id: 'PMT-303', farmerId: 'F-1026', farmerName: 'Maya Daher', amountOwed: 1420, amountPaid: 1420, remaining: 0, status: 'paid', lastPaymentDate: '2026-09-09' },
  { id: 'PMT-304', farmerId: 'F-1027', farmerName: 'Elias Rahme', amountOwed: 2100, amountPaid: 0, remaining: 2100, status: 'pending' },
  { id: 'PMT-305', farmerId: 'F-1028', farmerName: 'Fatima Zeaiter', amountOwed: 640, amountPaid: 320, remaining: 320, status: 'partial', lastPaymentDate: '2026-09-06' },
  { id: 'PMT-306', farmerId: 'F-1029', farmerName: 'Georges Nakhle', amountOwed: 1870, amountPaid: 1870, remaining: 0, status: 'paid', lastPaymentDate: '2026-09-08' },
  { id: 'PMT-307', farmerId: 'F-1032', farmerName: 'Khalil Abou Zeid', amountOwed: 760, amountPaid: 0, remaining: 760, status: 'pending' },
  { id: 'PMT-308', farmerId: 'F-1033', farmerName: 'Rita Khoury', amountOwed: 900, amountPaid: 360, remaining: 540, status: 'partial', lastPaymentDate: '2026-09-07' },
]

export const lebaneseRegions = [
  'Bekaa',
  'Zahle',
  'Akkar',
  'Bcharre',
  'Batroun',
  'Jezzine',
] as const
