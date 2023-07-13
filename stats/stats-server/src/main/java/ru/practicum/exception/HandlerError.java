package ru.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class HandlerError {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorRest handleValidationException(final ValidationException exception) {
        log.debug("Ошибка валидации: не верный запрос.");
        return createApiError(HttpStatus.BAD_REQUEST, "Ошибка валидации: не верный запрос.", exception);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorRest handleNotFoundException(final NotFoundException exception) {
        log.debug("Ошибка валидации: объект не найден.");
        return createApiError(HttpStatus.NOT_FOUND, "Ошибка валидации: объект не найден.", exception);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorRest handleConflictException(final ConflictException exception) {
        log.debug("Ошибка валидации: неизвестный конфликт.");
        return createApiError(HttpStatus.CONFLICT, "Ошибка валидации: неизвестный конфликт.", exception);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorRest handleThrowable(final Throwable exception) {
        log.debug("Ошибка валидации: ошибка сервера");
        return createApiError(HttpStatus.INTERNAL_SERVER_ERROR, "Ошибка валидации: ошибка сервера", exception);
    }

    private ErrorRest createApiError(HttpStatus status, String reason, Throwable exception) {
        return ErrorRest.builder()
                .status(status)
                .reason(reason)
                .message(exception.getLocalizedMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

}