import { chromium } from 'playwright'

const browser = await chromium.launch()
const page = await browser.newPage()
await page.goto('http://localhost:5173/login')
await page.getByLabel('Email or username').fill('manager@sahla.demo')
await page.locator('input[type="password"]').fill('SahlaDemo123!')

const [response] = await Promise.all([
  page.waitForResponse((r) => r.url().includes('/api/auth/login')),
  page.getByRole('button', { name: /sign in/i }).click(),
])

console.log('Response status:', response.status())
console.log('Response body:', await response.text())
await page.waitForTimeout(500)
console.log('Current URL:', page.url())
await page.screenshot({ path: 'qa-scripts/screenshots/bug-login-broken.png', fullPage: true })

await browser.close()
