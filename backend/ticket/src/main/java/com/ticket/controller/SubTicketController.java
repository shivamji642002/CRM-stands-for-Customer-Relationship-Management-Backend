package com.ticket.controller;

import com.ticket.entity.SubTicket;
import com.ticket.entity.Ticket;
import com.ticket.service.SubTicketService;
import com.ticket.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/subtickets")
@CrossOrigin(origins = "http://localhost:4200")
public class SubTicketController {
    private final SubTicketService subTicketService;
    private final TicketService ticketService;

    public SubTicketController(SubTicketService subTicketService, TicketService ticketService) {
        this.subTicketService = subTicketService;
        this.ticketService = ticketService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubTicket> createSubTicket(@Valid @RequestBody SubTicket subTicket) {
        SubTicket saved = subTicketService.saveSubTicket(subTicket);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SubTicket>> getAllSubTickets() {
        List<SubTicket> subTickets = subTicketService.getAllSubTickets();
        return ResponseEntity.ok(subTickets);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubTicket> getSubTicketById(@PathVariable Long id) {
        Optional<SubTicket> subTicket = subTicketService.getSubTicketById(id);
        return subTicket.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SubTicket> updateSubTicket(@PathVariable Long id, @Valid @RequestBody SubTicket subTicket) {
        SubTicket updated = subTicketService.updateSubTicket(id, subTicket);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubTicket(@PathVariable Long id) {
        subTicketService.deleteSubTicket(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/by-ticket/{ticketId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SubTicket>> getSubTicketsByTicket(@PathVariable Long ticketId) {
        Optional<Ticket> ticket = ticketService.getTicketById(ticketId);
        if (ticket.isPresent()) {
            List<SubTicket> subTickets = subTicketService.getSubTicketsByParent(ticket.get());
            return ResponseEntity.ok(subTickets);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/tree/{id}")
    public ResponseEntity<SubTicket> getSubTicketTree(@PathVariable Long id) {
        SubTicket fullTree = subTicketService.loadSubTicketHierarchy(id);
        return ResponseEntity.ok(fullTree);
    }

} 