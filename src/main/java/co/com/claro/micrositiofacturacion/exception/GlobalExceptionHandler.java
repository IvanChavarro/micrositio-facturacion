package co.com.claro.micrositiofacturacion.exception;

import co.com.claro.micrositiofacturacion.dto.ErrorResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AcifResultNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleAcifResultNotFound(
            AcifResultNotFoundException exception,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return buildResponse(exception.getMessage(), request, status);
    }

    @ExceptionHandler(AcifConversionException.class)
    public ResponseEntity<ErrorResponseDTO> handleAcifConversion(
            AcifConversionException exception,
            HttpServletRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return buildResponse(exception.getMessage(), request, status);
    }

    private ResponseEntity<ErrorResponseDTO> buildResponse(
            String message,
            HttpServletRequest request,
            HttpStatus status) {
        ErrorResponseDTO response = new ErrorResponseDTO(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());

        return ResponseEntity.status(status).body(response);
    }
}
