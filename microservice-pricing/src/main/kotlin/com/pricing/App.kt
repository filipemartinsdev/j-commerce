package com.pricing

import com.pricing.application.service.PricingEngine
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.inject.Produces

@ApplicationScoped
class App (
    private val pricingEngine: PricingEngine
) {

    @Scheduled(cron = "1/5 * * * * ?")
    fun refreshPrices(){
        pricingEngine.refreshPrices()
    }
}