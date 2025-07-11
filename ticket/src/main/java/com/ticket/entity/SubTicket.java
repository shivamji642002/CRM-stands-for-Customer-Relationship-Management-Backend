package com.ticket.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "subtickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Assignee is required")
    @Column(nullable = false)
    private String assign;

    @NotBlank(message = "Status is required")
    @Column(nullable = false)
    private String status;

    @NotBlank(message = "Priority is required")
    @Column(nullable = false)
    private String priority;

    @Column(nullable = false)
    private LocalDate dateCurrent;

    @Column(columnDefinition = "TEXT")
    private String subTask;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket parentTicket;

    @PrePersist
    protected void onCreate() {
        if (dateCurrent == null) {
            dateCurrent = LocalDate.now();
        }
    }
} 