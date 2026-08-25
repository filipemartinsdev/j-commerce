package com.pricing.infra

import com.pricing.application.UnitOfWork
import jakarta.enterprise.context.ApplicationScoped
import jakarta.transaction.Transactional

@ApplicationScoped
class QuarkusUnitOfWork: UnitOfWork {

    @Transactional
    override fun <T> execute(block: () -> T): T {
        return block()
    }
}