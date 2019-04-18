package io.pleo.antaeus.core.services

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Currency
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import io.pleo.antaeus.models.Money
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

class InvoiceServiceTest {
    private val dal = mockk<AntaeusDal>(relaxed = true) {
        every { fetchInvoice(404) } returns null
    }

    private val invoiceService = InvoiceService(dal = dal)

    @Test
    fun `will throw if customer is not found`() {
        assertThrows<InvoiceNotFoundException> {
            invoiceService.fetch(404)
        }
    }

    @Test
    fun `can mark an invoice as paid`() {
        val testInvoice = Invoice(1, 1, Money(BigDecimal("20000"), Currency.DKK), InvoiceStatus.PENDING)
        invoiceService.markAsPaid(testInvoice)
        verify { dal.updateInvoice(Invoice(1, 1, Money(BigDecimal("20000"), Currency.DKK), InvoiceStatus.PAID)) }
    }
}