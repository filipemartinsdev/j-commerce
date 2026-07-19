package com.payment.application.service

import com.payment.application.message.GeneratePaymentMessage
import com.payment.application.message.PaymentGeneratedMessage
import com.payment.application.message.PaymentRefundedMessage
import com.payment.application.message.PaymentTimeoutMessage
import com.payment.application.message.RefundPaymentMessage
import com.payment.domain.Payment
import com.payment.domain.PaymentStatus
import com.payment.infra.messaging.MessageBrokerProducer
import com.payment.infra.persistence.PaymentRepository
import jakarta.persistence.EntityManager
import jakarta.ws.rs.NotFoundException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import java.math.BigDecimal
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class PaymentServiceTests {

    @Mock
    lateinit var messageBrokerProducer: MessageBrokerProducer

    @Mock
    lateinit var paymentRepository: PaymentRepository

    @Mock
    lateinit var entityManager: EntityManager

    @InjectMocks
    lateinit var paymentService: PaymentService

    @Test
    @DisplayName("Should persist payment and emit event")
    fun generatePaymentTestCase1() {
        val message = GeneratePaymentMessage(
            salesOrderId = UUID.randomUUID(),
            userId = UUID.randomUUID(),
            deliveryAddressId = UUID.randomUUID(),
            totalAmount = BigDecimal("149.90")
        )

        val pendingStatus = PaymentStatus()
        pendingStatus.id = PaymentStatus.Companion.Value.PENDING.id
        pendingStatus.name = PaymentStatus.Companion.Value.PENDING.name

        val paidStatus = PaymentStatus()
        paidStatus.id = PaymentStatus.Companion.Value.PAID.id
        paidStatus.name = PaymentStatus.Companion.Value.PAID.name

        val payment = Payment()
        payment.id = UUID.randomUUID()
        payment.salesOrderId = message.salesOrderId
        payment.userId = message.userId
        payment.amount = message.totalAmount
        payment.status = pendingStatus

        Mockito.`when`(
            entityManager.getReference(PaymentStatus::class.java, PaymentStatus.Companion.Value.PENDING.id)
        ).thenReturn(pendingStatus)

        Mockito.`when`(
            entityManager.getReference(PaymentStatus::class.java, PaymentStatus.Companion.Value.PAID.id)
        ).thenReturn(paidStatus)

        Mockito.doAnswer { invocation ->
            val paymentArg = invocation.getArgument<Payment>(0)
            paymentArg.id = payment.id
            null
        }.`when`(paymentRepository).persist(any<Payment>())

        Mockito.`when`(paymentRepository.findById(payment.id))
            .thenReturn(payment)

        paymentService.generatePayment(message)

        Mockito.verify(paymentRepository, Mockito.times(2))
            .persist(any<Payment>())

        Mockito.verify(messageBrokerProducer)
            .producePaymentGenerated(any<PaymentGeneratedMessage>())
    }

    @Test
    @DisplayName("Should emit timeout event when payment is still pending")
    fun handlePaymentTimeoutTestCase1() {
        val pendingStatus = PaymentStatus()
        pendingStatus.id = PaymentStatus.Companion.Value.PENDING.id
        pendingStatus.name = PaymentStatus.Companion.Value.PENDING.name

        val payment = Payment()
        payment.id = UUID.randomUUID()
        payment.salesOrderId = UUID.randomUUID()
        payment.userId = UUID.randomUUID()
        payment.amount = BigDecimal.ONE
        payment.status = pendingStatus

        Mockito.`when`(paymentRepository.findById(payment.id))
            .thenReturn(payment)

        paymentService.handlePaymentTimeout(
            PaymentGeneratedMessage(payment.id, payment.salesOrderId, payment.userId, payment.amount)
        )

        Mockito.verify(paymentRepository).findById(payment.id)
        Mockito.verify(messageBrokerProducer).producePaymentTimeout(any())
    }

    @Test
    @DisplayName("Should skip timeout event when payment is already paid")
    fun handlePaymentTimeoutTestCase2() {
        val paidStatus = PaymentStatus()
        paidStatus.id = PaymentStatus.Companion.Value.PAID.id
        paidStatus.name = PaymentStatus.Companion.Value.PAID.name

        val payment = Payment()
        payment.id = UUID.randomUUID()
        payment.salesOrderId = UUID.randomUUID()
        payment.userId = UUID.randomUUID()
        payment.amount = BigDecimal.ONE
        payment.status = paidStatus

        Mockito.`when`(paymentRepository.findById(payment.id))
            .thenReturn(payment)

        paymentService.handlePaymentTimeout(
            PaymentGeneratedMessage(payment.id, payment.salesOrderId, payment.userId, payment.amount)
        )

        Mockito.verify(paymentRepository).findById(payment.id)
        Mockito.verify(messageBrokerProducer, never()).producePaymentTimeout(any())
    }

    @Test
    @DisplayName("Should throw NotFoundException when payment does not exist on timeout handling")
    fun handlePaymentTimeout_missingPayment() {
        val paymentId = UUID.randomUUID()

        Mockito.`when`(paymentRepository.findById(paymentId))
            .thenReturn(null)

        assertThrows(NotFoundException::class.java) {
            paymentService.handlePaymentTimeout(
                PaymentGeneratedMessage(paymentId, UUID.randomUUID(), UUID.randomUUID(), BigDecimal.ONE)
            )
        }

        Mockito.verify(paymentRepository).findById(paymentId)
    }

    @Test
    @DisplayName("Should refund all paid payments and emit refund events")
    fun refundPaymentTestCase1() {
        val salesOrderId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val amount = BigDecimal("210.00")
        val message = RefundPaymentMessage(salesOrderId, userId, emptyList(), amount)

        val paidStatus = PaymentStatus()
        paidStatus.id = PaymentStatus.Companion.Value.PAID.id
        paidStatus.name = PaymentStatus.Companion.Value.PAID.name

        val pendingStatus = PaymentStatus()
        pendingStatus.id = PaymentStatus.Companion.Value.PENDING.id
        pendingStatus.name = PaymentStatus.Companion.Value.PENDING.name

        val refundedStatus = PaymentStatus()
        refundedStatus.id = PaymentStatus.Companion.Value.REFUNDED.id
        refundedStatus.name = PaymentStatus.Companion.Value.REFUNDED.name

        val paidPayment = Payment()
        paidPayment.id = UUID.randomUUID()
        paidPayment.status = paidStatus
        paidPayment.salesOrderId = salesOrderId
        paidPayment.userId = userId
        paidPayment.amount = amount

        val pendingPayment = Payment()
        pendingPayment.id = UUID.randomUUID()
        pendingPayment.status = pendingStatus
        pendingPayment.salesOrderId = salesOrderId
        pendingPayment.userId = userId
        pendingPayment.amount = amount

        val refundedPayment = Payment()
        refundedPayment.id = UUID.randomUUID()
        refundedPayment.status = refundedStatus
        refundedPayment.salesOrderId = salesOrderId
        refundedPayment.userId = userId
        refundedPayment.amount = amount

        Mockito.`when`(paymentRepository.findAllBySalesOrderId(salesOrderId))
            .thenReturn(listOf(paidPayment, pendingPayment, refundedPayment))

        Mockito.`when`(
            entityManager.getReference(PaymentStatus::class.java, PaymentStatus.Companion.Value.REFUNDED.id)
        ).thenReturn(refundedStatus)

        paymentService.refundPayment(message)

        assertEquals(pendingStatus.id, pendingPayment.status.id)
        assertEquals(refundedStatus.id, refundedPayment.status.id)
        assertEquals(refundedStatus.id, paidPayment.status.id)

        Mockito.verify(paymentRepository).persist(paidPayment)
        Mockito.verify(messageBrokerProducer, Mockito.times(1))
            .producePaymentRefunded(any())
    }

    @Test
    @DisplayName("Should throw NotFoundException when there is no payment for refund")
    fun refundPaymentTestCase2() {
        val salesOrderId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val amount = BigDecimal("210.00")

        val message = RefundPaymentMessage(salesOrderId, userId, emptyList(), amount)

        Mockito.`when`(paymentRepository.findAllBySalesOrderId(salesOrderId))
            .thenReturn(emptyList())

        assertThrows(NotFoundException::class.java) {
            paymentService.refundPayment(message)
        }

        Mockito.verify(messageBrokerProducer, Mockito.never())
            .producePaymentRefunded(any())
    }

    @Test
    @DisplayName("Should throw NotFoundException when no paid payments are available for refund")
    fun refundPaymentTestCase3() {
        val salesOrderId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val amount = BigDecimal("210.00")

        val pendingStatus = PaymentStatus()
        pendingStatus.id = PaymentStatus.Companion.Value.PENDING.id
        pendingStatus.name = PaymentStatus.Companion.Value.PENDING.name

        val pendingPayment = Payment()
        pendingPayment.status = pendingStatus
        pendingPayment.salesOrderId = salesOrderId
        pendingPayment.userId = userId
        pendingPayment.amount = amount

        val message = RefundPaymentMessage(salesOrderId, userId, emptyList(), amount)

        Mockito.`when`(paymentRepository.findAllBySalesOrderId(salesOrderId))
            .thenReturn(listOf(pendingPayment))

        assertThrows(NotFoundException::class.java) {
            paymentService.refundPayment(message)
        }

        Mockito.verify(messageBrokerProducer, Mockito.never())
            .producePaymentRefunded(any<PaymentRefundedMessage>())
    }
}
