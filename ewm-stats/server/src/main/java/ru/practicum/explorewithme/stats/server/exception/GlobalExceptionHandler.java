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

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Некорректный запрос: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно составлен запрос.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .errors(List.of())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> handleMissingRequestParam(MissingServletRequestParameterException e) {
        log.warn("Отсутствует обязательный параметр: {}", e.getParameterName());
        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Отсутствует обязательный параметр запроса.")
                .message("Параметр '" + e.getParameterName() + "' обязателен")
                .timestamp(LocalDateTime.now())
                .errors(List.of())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception e) {
        log.error("Внутренняя ошибка сервера: {}", e.getMessage(), e);
        ApiError error = ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .reason("Внутренняя ошибка сервера.")
                .message(e.getMessage() != null ? e.getMessage() : "Неизвестная ошибка")
                .timestamp(LocalDateTime.now())
                .errors(List.of())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}