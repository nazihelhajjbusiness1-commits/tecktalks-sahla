import { chromium } from 'playwright'
import fs from 'node:fs'

const shots = 'qa-scripts/screenshots'
fs.mkdirSync(shots, { recursive: true })

const browser = await chromium.launch()
const page = await browser.newPage()

async function login() {
  await page.goto('http://localhost:5173/login')
  await page.getByLabel('Email or username').fill('manager@sahla.demo')
  await page.locator('input[type="password"]').fill('SahlaDemo123!')
  await Promise.all([
    page.waitForResponse((r) => r.url().includes('/api/auth/login')),
    page.getByRole('button', { name: /sign in/i }).click(),
  ])
  await page.waitForTimeout(500)
}

console.log('--- 1. Login ---')
await login()
console.log('URL after login:', page.url())

console.log('--- 2. Navigate to Farmers, search ---')
await page.goto('http://localhost:5173/farmers')
await page.waitForTimeout(500)
await page.getByPlaceholder('Search by name, ID, or village…').fill('Zahle')
await page.waitForTimeout(300)
const rowCount = await page.locator('table tbody tr').count()
console.log('Rows after searching "Zahle":', rowCount)
await page.screenshot({ path: `${shots}/find-farmer-search.png` })
await page.getByPlaceholder('Search by name, ID, or village…').fill('')

console.log('--- 3. Attempt Add Farmer ---')
await page.getByRole('button', { name: 'Add Farmer' }).click()
await page.waitForTimeout(300)
await page.screenshot({ path: `${shots}/add-farmer-modal.png` })
const modalText = await page.locator('[role="dialog"], .modal, [data-modal]').first().textContent().catch(() => null)
console.log('Add Farmer modal text:', modalText)
// Try clicking Save and confirm no new row appears / no POST fires.
let createPostFired = false
page.on('request', (req) => {
  if (req.url().includes('/api/farmers') && req.method() === 'POST') createPostFired = true
})
await page.getByRole('button', { name: 'Save Farmer' }).click()
await page.waitForTimeout(500)
console.log('POST /api/farmers fired when clicking Save Farmer:', createPostFired)

console.log('--- 4. Open a farmer detail page, look for Edit ---')
await page.goto('http://localhost:5173/farmers')
await page.waitForTimeout(500)
await page.locator('table tbody tr').first().click()
await page.waitForTimeout(500)
console.log('Detail page URL:', page.url())
const editButtonCount = await page.getByRole('button', { name: /edit/i }).count()
console.log('Edit button present on farmer detail page:', editButtonCount > 0)
await page.screenshot({ path: `${shots}/farmer-detail-page.png`, fullPage: true })

console.log('--- 5. Expired/invalid token ---')
await page.evaluate(() => localStorage.setItem('sahla.token', 'this-is-not-a-valid-jwt'))
await page.reload()
await page.waitForTimeout(800)
console.log('URL after reload with invalid token:', page.url())
await page.screenshot({ path: `${shots}/invalid-token-state.png`, fullPage: true })

console.log('--- 6. Missing token (logged out) entirely ---')
await page.evaluate(() => localStorage.clear())
await page.goto('http://localhost:5173/farmers')
await page.waitForTimeout(500)
console.log('URL when visiting /farmers with no token:', page.url())
await page.screenshot({ path: `${shots}/no-token-state.png`, fullPage: true })

await browser.close()
console.log('--- done ---')
