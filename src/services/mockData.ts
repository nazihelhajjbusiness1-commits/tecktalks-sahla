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
  { id: 'p-apple', name: 'Apples', unit: 'kg', category: 'Fruit' },
  { id: 'p-tomato', name: 'Tomatoes', unit: 'kg', category: 'Vegetable' },
  { id: 'p-potato', name: 'Potatoes', unit: 'kg', category: 'Vegetable' },
  { id: 'p-cherry', name: 'Cherries', unit: 'kg', category: 'Fruit' },
  { id: 'p-grape', name: 'Grapes', unit: 'kg', category: 'Fruit' },
  { id: 'p-olive', name: 'Olives', unit: 'kg', category: 'Fruit' },
]

export const farmers: Farmer[] = [
  { id: 'F-1024', name: 'Ahmad Khalil', village: 'Kfarzabad', region: 'Bekaa', phone: '+961 71 234 118', mainCrop: 'Apples', totalDeliveries: 42, balance: 1250, status: 'active', joinedAt: '2023-03-12' },
  { id: 'F-1025', name: 'Joseph Hanna', village: 'Zahle', region: 'Zahle', phone: '+961 70 991 233', mainCrop: 'Tomatoes', totalDeliveries: 31, balance: 480, status: 'active', joinedAt: '2023-05-02' },
  { id: 'F-1026', name: 'Maya Daher', village: 'Ablah', region: 'Bekaa', phone: '+961 76 552 907', mainCrop: 'Potatoes', totalDeliveries: 58, balance: 0, status: 'active', joinedAt: '2022-11-19' },
  { id: 'F-1027', name: 'Elias Rahme', village: 'Bcharre', region: 'Bcharre', phone: '+961 71 800 445', mainCrop: 'Apples', totalDeliveries: 27, balance: 2100, status: 'active', joinedAt: '2023-01-28' },
  { id: 'F-1028', name: 'Fatima Zeaiter', village: 'Baalbek', region: 'Bekaa', phone: '+961 78 340 662', mainCrop: 'Cherries', totalDeliveries: 19, balance: 320, status: 'active', joinedAt: '2023-06-15' },
  { id: 'F-1029', name: 'Georges Nakhle', village: 'Batroun', region: 'Batroun', phone: '+961 70 118 774', mainCrop: 'Grapes', totalDeliveries: 64, balance: 0, status: 'active', joinedAt: '2022-08-04' },
  { id: 'F-1030', name: 'Hassan Ismail', village: 'Halba', region: 'Akkar', phone: '+961 76 245 019', mainCrop: 'Potatoes', totalDeliveries: 12, balance: 90, status: 'active', joinedAt: '2024-02-11' },
  { id: 'F-1031', name: 'Nour Semaan', village: 'Jezzine', region: 'Jezzine', phone: '+961 71 667 302', mainCrop: 'Olives', totalDeliveries: 8, balance: 0, status: 'inactive', joinedAt: '2024-04-22' },
  { id: 'F-1032', name: 'Khalil Abou Zeid', village: 'Qab Elias', region: 'Bekaa', phone: '+961 70 559 128', mainCrop: 'Tomatoes', totalDeliveries: 46, balance: 760, status: 'active', joinedAt: '2023-02-09' },
  { id: 'F-1033', name: 'Rita Khoury', village: 'Zahle', region: 'Zahle', phone: '+961 78 902 351', mainCrop: 'Cherries', totalDeliveries: 22, balance: 540, status: 'active', joinedAt: '2023-09-30' },
  { id: 'F-1034', name: 'Samir Haddad', village: 'Bcharre', region: 'Bcharre', phone: '+961 71 443 890', mainCrop: 'Apples', totalDeliveries: 37, balance: 0, status: 'active', joinedAt: '2022-12-01' },
  { id: 'F-1035', name: 'Layla Mansour', village: 'Rankous', region: 'Akkar', phone: '+961 76 771 204', mainCrop: 'Potatoes', totalDeliveries: 15, balance: 210, status: 'inactive', joinedAt: '2024-01-17' },
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
