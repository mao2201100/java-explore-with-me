package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(final ValidationException exception) {
        log.debug("Запрос составлен некорректно.");
        return createApiError(HttpStatus.BAD_REQUEST, "Запрос составлен некорректно.", exception);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(final NotFoundException exception) {
        log.debug("Объект не найден.");
        return createApiError(HttpStatus.NOT_FOUND, "Объект не найден.", exception);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(final ConflictException exception) {
        log.debug("Нарушение целостности данных.");
        return createApiError(HttpStatus.CONFLICT, "Нарушение целостности данных.", exception);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleThrowable(final Throwable exception) {
        log.debug("Ошибка сервера");
        return createApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка сервера", exception);
    }

    private ApiError createApiError(HttpStatus status, String reason, Throwable exception) {
        return ApiError.builder()
                .status(status)
                .reason(reason)
                .message(exception.getLocalizedMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

}