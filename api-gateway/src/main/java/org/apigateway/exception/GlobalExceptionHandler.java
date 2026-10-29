package org.apigateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apigateway.dto.GatewayErrorResponse;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Gateway-wide exception handler that converts any unhandled {@link Throwable}
 * into the {@link GatewayErrorResponse} JSON shape.
 *
 * <p>Order(-2) ensures it runs before Spring Boot's default
 * {@code DefaultErrorWebExceptionHandler} (order = -1).
 */
@Slf4j
@Order(-2)
@Component
@RequiredArgsConstructor
public class GlobalExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        String path = exchange.getRequest().getPath().value();

        HttpStatus status;
        String message;

        if (ex instanceof NotFoundException) {
            status  = HttpStatus.SERVICE_UNAVAILABLE;
            message = "Downstream service is currently unavailable";
            log.warn("Service unavailable for path {}: {}", path, ex.getMessage());
        } else if (ex instanceof ResponseStatusException rse) {
            status  = HttpStatus.resolve(rse.getStatusCode().value());
            if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = rse.getReason() != null ? rse.getReason() : ex.getMessage();
            log.debug("ResponseStatusException on {}: {}", path, ex.getMessage());
        } else if (ex instanceof java.net.ConnectException || ex instanceof io.netty.channel.ConnectTimeoutException) {
            status  = HttpStatus.BAD_GATEWAY;
            message = "Unable to reach upstream service. Please try again shortly.";
            log.error("Connection error for path {}: {}", path, ex.getMessage());
        } else {
            status  = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "An unexpected gateway error occurred";
            log.error("Unexpected gateway error for path {}", path, ex);
        }

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        GatewayErrorResponse body = GatewayErrorResponse.of(status.value(), message, path);
        byte[] bytes = toJson(body);

        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    private byte[] toJson(Object obj) {
        try {
            return objectMapper.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            return "{\"success\":false,\"error\":\"Gateway error\"}".getBytes();
        }
    }
}