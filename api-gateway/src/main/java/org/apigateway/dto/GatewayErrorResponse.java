package org.apigateway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;

/**
 * Uniform error response body returned by the gateway for 4xx/5xx conditions.
 * Matches the {@code ApiResponse} shape used by downstream microservices.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GatewayErrorResponse {

    private boolean success = false;
    private String  error;
    private String  path;
    private int     status;

    @Builder.Default
    private Instant timestamp = Instant.now();

    // ── convenience factories ─────────────────────────────────────────────────

    public static GatewayErrorResponse of(int status, String error, String path) {
        return GatewayErrorResponse.builder()
                .status(status)
                .error(error)
                .path(path)
                .build();
    }
}