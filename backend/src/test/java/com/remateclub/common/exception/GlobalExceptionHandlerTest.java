package com.remateclub.common.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    validator.afterPropertiesSet();

    mockMvc = MockMvcBuilders
      .standaloneSetup(new TestController())
      .setControllerAdvice(new GlobalExceptionHandler())
      .setValidator(validator)
      .build();
  }

  @Test
  void returnsConsistentJsonForValidationErrors() throws Exception {
    mockMvc.perform(post("/test/validation")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.status").value(400))
      .andExpect(jsonPath("$.error").value("Bad Request"))
      .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
      .andExpect(jsonPath("$.message").value("Validation failed"))
      .andExpect(jsonPath("$.path").value("/test/validation"))
      .andExpect(jsonPath("$.fields[0].field").value("name"))
      .andExpect(jsonPath("$.fields[0].message").exists());
  }

  @Test
  void returnsConsistentJsonForNotFoundErrors() throws Exception {
    mockMvc.perform(get("/test/not-found"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.status").value(404))
      .andExpect(jsonPath("$.error").value("Not Found"))
      .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
      .andExpect(jsonPath("$.message").value("User not found"))
      .andExpect(jsonPath("$.path").value("/test/not-found"));
  }

  @Test
  void returnsConsistentJsonForConflictErrors() throws Exception {
    mockMvc.perform(post("/test/conflict"))
      .andExpect(status().isConflict())
      .andExpect(jsonPath("$.status").value(409))
      .andExpect(jsonPath("$.error").value("Conflict"))
      .andExpect(jsonPath("$.code").value("CONFLICT"))
      .andExpect(jsonPath("$.message").value("Email already exists"))
      .andExpect(jsonPath("$.path").value("/test/conflict"));
  }

  @RestController
  @RequestMapping("/test")
  public static class TestController {

    @PostMapping("/validation")
    void validation(@Valid @RequestBody TestRequest request) {
    }

    @GetMapping("/not-found")
    void notFound() {
      throw new ResourceNotFoundException("User not found");
    }

    @PostMapping("/conflict")
    void conflict() {
      throw new ConflictException("Email already exists");
    }
  }

  record TestRequest(@NotBlank String name) {
  }
}
