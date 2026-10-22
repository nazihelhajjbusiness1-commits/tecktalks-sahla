import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, waitFor, cleanup, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { RecordPaymentModal } from './RecordPaymentModal'
import { paymentService } from '@/services/paymentService'
import type { CurrencyBalance } from '@/types'

vi.mock('@/services/paymentService', () => ({
  paymentService: { recordPayment: vi.fn() },
}))

const balances: CurrencyBalance[] = [{ currency: 'USD', outstanding: 100 }]

function renderModal() {
  const onRecorded = vi.fn()
  render(
    <RecordPaymentModal
      open
      farmerId={1}
      farmerName="Test Farmer"
      balances={balances}
      onClose={vi.fn()}
      onRecorded={onRecorded}
    />,
  )
  return { onRecorded }
}

describe('RecordPaymentModal', () => {
  beforeEach(() => {
    vi.mocked(paymentService.recordPayment).mockReset()
  })

  afterEach(() => cleanup())

  it('rejects a zero amount without calling the API', async () => {
    renderModal()
    await userEvent.click(screen.getByRole('button', { name: /record payment/i }))
    expect(screen.getByText(/greater than zero/i)).toBeTruthy()
    expect(paymentService.recordPayment).not.toHaveBeenCalled()
  })

  it('rejects an overpayment above the outstanding balance', async () => {
    renderModal()
    fireEvent.change(screen.getByLabelText('Amount'), { target: { value: '150' } })
    await userEvent.click(screen.getByRole('button', { name: /record payment/i }))
    expect(screen.getByText(/exceeds the outstanding balance/i)).toBeTruthy()
    expect(paymentService.recordPayment).not.toHaveBeenCalled()
  })

  it('"Pay full" fills the balance and records a valid payment', async () => {
    vi.mocked(paymentService.recordPayment).mockResolvedValue({
      id: 1,
      farmerId: 1,
      amount: 100,
      currency: 'USD',
      paymentMethod: 'CASH',
      referenceNumber: null,
      notes: null,
      paidAt: '2026-10-05T00:00:00',
      recordedBy: 'Acc',
      status: 'RECORDED',
      createdAt: '2026-10-05T00:00:00',
      outstandingAfter: 0,
    })
    const { onRecorded } = renderModal()

    await userEvent.click(screen.getByRole('button', { name: /pay full/i }))
    await userEvent.click(screen.getByRole('button', { name: /record payment/i }))

    await waitFor(() => expect(paymentService.recordPayment).toHaveBeenCalledTimes(1))
    expect(paymentService.recordPayment).toHaveBeenCalledWith(
      expect.objectContaining({ farmerId: 1, amount: 100, currency: 'USD', paymentMethod: 'CASH' }),
    )
    expect(onRecorded).toHaveBeenCalled()
  })
})
