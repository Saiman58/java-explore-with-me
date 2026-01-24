package ru.practicum.explorewithme.server.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import ru.practicum.explorewithme.server.dto.ApiError;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
@Order(1)
public class GlobalExceptionHandler {

    @PostConstruct
    public void init() {
        log.info("GlobalExceptionHandler инициализирован — готов к обработке исключений!");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Некорректный запрос: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно составлен запрос.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException e) {
        log.warn("Конфликт: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .status(HttpStatus.CONFLICT.name())
                .reason("Для запрошенной операции условия не выполнены.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> handleConflict(ConflictException e) {
        log.warn("Конфликт: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .status(HttpStatus.CONFLICT.name())
                .reason("Нарушение целостности данных")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleCustomNotFound(EntityNotFoundException e) {
        log.warn("Объект не найден: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .status(HttpStatus.NOT_FOUND.name())
                .reason("Требуемый объект не найден.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.info("Обработка валидации аргументов метода: {}", e.getClass().getName());
        String message = e.getBindingResult().getFieldErrors().stream()
                .map((FieldError error) -> String.format("Поле: %s. Ошибка: %s. Значение: %s",
                        error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                .collect(Collectors.joining(". "));
        log.warn("Ошибка валидации: {}", message);
        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно составлен запрос.")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBindException(BindException e) {
        log.info("Обработка BindException: {}", e.getClass().getName());
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> String.format("Поле: %s. Ошибка: %s. Значение: %s",
                        error.getField(), error.getDefaultMessage(), error.getRejectedValue()))
                .collect(Collectors.joining(". "));
        log.warn("Ошибка привязки данных: {}", message);
        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно составлен запрос.")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException e) {
        log.info("Обработка нарушения ограничений: {}", e.getClass().getName());
        String message = e.getConstraintViolations().stream()
                .map(violation -> String.format("Поле: %s. Ошибка: %s. Значение: %s",
                        violation.getPropertyPath(), violation.getMessage(), violation.getInvalidValue()))
                .collect(Collectors.joining(". "));
        log.warn("Нарушение ограничений: {}", message);
        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно составлен запрос.")
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(TypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(TypeMismatchException e) {
        log.warn("Несоответствие типа: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно составлен запрос.")
                .message("Ошибка преобразования значения: " + e.getMessage())
                .timestamp(LocalDateTime.now())
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
                .build();
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("Ошибка чтения JSON", e);

        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно составлен запрос.")
                .message("Неверный формат тела запроса")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ApiError> handleInvalidFormat(InvalidFormatException e) {
        log.warn("Неверный формат данных", e);

        ApiError error = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно составлен запрос.")
                .message("Неверный формат поля запроса")
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("Нарушение целостности данных: {}", e.getMessage());
        ApiError error = ApiError.builder()
                .status(HttpStatus.CONFLICT.name())
                .reason("Для запрошенной операции условия не выполнены.")
                .message("Нарушение уникальности данных: " + e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception e) {
        log.error("Внутренняя ошибка сервера: {}", e.getMessage(), e);
        ApiError error = ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .reason("Внутренняя ошибка сервера.")
                .message(e.getMessage() != null ? e.getMessage() : "Неизвестная ошибка")
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}