import { describe, it, expect } from 'vitest'
import { formatMoney } from './format'

describe('formatMoney', () => {
  it('formats USD with two decimal places', () => {
    expect(formatMoney(550.5, 'USD')).toMatch(/550\.50/)
    expect(formatMoney(600, 'USD')).toContain('600')
  })

  it('formats LBP as a whole number (no decimals)', () => {
    const lbp = formatMoney(12000000, 'LBP')
    expect(lbp).toMatch(/12,000,000/)
    expect(lbp).not.toContain('.')
  })

  it('keeps USD and LBP as independent strings (no conversion/summing)', () => {
    const usd = formatMoney(570, 'USD')
    const lbp = formatMoney(12000000, 'LBP')
    expect(usd).not.toEqual(lbp)
    expect(usd).toContain('570')
    expect(lbp).toContain('12,000,000')
  })
})
