/**
 * Centralized brand configuration.
 * Keep all product naming/branding here so it can be swapped without
 * touching component code. "Sahla" (سهلة — "simple / easy") reflects the
 * goal of making cooperative management effortless.
 */
export const brand = {
  name: 'Sahla',
  /** Short tagline shown near the logo / on the login screen. */
  tagline: 'Agricultural Cooperative Management',
  /** One-line supporting description. */
  description:
    'From delivery to payment — one place to run your produce collection center.',
  /** Two-letter monogram used in the logo mark placeholder. */
  monogram: 'S',
  supportEmail: 'support@sahla.app',
  copyrightHolder: 'Sahla',
} as const

export type Brand = typeof brand
