package com.ticket.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket.entity.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void testHandleValidationExceptions() {
        // Given
        FieldError fieldError1 = new FieldError("ticket", "titleTicket", "Title is required");
        FieldError fieldError2 = new FieldError("ticket", "assign", "Assignee is required");
        
        BindException bindException = new BindException(new Object(), "ticket");
        bindException.addError(fieldError1);
        bindException.addError(fieldError2);
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                null, bindException);

        // When
        var response = globalExceptionHandler.handleValidationExceptions(exception);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Title is required", response.getBody().get("titleTicket"));
        assertEquals("Assignee is required", response.getBody().get("assign"));
    }

    @Test
    void testHandleTicketNotFoundException() {
        // Given
        TicketNotFoundException exception = new TicketNotFoundException("Ticket not found with id: 999");

        // When
        var response = globalExceptionHandler.handleTicketNotFoundException(exception);

        // Then
        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Ticket not found with id: 999", response.getBody().get("error"));
    }

    @Test
    void testHandleRuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("Database connection failed");

        // When
        var response = globalExceptionHandler.handleRuntimeException(exception);

        // Then
        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Database connection failed", response.getBody().get("error"));
    }

    @Test
    void testHandleGenericException() {
        // Given
        Exception exception = new Exception("Unexpected error");

        // When
        var response = globalExceptionHandler.handleGenericException(exception);

        // Then
        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("An unexpected error occurred", response.getBody().get("error"));
    }

    @Test
    void testHandleValidationExceptions_EmptyErrors() {
        // Given
        BindException bindException = new BindException(new Object(), "ticket");
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(
                null, bindException);

        // When
        var response = globalExceptionHandler.handleValidationExceptions(exception);

        // Then
        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testHandleTicketNotFoundException_WithId() {
        // Given
        TicketNotFoundException exception = new TicketNotFoundException(123L);

        // When
        var response = globalExceptionHandler.handleTicketNotFoundException(exception);

        // Then
        assertEquals(404, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Ticket not found with id: 123", response.getBody().get("error"));
    }
} 