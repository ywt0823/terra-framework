package com.terra.framework.nova.core.exception;

public class NovaException extends RuntimeException {
    
    public NovaException(String message) {
        super(message);
    }
    
    public NovaException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public NovaException(Throwable cause) {
        super(cause);
    }
} 