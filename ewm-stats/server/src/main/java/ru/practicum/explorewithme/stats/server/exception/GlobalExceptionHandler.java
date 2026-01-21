package ru.practicum.explorewithme.stats.server.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.practicum.explorewithme.stats.server.dto.ApiError;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
@Slf4j
@Order(1)
public class GlobalExceptionHandler {

    // Обработка IllegalArgumentException (некорректные аргументы)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Некорректный аргумент: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Incorrectly made request.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(List.of())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    // Обработка MissingServletRequestParameterException (отсутствующие параметры запроса)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingRequestParam(MissingServletRequestParameterException e) {
        log.warn("Отсутствует обязательный параметр запроса: {}", e.getParameterName());
        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Required request parameter is missing.")
                .message("Parameter '" + e.getParameterName() + "' is required")
                .timestamp(LocalDateTime.now())
                .errors(List.of())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    // Общая обработка всех исключений
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception e) {
        log.error("Внутренняя ошибка сервера: {}", e.getMessage(), e);
        ApiError error = ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .reason("Internal server error.")
                .message(e.getMessage() != null ? e.getMessage() : "Unknown error")
                .timestamp(LocalDateTime.now())
                .errors(List.of())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}