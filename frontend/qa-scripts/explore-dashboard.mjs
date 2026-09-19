import { chromium } from 'playwright'

const browser = await chromium.launch()
const page = await browser.newPage()
const failed = []
page.on('response', async (r) => {
  if (r.status() >= 400) {
    let body = ''
    try {
      body = (await r.text()).slice(0, 300)
    } catch {
      body = '(could not read body)'
    }
    failed.push({ url: r.url(), status: r.status(), body })
  }
})

await page.goto('http://localhost:5173/login')
await page.getByLabel('Email or username').fill('manager@sahla.demo')
await page.locator('input[type="password"]').fill('SahlaDemo123!')
await Promise.all([
  page.waitForResponse((r) => r.url().includes('/api/auth/login')),
  page.getByRole('button', { name: /sign in/i }).click(),
])
await page.waitForTimeout(1500)

console.log('Current URL:', page.url())
console.log('Failed requests:', JSON.stringify(failed, null, 2))
await page.screenshot({ path: 'qa-scripts/screenshots/dashboard-real-backend.png', fullPage: true })

await browser.close()
