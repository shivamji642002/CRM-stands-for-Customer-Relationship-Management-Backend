package com.ticket.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ticket.entity.Ticket;
import com.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TicketIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TicketRepository ticketRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        ticketRepository.deleteAll();
    }

    @Test
    void testCompleteTicketLifecycle() throws Exception {
        // Step 1: Create a ticket
        Ticket ticket = createSampleTicket();
        String ticketJson = objectMapper.writeValueAsString(ticket);

        String response = mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ticketJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titleTicket").value("Test Ticket"))
                .andExpect(jsonPath("$.assign").value("John Doe"))
                .andReturn().getResponse().getContentAsString();

        Ticket createdTicket = objectMapper.readValue(response, Ticket.class);
        assertNotNull(createdTicket.getSno());

        // Step 2: Get all tickets (should have 1)
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.hasSize(1)));

        // Step 3: Get the specific ticket
        mockMvc.perform(get("/api/tickets/" + createdTicket.getSno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titleTicket").value("Test Ticket"))
                .andExpect(jsonPath("$.status").value("Open"));

        // Step 4: Update the ticket
        Ticket updateTicket = createSampleTicket();
        updateTicket.setTitleTicket("Updated Ticket");
        updateTicket.setStatus("In Progress");
        updateTicket.setPriority("Medium");
        String updateJson = objectMapper.writeValueAsString(updateTicket);

        mockMvc.perform(put("/api/tickets/" + createdTicket.getSno())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titleTicket").value("Updated Ticket"))
                .andExpect(jsonPath("$.status").value("In Progress"))
                .andExpect(jsonPath("$.priority").value("Medium"));

        // Step 5: Verify the update in database
        List<Ticket> allTickets = ticketRepository.findAll();
        assertEquals(1, allTickets.size());
        assertEquals("Updated Ticket", allTickets.get(0).getTitleTicket());
        assertEquals("In Progress", allTickets.get(0).getStatus());

        // Step 6: Delete the ticket
        mockMvc.perform(delete("/api/tickets/" + createdTicket.getSno()))
                .andExpect(status().isNoContent());

        // Step 7: Verify deletion
        mockMvc.perform(get("/api/tickets/" + createdTicket.getSno()))
                .andExpect(status().isNotFound());

        // Step 8: Verify no tickets exist
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.hasSize(0)));
    }

    @Test
    void testMultipleTicketsOperations() throws Exception {
        // Create multiple tickets
        Ticket ticket1 = createSampleTicket();
        ticket1.setTitleTicket("Ticket 1");
        ticket1.setAssign("Alice");

        Ticket ticket2 = createSampleTicket();
        ticket2.setTitleTicket("Ticket 2");
        ticket2.setAssign("Bob");

        Ticket ticket3 = createSampleTicket();
        ticket3.setTitleTicket("Ticket 3");
        ticket3.setAssign("Charlie");

        // Save all tickets
        String ticket1Json = objectMapper.writeValueAsString(ticket1);
        String ticket2Json = objectMapper.writeValueAsString(ticket2);
        String ticket3Json = objectMapper.writeValueAsString(ticket3);

        String response1 = mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ticket1Json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String response2 = mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ticket2Json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String response3 = mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ticket3Json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Ticket created1 = objectMapper.readValue(response1, Ticket.class);
        Ticket created2 = objectMapper.readValue(response2, Ticket.class);
        Ticket created3 = objectMapper.readValue(response3, Ticket.class);

        // Verify all tickets exist
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.hasSize(3)));

        // Update one ticket
        Ticket updateTicket = createSampleTicket();
        updateTicket.setTitleTicket("Updated Ticket 1");
        updateTicket.setStatus("Completed");
        String updateJson = objectMapper.writeValueAsString(updateTicket);

        mockMvc.perform(put("/api/tickets/" + created1.getSno())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titleTicket").value("Updated Ticket 1"))
                .andExpect(jsonPath("$.status").value("Completed"));

        // Delete one ticket
        mockMvc.perform(delete("/api/tickets/" + created2.getSno()))
                .andExpect(status().isNoContent());

        // Verify remaining tickets
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.hasSize(2)));

        // Verify specific tickets still exist
        mockMvc.perform(get("/api/tickets/" + created1.getSno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titleTicket").value("Updated Ticket 1"));

        mockMvc.perform(get("/api/tickets/" + created3.getSno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titleTicket").value("Ticket 3"));

        // Verify deleted ticket doesn't exist
        mockMvc.perform(get("/api/tickets/" + created2.getSno()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testValidationErrors() throws Exception {
        // Test missing required fields
        Ticket invalidTicket = new Ticket();
        invalidTicket.setTitleTicket("Only title set");
        // Missing assign, status, priority, dateCurrent

        String invalidJson = objectMapper.writeValueAsString(invalidTicket);

        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.assign").value("Assignee is required"))
                .andExpect(jsonPath("$.status").value("Status is required"))
                .andExpect(jsonPath("$.priority").value("Priority is required"));
        // Note: dateCurrent is no longer required as it's auto-set
    }

    @Test
    void testAutomaticDateSetting() throws Exception {
        // Test creating ticket without date (should auto-set)
        Ticket ticketWithoutDate = createSampleTicket();
        ticketWithoutDate.setDateCurrent(null);
        String ticketJson = objectMapper.writeValueAsString(ticketWithoutDate);

        String response = mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ticketJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dateCurrent").exists())
                .andReturn().getResponse().getContentAsString();

        Ticket createdTicket = objectMapper.readValue(response, Ticket.class);
        assertNotNull(createdTicket.getDateCurrent());
        assertEquals(LocalDate.now(), createdTicket.getDateCurrent());
    }

    @Test
    void testCustomDateSetting() throws Exception {
        // Test creating ticket with custom date
        Ticket ticketWithCustomDate = createSampleTicket();
        ticketWithCustomDate.setDateCurrent(LocalDate.of(2024, 1, 15));
        String ticketJson = objectMapper.writeValueAsString(ticketWithCustomDate);

        String response = mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(ticketJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dateCurrent").value("2024-01-15"))
                .andReturn().getResponse().getContentAsString();

        Ticket createdTicket = objectMapper.readValue(response, Ticket.class);
        assertEquals(LocalDate.of(2024, 1, 15), createdTicket.getDateCurrent());
    }

    @Test
    void testErrorScenarios() throws Exception {
        // Test getting non-existent ticket
        mockMvc.perform(get("/api/tickets/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ticket not found with id: 999"));

        // Test updating non-existent ticket
        Ticket updateTicket = createSampleTicket();
        String updateJson = objectMapper.writeValueAsString(updateTicket);

        mockMvc.perform(put("/api/tickets/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ticket not found with id: 999"));

        // Test deleting non-existent ticket
        mockMvc.perform(delete("/api/tickets/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ticket not found with id: 999"));
    }

    private Ticket createSampleTicket() {
        Ticket ticket = new Ticket();
        ticket.setTitleTicket("Test Ticket");
        ticket.setAssign("John Doe");
        ticket.setStatus("Open");
        ticket.setPriority("High");
        ticket.setDateCurrent(LocalDate.now());
        ticket.setSubTask("Test task");
        return ticket;
    }
} 