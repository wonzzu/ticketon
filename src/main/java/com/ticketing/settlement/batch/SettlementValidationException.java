package com.ticketing.settlement.batch;

public class SettlementValidationException extends RuntimeException {
    public SettlementValidationException(String message) {
        super(message);
    }
}
