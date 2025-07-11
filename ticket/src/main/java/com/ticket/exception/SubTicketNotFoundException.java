package com.ticket.exception;

public class SubTicketNotFoundException extends RuntimeException {
    public SubTicketNotFoundException(Long id) {
        super("SubTicket not found with id: " + id);
    }
} 