package ru.practicum.explorewithme.server.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 404 - Не найдено
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(NotFoundException e) {
        log.error("Объект не найден: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.NOT_FOUND.name())
                .reason("Требуемый объект не был найден.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    // 409 - Конфликт (бизнес-правила, уникальность)
    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflict(ConflictException e) {
        log.error("Конфликт данных: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.CONFLICT.name())
                .reason("Для запрошенной операции условия не выполнены.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    // 400 - Валидация DTO (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidation(MethodArgumentNotValidException e) {
        log.error("Ошибка валидации DTO: {}", e.getMessage());

        String fieldError = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(field -> String.format("Поле: %s. Ошибка: %s. Значение: %s",
                        field.getField(),
                        field.getDefaultMessage(),
                        field.getRejectedValue()))
                .orElse(e.getMessage());

        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно составленный запрос.")
                .message(fieldError)
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    // 400 - Валидация параметров (@RequestParam, @PathVariable)
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleConstraintViolation(ConstraintViolationException e) {
        log.error("Ошибка валидации параметров: {}", e.getMessage());

        String errorMessage = e.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> String.format("Поле: %s. Ошибка: %s. Значение: %s",
                        violation.getPropertyPath().toString(),
                        violation.getMessage(),
                        violation.getInvalidValue()))
                .orElse("Ошибка валидации");

        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно составленный запрос.")
                .message(errorMessage)
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    // 400 - Пользовательская валидация (ValidationException)
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(ValidationException e) {
        log.error("Ошибка бизнес-валидации: {}", e.getMessage());
        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Для запрошенной операции условия не выполнены.")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    // 📌 НОВЫЙ ОБРАБОТЧИК: 400 - Отсутствует обязательный параметр
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingParameter(MissingServletRequestParameterException e) {
        log.error("Отсутствует обязательный параметр: {}", e.getMessage());

        String errorMessage = String.format("Отсутствует обязательный параметр: '%s' типа %s",
                e.getParameterName(),
                e.getParameterType());

        return ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.name())
                .reason("Некорректно составленный запрос.")
                .message(errorMessage)
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }

    // 500 - Все остальные исключения
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleAllExceptions(Exception e) {
        log.error("Внутренняя ошибка сервера: {}", e.getMessage(), e);
        return ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.name())
                .reason("Внутренняя ошибка сервера.")
                .message("Произошла непредвиденная ошибка.")
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build();
    }
}