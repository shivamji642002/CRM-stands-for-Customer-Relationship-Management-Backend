package com.ticket.controller;

import com.ticket.entity.Ticket;
import com.ticket.exception.TicketNotFoundException;
import com.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "http://localhost:4200")
@Tag(name = "Ticket Management", description = "APIs for managing support tickets and tasks")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create a new ticket", description = "Creates a new ticket with the provided details. The dateCurrent field will be automatically set to the current date if not provided.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ticket created successfully", content = @Content(schema = @Schema(implementation = Ticket.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
    })
    public ResponseEntity<Ticket> createTicket(
            @Parameter(description = "Ticket object to be created", required = true)
            @Valid @RequestBody Ticket ticket) {
        Ticket savedTicket = ticketService.saveTicket(ticket);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTicket);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get all tickets", description = "Retrieves a list of all tickets in the system. Returns an empty array if no tickets exist.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved tickets", content = @Content(schema = @Schema(implementation = Ticket.class)))
    })
    public ResponseEntity<List<Ticket>> getAllTickets() {
        List<Ticket> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get ticket by ID", description = "Retrieves a specific ticket by its unique identifier.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved ticket", content = @Content(schema = @Schema(implementation = Ticket.class))),
        @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content)
    })
    public ResponseEntity<Ticket> getTicketById(
            @Parameter(description = "ID of the ticket to retrieve", example = "1", required = true)
            @PathVariable Long id) {
        Optional<Ticket> ticket = ticketService.getTicketById(id);
        return ticket.map(ResponseEntity::ok)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update a ticket", description = "Updates an existing ticket with new information. Only provided fields will be updated. The dateCurrent field will only be updated if explicitly provided.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ticket updated successfully", content = @Content(schema = @Schema(implementation = Ticket.class))),
        @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content)
    })
    public ResponseEntity<Ticket> updateTicket(
            @Parameter(description = "ID of the ticket to update", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated ticket information", required = true)
            @Valid @RequestBody Ticket ticket) {
        Ticket updatedTicket = ticketService.updateTicket(id, ticket);
        return ResponseEntity.ok(updatedTicket);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a ticket", description = "Deletes a ticket by its unique identifier. This action cannot be undone.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Ticket deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Ticket not found", content = @Content)
    })
    public ResponseEntity<Void> deleteTicket(
            @Parameter(description = "ID of the ticket to delete", example = "1", required = true)
            @PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Health check", description = "Simple health check endpoint to verify API is running and accessible.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "API is healthy", content = @Content)
    })
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("{\"status\": \"UP\", \"message\": \"Ticket API is running\"}");
    }

    @GetMapping(value = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Simple test endpoint", description = "Basic test endpoint to verify API connectivity without database access.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Test successful", content = @Content)
    })
    public ResponseEntity<String> testEndpoint() {
        return ResponseEntity.ok("{\"message\": \"Test endpoint working\"}");
    }

    @GetMapping(value = "/db-test", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Database connection test", description = "Tests the database connection and returns the current number of tickets in the system.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Database connection successful", content = @Content),
        @ApiResponse(responseCode = "500", description = "Database connection failed", content = @Content)
    })
    public ResponseEntity<String> databaseTest() {
        try {
            List<Ticket> tickets = ticketService.getAllTickets();
            return ResponseEntity.ok("{\"message\": \"Database connection successful\", \"ticketCount\": " + tickets.size() + "}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Database connection failed: " + e.getMessage() + "\"}");
        }
    }
}
