package com.pricing.application

/**
 * Abstraction to represent an atomic operation, hiding details about ACID from Framework.
 * An execution is atomic because if any error occurs, the entire operation is roll backed.
 */
interface UnitOfWork {
    fun <T> execute(block: () -> T): T;
}

