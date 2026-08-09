package com.remateclub.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
  Instant timestamp,
  int status,
  String error,
  String code,
  String message,
  String path,
  List<FieldErrorResponse> fields
) {

  public static ErrorResponse of(
    HttpStatus status,
    String code,
    String message,
    String path
  ) {
    return new ErrorResponse(
      Instant.now(),
      status.value(),
      status.getReasonPhrase(),
      code,
      message,
      path,
      List.of()
    );
  }

  public static ErrorResponse validation(
    String message,
    String path,
    List<FieldErrorResponse> fields
  ) {
    HttpStatus status = HttpStatus.BAD_REQUEST;

    return new ErrorResponse(
      Instant.now(),
      status.value(),
      status.getReasonPhrase(),
      "VALIDATION_FAILED",
      message,
      path,
      fields
    );
  }
}
