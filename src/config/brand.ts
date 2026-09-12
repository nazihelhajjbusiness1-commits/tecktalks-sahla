/**
 * Centralized brand configuration.
 * Keep all product naming/branding here so it can be swapped without
 * touching component code. "Mawsim" (موسم — "harvest season") is a
 * temporary placeholder name.
 */
export const brand = {
  name: 'Mawsim',
  /** Short tagline shown near the logo / on the login screen. */
  tagline: 'Agricultural Cooperative Management',
  /** One-line supporting description. */
  description:
    'From delivery to payment — one place to run your produce collection center.',
  /** Two-letter monogram used in the logo mark placeholder. */
  monogram: 'M',
  supportEmail: 'support@mawsim.app',
  copyrightHolder: 'Mawsim',
} as const

export type Brand = typeof brand
