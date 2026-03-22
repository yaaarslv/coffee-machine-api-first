package org.isrpo.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Model of error response
 */
@Getter
@Setter
public class CustomErrorResponse {
    private String name;
    private int status;
    private String message;
    private LocalDateTime timestamp;

    public CustomErrorResponse(String name, int status, String message) {
        this.name = name;
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
