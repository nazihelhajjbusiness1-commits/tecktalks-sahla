/**
 * Tiny className combiner. Filters falsy values and joins with a space.
 * Kept dependency-free (no clsx/tailwind-merge) to keep the Week 1
 * bundle lean; component variant maps are written to avoid conflicts.
 */
export type ClassValue = string | number | false | null | undefined

export function cn(...values: ClassValue[]): string {
  return values.filter(Boolean).join(' ')
}
