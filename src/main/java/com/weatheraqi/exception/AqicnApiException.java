package com.weatheraqi.exception;

public class AqicnApiException extends RuntimeException {
    public AqicnApiException(String message) {
        super(message);
    }
    
    public AqicnApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
