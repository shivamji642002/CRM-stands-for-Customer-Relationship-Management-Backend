package com.ticket.service;

import com.ticket.entity.SubTicket;
import com.ticket.entity.Ticket;
import com.ticket.exception.SubTicketNotFoundException;
import com.ticket.repository.SubTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SubTicketService {
    private final SubTicketRepository subTicketRepository;

    public SubTicketService(SubTicketRepository subTicketRepository) {
        this.subTicketRepository = subTicketRepository;
    }

    public SubTicket saveSubTicket(SubTicket subTicket) {
        try {
            return subTicketRepository.save(subTicket);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save subticket: " + e.getMessage(), e);
        }
    }

    public List<SubTicket> getAllSubTickets() {
        try {
            return subTicketRepository.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve subtickets: " + e.getMessage(), e);
        }
    }

    public Optional<SubTicket> getSubTicketById(Long id) {
        try {
            return subTicketRepository.findById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve subticket: " + e.getMessage(), e);
        }
    }

    public SubTicket getSubTicketByIdOrThrow(Long id) {
        return getSubTicketById(id)
                .orElseThrow(() -> new SubTicketNotFoundException(id));
    }

    public List<SubTicket> getSubTicketsByParent(Ticket parentTicket) {
        try {
            return subTicketRepository.findByParentTicket(parentTicket);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve subtickets by parent: " + e.getMessage(), e);
        }
    }

    public SubTicket updateSubTicket(Long id, SubTicket updatedSubTicket) {
        try {
            return subTicketRepository.findById(id)
                    .map(existingSubTicket -> {
                        existingSubTicket.setTitle(updatedSubTicket.getTitle());
                        existingSubTicket.setAssign(updatedSubTicket.getAssign());
                        existingSubTicket.setStatus(updatedSubTicket.getStatus());
                        existingSubTicket.setPriority(updatedSubTicket.getPriority());
                        if (updatedSubTicket.getDateCurrent() != null) {
                            existingSubTicket.setDateCurrent(updatedSubTicket.getDateCurrent());
                        }
                        existingSubTicket.setSubTask(updatedSubTicket.getSubTask());
                        existingSubTicket.setParentTicket(updatedSubTicket.getParentTicket());
                        return subTicketRepository.save(existingSubTicket);
                    })
                    .orElseThrow(() -> new SubTicketNotFoundException(id));
        } catch (Exception e) {
            throw new RuntimeException("Failed to update subticket: " + e.getMessage(), e);
        }
    }

    public void deleteSubTicket(Long id) {
        try {
            if (!subTicketRepository.existsById(id)) {
                throw new SubTicketNotFoundException(id);
            }
            subTicketRepository.deleteById(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete subticket: " + e.getMessage(), e);
        }
    }
} 