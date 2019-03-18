/*
    Defines the main() entry point of the app.
    Configures the database and sets up the REST web service.
 */

@file:JvmName("AntaeusApp")

package io.pleo.antaeus.app

import getPaymentProvider
import io.pleo.antaeus.core.services.BillingService
import io.pleo.antaeus.core.services.CustomerService
import io.pleo.antaeus.core.services.InvoiceService
import io.pleo.antaeus.data.AntaeusDal
import io.pleo.antaeus.data.CustomerTable
import io.pleo.antaeus.data.InvoiceTable
import io.pleo.antaeus.rest.AntaeusRest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.quartz.CronScheduleBuilder.cronSchedule
import org.quartz.JobBuilder.newJob
import setupInitialData
import java.sql.Connection
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.time.temporal.ChronoUnit
import java.time.ZonedDateTime
import org.quartz.SchedulerException
import org.quartz.impl.StdSchedulerFactory
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.JobDetail
import org.quartz.TriggerBuilder.newTrigger


fun main() {
    // The tables to create in the database.
    val tables = arrayOf(InvoiceTable, CustomerTable)

    // Connect to the database and create the needed tables. Drop any existing data.
    val db = Database
            .connect("jdbc:sqlite:/tmp/data.db", "org.sqlite.JDBC")
            .also {
                TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
                transaction(it) {
                    addLogger(StdOutSqlLogger)
                    // Drop all existing tables to ensure a clean slate on each run
                    SchemaUtils.drop(*tables)
                    // Create all tables
                    SchemaUtils.create(*tables)
                }
            }

    // Set up data access layer.
    val dal = AntaeusDal(db = db)

    // Insert example data in the database.
    setupInitialData(dal = dal)

    // Get third parties
    val paymentProvider = getPaymentProvider()

    // Create core services
    val invoiceService = InvoiceService(dal = dal)
    val customerService = CustomerService(dal = dal)

    val billingService = BillingService(paymentProvider, invoiceService)
    val scheduler = StdSchedulerFactory.getDefaultScheduler()
    try {
        scheduler.start()
        val job = newJob(billingService.javaClass)
                .withIdentity("Billing", "Monthly services")
                .build()
        val trigger = newTrigger()
                .withIdentity("payPendingInvoices", "Monthly trigger")
                .withSchedule(cronSchedule("0 0 10am 1 * ?"))
                .build()
        scheduler.scheduleJob(job, trigger)

    } catch (se: SchedulerException) {
        se.printStackTrace()
    } finally {
        //scheduler.shutdown() needs to be done somewhere on app shutdown
    }


    // Create REST web service
    AntaeusRest(
            invoiceService = invoiceService,
            customerService = customerService
    ).run()
}

