package com.payment.infra.persistence

import com.payment.domain.PaymentStatus
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase
import jakarta.enterprise.context.ApplicationScoped

@ApplicationScoped
class PaymentStatusRepository: PanacheRepositoryBase<PaymentStatus, Int>