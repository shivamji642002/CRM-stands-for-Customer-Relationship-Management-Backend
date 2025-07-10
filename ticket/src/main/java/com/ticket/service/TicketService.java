package com.ticket.service;

import com.ticket.entity.Ticket;
import com.ticket.exception.TicketNotFoundException;
import com.ticket.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Ticket saveTicket(Ticket ticket) {
        try {
            // dateCurrent will be automatically set by @PrePersist if null
            return ticketRepository.save(ticket);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save ticket: " + e.getMessage(), e);
        }
    }

    public List<Ticket> getAllTickets() {
        try {
            return ticketRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve tickets: " + e.getMessage(), e);
        }
    }

    public Optional<Ticket> getTicketById(Long id) {
        try {
            return ticketRepository.findById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve ticket: " + e.getMessage(), e);
        }
    }

    public Ticket updateTicket(Long id, Ticket updatedTicket) {
        try {
            return ticketRepository.findById(id)
                    .map(ticket -> {
                        ticket.setTitleTicket(updatedTicket.getTitleTicket());
                        ticket.setAssign(updatedTicket.getAssign());
                        ticket.setStatus(updatedTicket.getStatus());
                        ticket.setPriority(updatedTicket.getPriority());
                        // Only update date if provided, otherwise keep existing
                        if (updatedTicket.getDateCurrent() != null) {
                            ticket.setDateCurrent(updatedTicket.getDateCurrent());
                        }
                        ticket.setSubTask(updatedTicket.getSubTask());
                        return ticketRepository.save(ticket);
                    })
                    .orElseThrow(() -> new TicketNotFoundException(id));
        } catch (TicketNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to update ticket: " + e.getMessage(), e);
        }
    }

    public void deleteTicket(Long id) {
        try {
            if (!ticketRepository.existsById(id)) {
                throw new TicketNotFoundException(id);
            }
            ticketRepository.deleteById(id);
        } catch (TicketNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete ticket: " + e.getMessage(), e);
        }
    }
}
