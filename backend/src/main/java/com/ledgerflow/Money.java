package com.ledgerflow;

import java.math.BigDecimal;

final class Money {
    private Money() {}
    static long minor(BigDecimal value) {
        if (value == null || value.signum() <= 0 || value.compareTo(new BigDecimal("1000000.00")) > 0)
            throw new DomainException(400, "Amount must be between 0.01 and 1000000.00 USD.");
        try { return value.movePointRight(2).longValueExact(); }
        catch (ArithmeticException e) { throw new DomainException(400, "Amounts must have at most two decimal places."); }
    }
    static String decimal(long minor) { return BigDecimal.valueOf(minor, 2).toPlainString(); }
}
