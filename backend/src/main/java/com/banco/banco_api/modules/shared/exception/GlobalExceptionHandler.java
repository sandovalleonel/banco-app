package com.banco.banco_api.modules.shared.exception;

import com.banco.banco_api.modules.shared.dto.GeneralResponseDto;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GeneralResponseDto<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        GeneralResponseDto<Void> response = GeneralResponseDto.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();
        log.error("ResourceNotFoundException handled: {}", response.getMessage());
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<GeneralResponseDto<Void>> handleBusinessRuleException(BusinessRuleException ex) {
        GeneralResponseDto<Void> response = GeneralResponseDto.<Void>builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .build();
        log.error("BusinessRuleException handled: {}", response.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GeneralResponseDto<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        GeneralResponseDto<Void> response = GeneralResponseDto.<Void>builder()
                .success(false)
                .message(validationErrors)
                .data(null)
                .build();
        log.error("ValidationException handled: {}", response.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GeneralResponseDto<Void>> handleGenericException(Exception ex) {
        GeneralResponseDto<Void> response = GeneralResponseDto.<Void>builder()
                .success(false)
                .message("Ha ocurrido un error interno en el servidor: " + ex.getMessage())
                .data(null)
                .build();
        log.error("Generic Exception handled: {}", response.getMessage(), ex);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
