package com.medicalstore.pos.exception;

public class StockInsufficientException extends BusinessRuleException {
    private final int requested;
    private final int available;

    public StockInsufficientException(String medicamento, int requested, int available) {
        super("STOCK_INSUFICIENTE", 
            String.format("Stock insuficiente de %s: se solicitan %d y solo hay %d vigentes", 
                medicamento, requested, available));
        this.requested = requested;
        this.available = available;
    }

    public int getRequested() { return requested; }
    public int getAvailable() { return available; }
}