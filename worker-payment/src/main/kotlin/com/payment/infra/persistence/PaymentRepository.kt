package com.payment.infra.persistence

import com.payment.domain.Payment
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped
import java.util.UUID

@ApplicationScoped
class PaymentRepository: PanacheRepositoryBase<Payment, UUID> {
    fun findAllBySalesOrderId (salesOrderId: UUID): List<Payment> {
        return find("salesOrderId", salesOrderId).list()
    }
}