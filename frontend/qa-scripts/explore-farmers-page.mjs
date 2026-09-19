import { chromium } from 'playwright'

const browser = await chromium.launch()
const page = await browser.newPage()
const consoleErrors = []
page.on('console', (msg) => {
  if (msg.type() === 'error') consoleErrors.push(msg.text())
})
page.on('pageerror', (err) => consoleErrors.push('pageerror: ' + err.message))

await page.goto('http://localhost:5173/login')
await page.getByLabel('Email or username').fill('manager@sahla.demo')
await page.locator('input[type="password"]').fill('SahlaDemo123!')
await Promise.all([
  page.waitForResponse((r) => r.url().includes('/api/auth/login')),
  page.getByRole('button', { name: /sign in/i }).click(),
])
await page.waitForTimeout(500)

const [farmersResponse] = await Promise.all([
  page.waitForResponse((r) => r.url().includes('/api/farmers')),
  page.goto('http://localhost:5173/farmers'),
])
console.log('GET /api/farmers status:', farmersResponse.status())
console.log('GET /api/farmers body (first 500 chars):', (await farmersResponse.text()).slice(0, 500))

await page.waitForTimeout(1000)
console.log('Console/page errors:', JSON.stringify(consoleErrors, null, 2))
await page.screenshot({ path: 'qa-scripts/screenshots/farmers-page-real-backend.png', fullPage: true })

await browser.close()
