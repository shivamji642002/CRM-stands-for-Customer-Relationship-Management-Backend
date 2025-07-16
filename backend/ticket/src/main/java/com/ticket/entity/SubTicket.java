
package com.ticket.entity;

import java.util.List;
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

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description;

    // Existing relationship with parent Ticket
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket parentTicket;

    // ✅ Recursive: parent sub-task
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_subticket_id")
    private SubTicket parentSubTicket;

    // ✅ Recursive: list of sub-sub-tasks
    @OneToMany(mappedBy = "parentSubTicket", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SubTicket> subTasks;

    @PrePersist
    protected void onCreate() {
        if (dateCurrent == null) {
            dateCurrent = LocalDate.now();
        }
    }
}
