package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.external.PaymentProvider
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus
import mu.KotlinLogging
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

class BillingService(
        private val paymentProvider: PaymentProvider,
        private val invoiceService: InvoiceService
) : Job {
    companion object {
        const val INVOICE_LIMIT = 100
    }

    override fun execute(context: JobExecutionContext) {
        logger.info { "Started scheduled job on " + LocalDateTime.now() }
        val pendingInvoices: List<Invoice> = invoiceService.getPendingInvoices(INVOICE_LIMIT)
        logger.debug { "Found ${pendingInvoices.size} invoices pending" + LocalDateTime.now() }
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
