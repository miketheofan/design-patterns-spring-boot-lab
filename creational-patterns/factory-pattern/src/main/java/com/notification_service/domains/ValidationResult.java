package com.notification_service.domains;

import lombok.Builder;

import java.util.List;

/**
 * Result of a notification request validation.
 */
@Builder
public record ValidationResult(boolean valid, List<String> errors) {

    /** Creates a successful validation result. */
    public static ValidationResult success() {
        return builder().valid(true).errors(List.of()).build();
    }

    /** Creates a failed validation result with the given errors. */
    public static ValidationResult failure(List<String> errors) {
        return builder().valid(false).errors(errors).build();
    }
}
