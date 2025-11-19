package com.weatheraqi.exception;

import com.weatheraqi.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(WeatherApiException.class)
    public ResponseEntity<ErrorResponse> handleWeatherApiException(
            WeatherApiException ex, HttpServletRequest request) {
        log.error("Weather API Exception: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Weather API Error",
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
    
    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpClientError(
            HttpClientErrorException ex, HttpServletRequest request) {
        log.error("HTTP Client Error: {}", ex.getMessage());
        
        String message = "Invalid request to weather service";
        if (ex.getStatusCode().value() == 404) {
            message = "Location not found";
        } else if (ex.getStatusCode().value() == 401) {
            message = "Invalid API key";
        }
        
        ErrorResponse error = new ErrorResponse(
                ex.getStatusCode().value(),
                "Client Error",
                message,
                request.getRequestURI()
        );
        
        return ResponseEntity.status(ex.getStatusCode()).body(error);
    }
    
    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorResponse> handleHttpServerError(
            HttpServerErrorException ex, HttpServletRequest request) {
        log.error("HTTP Server Error: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                "Weather service is temporarily unavailable",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }
    
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleResourceAccessException(
            ResourceAccessException ex, HttpServletRequest request) {
        log.error("Resource Access Exception: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.GATEWAY_TIMEOUT.value(),
                "Timeout",
                "Request to weather service timed out",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).body(error);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
