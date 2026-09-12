/**
 * Display formatters. Centralized so currency/weight/date presentation
 * stays consistent across every screen (and can be localized for Arabic
 * later by swapping the locale argument).
 */

const LOCALE = 'en-US'

/** USD money, no cents for whole amounts to stay scannable in tables. */
export function formatCurrency(value: number): string {
  return new Intl.NumberFormat(LOCALE, {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: value % 1 === 0 ? 0 : 2,
  }).format(value)
}

/** Kilograms with thousands separators, e.g. "8,420 kg". */
export function formatWeight(kg: number): string {
  return `${new Intl.NumberFormat(LOCALE).format(kg)} kg`
}

/** Plain grouped number, e.g. "1,146". */
export function formatNumber(value: number): string {
  return new Intl.NumberFormat(LOCALE).format(value)
}

/** Short human date, e.g. "11 Sep 2026". */
export function formatDate(iso: string): string {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return new Intl.DateTimeFormat(LOCALE, {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  }).format(d)
}

/** Date + time, e.g. "11 Sep 2026, 09:20". */
export function formatDateTime(iso: string): string {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return iso
  return new Intl.DateTimeFormat(LOCALE, {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(d)
}

/** Relative-ish "time ago" for the Last Updated column. */
export function timeAgo(iso: string): string {
  const then = new Date(iso).getTime()
  if (Number.isNaN(then)) return iso
  const diffMs = Date.now() - then
  const mins = Math.round(diffMs / 60000)
  if (mins < 1) return 'just now'
  if (mins < 60) return `${mins} min ago`
  const hours = Math.round(mins / 60)
  if (hours < 24) return `${hours} h ago`
  const days = Math.round(hours / 24)
  if (days < 30) return `${days} d ago`
  return formatDate(iso)
}

/** "RA" from "Rania Aoun" — used for avatar fallbacks. */
export function initials(name: string): string {
  return name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0]?.toUpperCase() ?? '')
    .join('')
}
