package ru.kubsu.clientservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.kubsu.contracts.dto.common.ErrorResponseTo;
import ru.kubsu.contracts.exception.service.client.BatchRequestNotFoundException;
import ru.kubsu.contracts.exception.service.client.ClientAlreadyExistsException;
import ru.kubsu.contracts.exception.service.client.ClientNotFoundException;
import ru.kubsu.contracts.exception.service.client.ClientServiceException;
import ru.kubsu.contracts.exception.service.client.ClientValidationException;
import ru.kubsu.contracts.exception.service.client.ExternalRequestNotFoundException;
import ru.kubsu.contracts.exception.service.client.RequestNotFoundException;

/**
 * Глобальный обработчик исключений client-service.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключение «клиент не найден».
     *
     * @param exception исключение
     * @return HTTP 404 с телом ошибки
     */
    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ErrorResponseTo> handleClientNotFound(ClientNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception);
    }

    /**
     * Обрабатывает исключение «внешний запрос не найден».
     *
     * @param exception исключение
     * @return HTTP 404 с телом ошибки
     */
    @ExceptionHandler(ExternalRequestNotFoundException.class)
    public ResponseEntity<ErrorResponseTo> handleExternalRequestNotFound(ExternalRequestNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception);
    }

    /**
     * Обрабатывает исключение «пакетный запрос не найден».
     *
     * @param exception исключение
     * @return HTTP 404 с телом ошибки
     */
    @ExceptionHandler(BatchRequestNotFoundException.class)
    public ResponseEntity<ErrorResponseTo> handleBatchRequestNotFound(BatchRequestNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception);
    }

    /**
     * Обрабатывает исключение «запрос не найден».
     *
     * @param exception исключение
     * @return HTTP 404 с телом ошибки
     */
    @ExceptionHandler(RequestNotFoundException.class)
    public ResponseEntity<ErrorResponseTo> handleRequestNotFound(RequestNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception);
    }

    /**
     * Обрабатывает исключение «клиент уже существует».
     *
     * @param exception исключение
     * @return HTTP 409 с телом ошибки
     */
    @ExceptionHandler(ClientAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseTo> handleClientAlreadyExists(ClientAlreadyExistsException exception) {
        return buildResponse(HttpStatus.CONFLICT, exception);
    }

    /**
     * Обрабатывает ошибки бизнес-валидации клиента.
     *
     * @param exception исключение
     * @return HTTP 400 с телом ошибки
     */
    @ExceptionHandler(ClientValidationException.class)
    public ResponseEntity<ErrorResponseTo> handleClientValidation(ClientValidationException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, exception);
    }

    /**
     * Обрабатывает ошибки валидации входных DTO.
     *
     * @param exception исключение валидации
     * @return HTTP 400 с телом ошибки
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseTo> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Ошибка валидации входных данных");
        ErrorResponseTo body = new ErrorResponseTo()
                .setCode(ClientValidationException.ERROR_CODE)
                .setMessage(message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /**
     * Формирует HTTP-ответ с кодом и телом ошибки.
     *
     * @param status    HTTP-статус
     * @param exception доменное исключение
     * @return ответ с ErrorResponseTo
     */
    private ResponseEntity<ErrorResponseTo> buildResponse(HttpStatus status, ClientServiceException exception) {
        ErrorResponseTo body = new ErrorResponseTo()
                .setCode(exception.getCode())
                .setMessage(exception.getMessage());
        return ResponseEntity.status(status).body(body);
    }
}
