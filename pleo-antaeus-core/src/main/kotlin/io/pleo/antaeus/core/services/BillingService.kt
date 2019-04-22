package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class BillingService(
        private val paymentProvider: PaymentProvider,
        private val invoiceService: InvoiceService
) {

    fun chargePendingInvoices(): Int {
        val pendingInvoices: List<Invoice> = invoiceService.getPendingInvoices(100)
        logger.info { "Found ${pendingInvoices.size} invoices pending" }
        var chargedInvoices = 0
        for (invoice in pendingInvoices) {
            if (processInvoice(invoice)) {
                chargedInvoices++
            }
        }
        logger.info { "Charged " + chargedInvoices + " invoices out of " + pendingInvoices.size }
        return pendingInvoices.size;
    }

    fun processInvoice(id: Int): Boolean {
        return processInvoice(invoiceService.fetch(id))
    }

    private fun processInvoice(invoice: Invoice): Boolean {
        val charged = paymentProvider.charge(invoice)
        if (charged) {
            invoiceService.markAsPaid(invoice)
        }
        return charged;
    }
}
