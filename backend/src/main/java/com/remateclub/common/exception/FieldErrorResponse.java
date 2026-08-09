package com.remateclub.common.exception;

public record FieldErrorResponse(
  String field,
  String message
) {
}
