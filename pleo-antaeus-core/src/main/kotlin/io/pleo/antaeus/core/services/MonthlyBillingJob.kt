package io.pleo.antaeus.core.services

import mu.KotlinLogging
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

class MonthlyBillingJob : Job {
    override fun execute(context: JobExecutionContext?) {
        logger.info { "Started scheduled job on " + LocalDateTime.now() }
        val billingService: BillingService = context?.scheduler?.context?.get("billingService") as BillingService
        do {
            val chargedInvoices = billingService.chargePendingInvoices()
        } while (chargedInvoices != 0)

    }
}