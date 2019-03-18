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
    override fun execute(context: JobExecutionContext) {
        val pendingInvoices: List<Invoice> = invoiceService.getPendingInvoices()
        for (invoice in pendingInvoices) {
            if (paymentProvider.charge(invoice)) {
                val paid: Invoice = invoice.copy(invoice.id, invoice.customerId, invoice.amount, InvoiceStatus.PAID)
                invoiceService.updateInvoice(paid)
            }
        }
    }
}
