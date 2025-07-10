package com.ticket.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ticket entity representing a support ticket or task")
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier for the ticket", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long sno;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    @Schema(description = "Title or summary of the ticket", example = "Fix login authentication bug", required = true)
    private String titleTicket;

    @NotBlank(message = "Assignee is required")
    @Column(nullable = false)
    @Schema(description = "Person or team assigned to work on this ticket", example = "John Doe", required = true)
    private String assign;

    @NotBlank(message = "Status is required")
    @Column(nullable = false)
    @Schema(description = "Current status of the ticket", 
            example = "Open", 
            allowableValues = {"Open", "In Progress", "Completed", "Closed", "Cancelled"},
            required = true)
    private String status;

    @NotBlank(message = "Priority is required")
    @Column(nullable = false)
    @Schema(description = "Priority level of the ticket", 
            example = "High", 
            allowableValues = {"Low", "Medium", "High", "Critical"},
            required = true)
    private String priority;

    @Column(nullable = false)
    @Schema(description = "Date when the ticket was created (automatically set if not provided)", 
            example = "2024-01-15",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDate dateCurrent;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Additional details, subtasks, or notes for the ticket", 
            example = "Investigate authentication flow and implement proper error handling")
    private String subTask;

    @PrePersist
    protected void onCreate() {
        if (dateCurrent == null) {
            dateCurrent = LocalDate.now();
        }
    }
}
