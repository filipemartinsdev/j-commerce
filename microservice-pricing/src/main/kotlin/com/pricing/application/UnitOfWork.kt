package com.pricing.application

interface UnitOfWork {
    fun <T> execute(block: () -> T): T;
}

