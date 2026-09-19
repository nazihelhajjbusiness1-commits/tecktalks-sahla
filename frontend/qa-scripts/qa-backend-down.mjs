import { chromium } from 'playwright'

const browser = await chromium.launch()
const page = await browser.newPage()

await page.goto('http://localhost:5173/login')
await page.getByLabel('Email or username').fill('manager@sahla.demo')
await page.locator('input[type="password"]').fill('SahlaDemo123!')
await Promise.all([
  page.waitForResponse((r) => r.url().includes('/api/auth/login')),
  page.getByRole('button', { name: /sign in/i }).click(),
])
await page.waitForTimeout(500)

// Simulate backend unavailable by blocking all requests to it.
await page.route('http://localhost:8080/**', (route) => route.abort('connectionrefused'))

await page.goto('http://localhost:5173/farmers')
await page.waitForTimeout(1500)
console.log('URL:', page.url())
await page.screenshot({ path: 'qa-scripts/screenshots/backend-down-farmers.png', fullPage: true })

await browser.close()
