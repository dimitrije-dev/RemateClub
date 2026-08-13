package com.remateclub.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.remateclub.common.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

final class SecurityErrorWriter {

  private SecurityErrorWriter() {
  }

  static void write(
    ObjectMapper objectMapper,
    HttpServletResponse response,
    HttpStatus status,
    String code,
    String message,
    String path
  ) throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(
      response.getOutputStream(),
      ErrorResponse.of(status, code, message, path)
    );
  }
}
