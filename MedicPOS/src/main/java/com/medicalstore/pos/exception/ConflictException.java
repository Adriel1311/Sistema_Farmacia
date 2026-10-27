package com.medicalstore.pos.exception;

public class ConflictException extends BusinessRuleException {
    public ConflictException(String message) {
        super("CONFLICT", message);
    }

    public ConflictException(String resource, String field, Object value) {
        super("CONFLICT", resource + " already exists with " + field + ": " + value);
    }
}