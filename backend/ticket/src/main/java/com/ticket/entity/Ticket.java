
package com.ticket.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sno;

    @NotBlank(message = "Title is required")
    @Column(nullable = false)
    private String titleTicket;

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

    @Lob // For large text objects (HTML from Quill)
    @Column(columnDefinition = "LONGTEXT") // Use LONGTEXT for very large HTML content
    private String description; // Stores rich text (HTML)

    @Lob // For large binary objects (PDF file)
    @Column(name = "pdf_data", columnDefinition="LONGBLOB")
    private byte[] pdfData; // Stores the PDF binary data

    @Lob // For large binary objects (DOCX file)
    @Column(name = "docx_data", columnDefinition="LONGBLOB")
    private byte[] docxData; // Stores the DOCX binary data

    @OneToMany(mappedBy = "parentTicket", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<SubTicket> subTickets;

    @OneToMany(mappedBy = "parentTicket", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Link> links;

    @OneToMany(mappedBy = "sourceTicket", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Relationship> relationships;

    @PrePersist
    protected void onCreate() {
        if (dateCurrent == null) {
            dateCurrent = LocalDate.now();
        }
    }
}


