package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import org.quartz.Job
import org.quartz.JobExecutionContext

class BillingService(
        private val paymentProvider: PaymentProvider,
        private val invoiceService: InvoiceService
) : Job {
    companion object {
        const val INVOICE_LIMIT = 100
    }

    override fun execute(context: JobExecutionContext) {
        val pendingInvoices: List<Invoice> = invoiceService.getPendingInvoices(INVOICE_LIMIT)
        for (invoice in pendingInvoices) {
            processInvoice(invoice)
        }
    }

    private fun processInvoice(invoice: Invoice) {
        if (paymentProvider.charge(invoice)) {
            invoiceService.markAsPaid(invoice)
        }
    }

    fun processInvoice(id: Int) {
        processInvoice(invoiceService.fetch(id))
    }
}
