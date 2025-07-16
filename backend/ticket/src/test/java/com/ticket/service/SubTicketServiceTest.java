package com.ticket.service;

import com.ticket.entity.SubTicket;
import com.ticket.entity.Ticket;
import com.ticket.repository.SubTicketRepository;
import com.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubTicketServiceTest {

    @Mock
    private SubTicketRepository subTicketRepository;

    @Mock
    private TicketRepository ticketRepository; // Keep this mock, even if not directly used in all tests

    @InjectMocks
    private SubTicketService subTicketService;

    private SubTicket sampleSubTicket;
    private Ticket sampleParentTicket;

    @BeforeEach
    void setUp() {
        sampleParentTicket = createSampleTicket();
        sampleSubTicket = createSampleSubTicket();
        sampleSubTicket.setParentTicket(sampleParentTicket);
    }

    @Test
    void testSaveSubTicket_Success() {
        // Given
        when(subTicketRepository.save(any(SubTicket.class))).thenReturn(sampleSubTicket);

        // When
        SubTicket savedSubTicket = subTicketService.saveSubTicket(sampleSubTicket);

        // Then
        assertNotNull(savedSubTicket);
        assertEquals("Test SubTicket", savedSubTicket.getTitle());
        assertEquals("Jane Smith", savedSubTicket.getAssign());
        verify(subTicketRepository, times(1)).save(sampleSubTicket);
    }

    @Test
    void testGetAllSubTickets_Success() {
        // Given
        List<SubTicket> expectedSubTickets = Arrays.asList(sampleSubTicket, createSampleSubTicket());
        when(subTicketRepository.findAll()).thenReturn(expectedSubTickets);

        // When
        List<SubTicket> actualSubTickets = subTicketService.getAllSubTickets();

        // Then
        assertNotNull(actualSubTickets);
        assertEquals(2, actualSubTickets.size());
        verify(subTicketRepository, times(1)).findAll();
    }

    @Test
    void testGetSubTicketById_Success() {
        // Given
        Long subTicketId = 1L;
        when(subTicketRepository.findById(subTicketId)).thenReturn(Optional.of(sampleSubTicket));

        // When
        Optional<SubTicket> foundSubTicket = subTicketService.getSubTicketById(subTicketId);

        // Then
        assertTrue(foundSubTicket.isPresent());
        assertEquals(sampleSubTicket.getTitle(), foundSubTicket.get().getTitle());
        verify(subTicketRepository, times(1)).findById(subTicketId);
    }

    @Test
    void testGetSubTicketById_NotFound() {
        // Given
        Long subTicketId = 999L;
        when(subTicketRepository.findById(subTicketId)).thenReturn(Optional.empty());

        // When
        Optional<SubTicket> foundSubTicket = subTicketService.getSubTicketById(subTicketId);

        // Then
        assertFalse(foundSubTicket.isPresent());
        verify(subTicketRepository, times(1)).findById(subTicketId);
    }

    @Test
    void testGetSubTicketsByParent_Success() {
        // Given
        List<SubTicket> expectedSubTickets = Arrays.asList(sampleSubTicket, createSampleSubTicket());
        when(subTicketRepository.findByParentTicket(sampleParentTicket)).thenReturn(expectedSubTickets);

        // When
        List<SubTicket> actualSubTickets = subTicketService.getSubTicketsByParent(sampleParentTicket);

        // Then
        assertNotNull(actualSubTickets);
        assertEquals(2, actualSubTickets.size());
        verify(subTicketRepository, times(1)).findByParentTicket(sampleParentTicket);
    }

    @Test
    void testDeleteSubTicket_Success() {
        // Given
        Long subTicketId = 1L;
        // Mock existsById if deleteById is conditionally called based on existence
        when(subTicketRepository.existsById(subTicketId)).thenReturn(true); 

        // When
        subTicketService.deleteSubTicket(subTicketId);

        // Then
        verify(subTicketRepository, times(1)).deleteById(subTicketId);
    }

    @Test
    void testSaveSubTicket_WithParentTicket() {
        // Given
        SubTicket subTicketWithParent = createSampleSubTicket();
        subTicketWithParent.setParentTicket(sampleParentTicket);
        when(subTicketRepository.save(any(SubTicket.class))).thenReturn(subTicketWithParent);

        // When
        SubTicket savedSubTicket = subTicketService.saveSubTicket(subTicketWithParent);

        // Then
        assertNotNull(savedSubTicket);
        assertNotNull(savedSubTicket.getParentTicket());
        assertEquals(sampleParentTicket.getSno(), savedSubTicket.getParentTicket().getSno());
        verify(subTicketRepository, times(1)).save(subTicketWithParent);
    }

    @Test
    void testGetAllSubTickets_EmptyList() {
        // Given
        when(subTicketRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<SubTicket> actualSubTickets = subTicketService.getAllSubTickets();

        // Then
        assertNotNull(actualSubTickets);
        assertTrue(actualSubTickets.isEmpty());
        verify(subTicketRepository, times(1)).findAll();
    }

    @Test
    void testGetSubTicketsByParent_EmptyList() {
        // Given
        when(subTicketRepository.findByParentTicket(sampleParentTicket)).thenReturn(Arrays.asList());

        // When
        List<SubTicket> actualSubTickets = subTicketService.getSubTicketsByParent(sampleParentTicket);

        // Then
        assertNotNull(actualSubTickets);
        assertTrue(actualSubTickets.isEmpty());
        verify(subTicketRepository, times(1)).findByParentTicket(sampleParentTicket);
    }

    @Test
    void testSubTicketPersistence() {
        // Given
        SubTicket subTicket = createSampleSubTicket();
        subTicket.setDescription("A very long subtask that might contain a lot of text and should be stored properly in the database"); // Changed setSubTask to setDescription
        when(subTicketRepository.save(any(SubTicket.class))).thenReturn(subTicket);

        // When
        SubTicket savedSubTicket = subTicketService.saveSubTicket(subTicket);

        // Then
        assertNotNull(savedSubTicket);
        assertEquals("A very long subtask that might contain a lot of text and should be stored properly in the database", 
                    savedSubTicket.getDescription()); // Changed getSubTask to getDescription
        verify(subTicketRepository, times(1)).save(subTicket);
    }

    @Test
    void testLoadSubTicketHierarchy_NotImplemented() {
        Long subTicketId = 1L;
        assertThrows(UnsupportedOperationException.class, () -> {
            subTicketService.loadSubTicketHierarchy(subTicketId);
        });
    }

    private Ticket createSampleTicket() {
        Ticket ticket = new Ticket();
        ticket.setSno(1L);
        ticket.setTitleTicket("Test Ticket");
        ticket.setAssign("John Doe");
        ticket.setStatus("Open");
        ticket.setPriority("High");
        ticket.setDateCurrent(LocalDate.now());
        ticket.setDescription("Test task description"); // Changed setSubTask to setDescription
        return ticket;
    }

    private SubTicket createSampleSubTicket() {
        SubTicket subTicket = new SubTicket();
        subTicket.setId(1L);
        subTicket.setTitle("Test SubTicket");
        subTicket.setAssign("Jane Smith");
        subTicket.setStatus("In Progress");
        subTicket.setPriority("Medium");
        subTicket.setDateCurrent(LocalDate.now());
        subTicket.setDescription("Test subtask description"); // Changed setSubTask to setDescription
        return subTicket;
    }
}