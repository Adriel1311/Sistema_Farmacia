package com.medicalstore.pos.exception;

public class BusinessRuleException extends RuntimeException {
    private final String ruleCode;

    public BusinessRuleException(String ruleCode, String message) {
        super(message);
        this.ruleCode = ruleCode;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public static BusinessRuleException of(String ruleCode, String message) {
        return new BusinessRuleException(ruleCode, message);
    }
}