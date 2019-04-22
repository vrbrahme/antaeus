package io.pleo.antaeus.core.services

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.quartz.JobExecutionContext
import java.math.BigDecimal

class BillingServiceTest {
    private val invoiceService: InvoiceService = mockk(relaxed = true)
    private val paymentProvider: PaymentProvider = mockk(relaxed = true)

    private val billingService: BillingService = BillingService(paymentProvider, invoiceService)

    @BeforeEach
    fun `setup`() {
        clearAllMocks()
    }

    @Test
    fun `can process invoice by id if the invoice is charged`() {
        val invoiceId = 24
        val mockInvoice = Invoice(24, 2, Money(BigDecimal.valueOf(2300), Currency.GBP), InvoiceStatus.PENDING)
        every { invoiceService.fetch(invoiceId) } returns mockInvoice
        every { paymentProvider.charge(mockInvoice) } returns true

        assert(billingService.processInvoice(invoiceId))

        verify { invoiceService.markAsPaid(mockInvoice) }
    }

    @Test
    fun `does not process invoice if the invoice cannot be charged`() {
        val invoiceId = 24
        val mockInvoice = Invoice(24, 2, Money(BigDecimal.valueOf(2300), Currency.GBP), InvoiceStatus.PENDING)
        every { invoiceService.fetch(invoiceId) } returns mockInvoice
        every { paymentProvider.charge(mockInvoice) } returns false

        assert(!billingService.processInvoice(invoiceId))

        verify(exactly = 0) { invoiceService.markAsPaid(mockInvoice) }
    }

    @Test
    fun `can process all pending invoices if the invoices have been charged`() {
        val invoice1 = Invoice(1, 1, Money(BigDecimal.valueOf(10), Currency.DKK), InvoiceStatus.PENDING)
        val invoice2 = Invoice(2, 1, Money(BigDecimal.valueOf(10), Currency.DKK), InvoiceStatus.PENDING)
        val invoice3 = Invoice(3, 1, Money(BigDecimal.valueOf(10), Currency.DKK), InvoiceStatus.PENDING)
        every { invoiceService.getPendingInvoices(100) } returns listOfNotNull(invoice1, invoice2, invoice3)
        every { paymentProvider.charge(invoice1) } returns true
        every { paymentProvider.charge(invoice2) } returns false
        every { paymentProvider.charge(invoice3) } returns true

        assert(billingService.chargePendingInvoices() == 3)

        verify { invoiceService.markAsPaid(invoice1) }
        verify { invoiceService.markAsPaid(invoice3) }
    }
}