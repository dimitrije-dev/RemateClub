package com.remateclub.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> handleValidation(
    MethodArgumentNotValidException exception,
    HttpServletRequest request
  ) {
    List<FieldErrorResponse> fields = exception
      .getBindingResult()
      .getFieldErrors()
      .stream()
      .sorted(Comparator.comparing(FieldError::getField))
      .map(error -> new FieldErrorResponse(
        error.getField(),
        error.getDefaultMessage()
      ))
      .toList();

    return ResponseEntity
      .badRequest()
      .body(ErrorResponse.validation(
        "Validation failed",
        request.getRequestURI(),
        fields
      ));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  ResponseEntity<ErrorResponse> handleConstraintViolation(
    ConstraintViolationException exception,
    HttpServletRequest request
  ) {
    List<FieldErrorResponse> fields = exception
      .getConstraintViolations()
      .stream()
      .map(violation -> new FieldErrorResponse(
        violation.getPropertyPath().toString(),
        violation.getMessage()
      ))
      .toList();

    return ResponseEntity
      .badRequest()
      .body(ErrorResponse.validation(
        "Validation failed",
        request.getRequestURI(),
        fields
      ));
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  ResponseEntity<ErrorResponse> handleNotFound(
    ResourceNotFoundException exception,
    HttpServletRequest request
  ) {
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(ErrorResponse.of(
        HttpStatus.NOT_FOUND,
        "RESOURCE_NOT_FOUND",
        exception.getMessage(),
        request.getRequestURI()
      ));
  }

  @ExceptionHandler(NoResourceFoundException.class)
  ResponseEntity<ErrorResponse> handleNoResourceFound(
    NoResourceFoundException exception,
    HttpServletRequest request
  ) {
    return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(ErrorResponse.of(
        HttpStatus.NOT_FOUND,
        "RESOURCE_NOT_FOUND",
        "Resource not found",
        request.getRequestURI()
      ));
  }

  @ExceptionHandler(ConflictException.class)
  ResponseEntity<ErrorResponse> handleConflict(
    ConflictException exception,
    HttpServletRequest request
  ) {
    return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(ErrorResponse.of(
        HttpStatus.CONFLICT,
        "CONFLICT",
        exception.getMessage(),
        request.getRequestURI()
      ));
  }

  @ExceptionHandler(BadRequestException.class)
  ResponseEntity<ErrorResponse> handleBadRequestException(
    BadRequestException exception,
    HttpServletRequest request
  ) {
    return ResponseEntity
      .badRequest()
      .body(ErrorResponse.of(
        HttpStatus.BAD_REQUEST,
        "BAD_REQUEST",
        exception.getMessage(),
        request.getRequestURI()
      ));
  }

  @ExceptionHandler(BadCredentialsException.class)
  ResponseEntity<ErrorResponse> handleBadCredentials(
    BadCredentialsException exception,
    HttpServletRequest request
  ) {
    return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(ErrorResponse.of(
        HttpStatus.UNAUTHORIZED,
        "UNAUTHORIZED",
        exception.getMessage(),
        request.getRequestURI()
      ));
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ErrorResponse> handleAccessDenied(
    AccessDeniedException exception,
    HttpServletRequest request
  ) {
    return ResponseEntity
      .status(HttpStatus.FORBIDDEN)
      .body(ErrorResponse.of(
        HttpStatus.FORBIDDEN,
        "FORBIDDEN",
        "Access is denied",
        request.getRequestURI()
      ));
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class,
    MissingServletRequestParameterException.class
  })
  ResponseEntity<ErrorResponse> handleBadRequest(
    Exception exception,
    HttpServletRequest request
  ) {
    return ResponseEntity
      .badRequest()
      .body(ErrorResponse.of(
        HttpStatus.BAD_REQUEST,
        "BAD_REQUEST",
        exception.getMessage(),
        request.getRequestURI()
      ));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> handleUnexpected(
    Exception exception,
    HttpServletRequest request
  ) {
    return ResponseEntity
      .status(HttpStatus.INTERNAL_SERVER_ERROR)
      .body(ErrorResponse.of(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_SERVER_ERROR",
        "Unexpected server error",
        request.getRequestURI()
      ));
  }
}
