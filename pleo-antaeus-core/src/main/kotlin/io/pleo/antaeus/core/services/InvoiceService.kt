/*
    Implements endpoints related to invoices.
 */

package io.pleo.antaeus.core.services

import io.pleo.antaeus.core.exceptions.InvoiceNotFoundException
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.models.Invoice
import io.pleo.antaeus.models.InvoiceStatus

class InvoiceService(private val dal: AntaeusDal) {
    fun fetchAll(): List<Invoice> {
        return dal.fetchInvoices()
    }

    fun fetch(id: Int): Invoice {
        return dal.fetchInvoice(id) ?: throw InvoiceNotFoundException(id)
    }

    fun getPendingInvoices(limit: Int): List<Invoice> {
        return dal.getPendingInvoices(limit)
    }

    fun markAsPaid(invoice: Invoice) {
        val paid: Invoice = invoice.copy(invoice.id, invoice.customerId, invoice.amount, InvoiceStatus.PAID)
        dal.updateInvoice(paid)
    }
}

