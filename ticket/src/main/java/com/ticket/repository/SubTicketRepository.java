package com.ticket.repository;

import com.ticket.entity.SubTicket;
import com.ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubTicketRepository extends JpaRepository<SubTicket, Long> {
    List<SubTicket> findByParentTicket(Ticket parentTicket);
} 