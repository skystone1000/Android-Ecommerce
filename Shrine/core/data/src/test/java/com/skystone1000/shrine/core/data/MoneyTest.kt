package com.skystone1000.shrine.core.data

import com.skystone1000.shrine.core.model.Money
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Money formatting is pure JVM logic (no Android), so it runs as a plain JUnit test. Cases avoid
 * thousands-grouping so the assertions are locale-independent.
 */
class MoneyTest {

    @Test
    fun wholeDollarAmountsDropTheDecimals() {
        assertEquals("$0", Money.format(0))
        assertEquals("$5", Money.format(500))
        assertEquals("$99", Money.format(9900))
    }

    @Test
    fun fractionalAmountsKeepTwoDecimals() {
        assertEquals("$12.99", Money.format(1299))
        assertEquals("$0.05", Money.format(5))
        assertEquals("$99.99", Money.format(9999))
    }
}
