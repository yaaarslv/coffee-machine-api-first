package org.isrpo.tools;

import org.isrpo.models.CustomErrorResponse;
import org.isrpo.tools.exceptions.CoffeeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Exception handler of CoffeeException
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CoffeeException.class)
    public ResponseEntity<CustomErrorResponse> handleRuntimeException(Exception ex) {
        CustomErrorResponse errorResponse = new CustomErrorResponse(
                ex.getClass().getSimpleName(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                ex.getMessage()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
