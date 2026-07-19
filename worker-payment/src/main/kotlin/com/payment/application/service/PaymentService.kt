package com.payment.application.service

import com.payment.application.message.PaymentConfirmedMessage
import com.payment.application.message.PaymentGeneratedMessage
import com.payment.application.message.PaymentRefundedMessage
import com.payment.application.message.PaymentTimeoutMessage
import com.payment.application.message.RefundPaymentMessage
import com.payment.application.message.GeneratePaymentMessage
import com.payment.domain.Payment
import com.payment.domain.PaymentStatus
import com.payment.infra.messaging.MessageBrokerProducer
import com.payment.infra.persistence.PaymentRepository
import jakarta.enterprise.context.ApplicationScoped
import jakarta.persistence.EntityManager
import jakarta.transaction.Transactional
import jakarta.ws.rs.BadRequestException
import jakarta.ws.rs.NotFoundException
import org.jboss.logging.Logger
import java.util.UUID

@ApplicationScoped
class PaymentService(
    private val messageBrokerProducer: MessageBrokerProducer,
    private val paymentRepository: PaymentRepository,
    private val entityManager: EntityManager,
) {
    val log: Logger = Logger.getLogger(PaymentService::class.java)

    @Transactional
    fun generatePayment(message: GeneratePaymentMessage) {
        val payment = Payment()
        payment.salesOrderId = message.salesOrderId
        payment.userId = message.userId
        payment.amount = message.totalAmount
        payment.status = entityManager.getReference(
            PaymentStatus::class.java,
            PaymentStatus.Companion.Value.PENDING.id
        )

        paymentRepository.persist(payment)

        messageBrokerProducer.producePaymentGenerated(
            PaymentGeneratedMessage(
                paymentId = payment.id,
                orderId = message.salesOrderId,
                userId = message.userId,
                amount = message.totalAmount
            )
        )

        // That's a mock, then, instantly confirmed
        confirmPaymentById(payment.id)
    }

    private fun confirmPaymentById(id: UUID){
        val payment: Payment = paymentRepository.findById(id)
            ?: throw NotFoundException("Payment not found by ID: $id")

        if (payment.status.id != PaymentStatus.Companion.Value.PENDING.id)
            throw BadRequestException("Payment not in pending status: ${payment.status.name}")

        payment.status = entityManager.getReference(
            PaymentStatus::class.java,
            PaymentStatus.Companion.Value.PAID.id
        )

        paymentRepository.persist(payment)

        messageBrokerProducer.producePaymentConfirmed(
            PaymentConfirmedMessage(
                paymentId = payment.id,
                orderId = payment.salesOrderId,
                userId = payment.userId,
                amount = payment.amount
            )
        )
    }


    fun handlePaymentTimeout(message: PaymentGeneratedMessage) {
        val payment = paymentRepository.findById(message.paymentId)
            ?: throw NotFoundException("Payment not found by ID: ${message.paymentId}")

        if (payment.status.id == PaymentStatus.Companion.Value.PAID.id)
            return

        messageBrokerProducer.producePaymentTimeout(
            PaymentTimeoutMessage(
                paymentId = message.paymentId,
                orderId = message.orderId,
                userId = message.userId,
                amount = message.amount
            )
        )
    }

    @Transactional
    fun refundPayment(message: RefundPaymentMessage){
        val payments: List<Payment> = paymentRepository.findAllBySalesOrderId(message.salesOrderId)

        if (payments.isEmpty())
            throw NotFoundException("Payment not found by salesOrderId: ${message.salesOrderId}")

        var refundCount = 0

        for (payment in payments) {
            if (payment.status.id != PaymentStatus.Companion.Value.PAID.id)
                continue
            refund(payment)
            refundCount++
        }

        if (refundCount == 0)
            throw NotFoundException("No payments found for refund")
    }

    private fun refund(payment: Payment) {
        payment.status = entityManager.getReference(
            PaymentStatus::class.java,
            PaymentStatus.Companion.Value.REFUNDED.id
        )

        paymentRepository.persist(payment)

        log.info("Payment refunded: " + payment.id)

        messageBrokerProducer.producePaymentRefunded(
            PaymentRefundedMessage(
                paymentId = payment.id,
                orderId = payment.salesOrderId,
                userId = payment.userId,
                amount = payment.amount
            )
        )
    }
}