import { defineConfig, mergeConfig } from 'vitest/config'
import viteConfig from './vite.config'

// Reuse the app's Vite config (React plugin + `@` alias) and add the test env.
export default mergeConfig(
  viteConfig,
  defineConfig({
    test: {
      environment: 'jsdom',
      css: false,
      include: ['src/**/*.{test,spec}.{ts,tsx}'],
    },
  }),
)
