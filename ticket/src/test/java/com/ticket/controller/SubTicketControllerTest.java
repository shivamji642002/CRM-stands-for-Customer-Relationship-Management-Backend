package com.ticket.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.ticket.entity.SubTicket;
import com.ticket.entity.Ticket;
import com.ticket.repository.SubTicketRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubTicketControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private SubTicketRepository subTicketRepository;

    @Autowired
    private TicketRepository ticketRepository;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        subTicketRepository.deleteAll();
        ticketRepository.deleteAll();
    }

    @Test
    void testCreateSubTicket_Success() throws Exception {
        // First create a parent ticket
        Ticket parentTicket = createSampleTicket();
        Ticket savedParentTicket = ticketRepository.save(parentTicket);

        SubTicket subTicket = createSampleSubTicket();
        subTicket.setParentTicket(savedParentTicket);
        String subTicketJson = objectMapper.writeValueAsString(subTicket);

        mockMvc.perform(post("/api/subtickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(subTicketJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test SubTicket"))
                .andExpect(jsonPath("$.assign").value("Jane Smith"))
                .andExpect(jsonPath("$.status").value("In Progress"))
                .andExpect(jsonPath("$.priority").value("Medium"))
                .andExpect(jsonPath("$.subTask").value("Test subtask"));
    }

    @Test
    void testCreateSubTicket_ValidationError_MissingTitle() throws Exception {
        // First create a parent ticket
        Ticket parentTicket = createSampleTicket();
        Ticket savedParentTicket = ticketRepository.save(parentTicket);

        SubTicket subTicket = createSampleSubTicket();
        subTicket.setTitle(null);
        subTicket.setParentTicket(savedParentTicket);
        String subTicketJson = objectMapper.writeValueAsString(subTicket);

        mockMvc.perform(post("/api/subtickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(subTicketJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Title is required"));
    }

    @Test
    void testGetAllSubTickets_Success() throws Exception {
        // First create a parent ticket
        Ticket parentTicket = createSampleTicket();
        Ticket savedParentTicket = ticketRepository.save(parentTicket);

        // Create and save a subTicket
        SubTicket subTicket = createSampleSubTicket();
        subTicket.setParentTicket(savedParentTicket);
        subTicketRepository.save(subTicket);

        mockMvc.perform(get("/api/subtickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Test SubTicket"));
    }

    @Test
    void testGetSubTicketById_Success() throws Exception {
        // First create a parent ticket
        Ticket parentTicket = createSampleTicket();
        Ticket savedParentTicket = ticketRepository.save(parentTicket);

        // Create and save a subTicket
        SubTicket subTicket = createSampleSubTicket();
        subTicket.setParentTicket(savedParentTicket);
        SubTicket savedSubTicket = subTicketRepository.save(subTicket);

        mockMvc.perform(get("/api/subtickets/" + savedSubTicket.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test SubTicket"))
                .andExpect(jsonPath("$.assign").value("Jane Smith"))
                .andExpect(jsonPath("$.status").value("In Progress"));
    }

    @Test
    void testGetSubTicketById_NotFound() throws Exception {
        mockMvc.perform(get("/api/subtickets/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateSubTicket_Success() throws Exception {
        // First create a parent ticket
        Ticket parentTicket = createSampleTicket();
        Ticket savedParentTicket = ticketRepository.save(parentTicket);

        // Create and save a subTicket
        SubTicket subTicket = createSampleSubTicket();
        subTicket.setParentTicket(savedParentTicket);
        SubTicket savedSubTicket = subTicketRepository.save(subTicket);

        // Update the subTicket
        SubTicket updateSubTicket = createSampleSubTicket();
        updateSubTicket.setTitle("Updated SubTicket");
        updateSubTicket.setStatus("Completed");
        updateSubTicket.setParentTicket(savedParentTicket);
        String updateJson = objectMapper.writeValueAsString(updateSubTicket);

        mockMvc.perform(put("/api/subtickets/" + savedSubTicket.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated SubTicket"))
                .andExpect(jsonPath("$.status").value("Completed"));
    }

    @Test
    void testUpdateSubTicket_NotFound() throws Exception {
        // First create a parent ticket
        Ticket parentTicket = createSampleTicket();
        Ticket savedParentTicket = ticketRepository.save(parentTicket);

        SubTicket updateSubTicket = createSampleSubTicket();
        updateSubTicket.setParentTicket(savedParentTicket);
        String updateJson = objectMapper.writeValueAsString(updateSubTicket);

        mockMvc.perform(put("/api/subtickets/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteSubTicket_Success() throws Exception {
        // First create a parent ticket
        Ticket parentTicket = createSampleTicket();
        Ticket savedParentTicket = ticketRepository.save(parentTicket);

        // Create and save a subTicket
        SubTicket subTicket = createSampleSubTicket();
        subTicket.setParentTicket(savedParentTicket);
        SubTicket savedSubTicket = subTicketRepository.save(subTicket);

        mockMvc.perform(delete("/api/subtickets/" + savedSubTicket.getId()))
                .andExpect(status().isNoContent());

        // Verify subTicket is deleted
        mockMvc.perform(get("/api/subtickets/" + savedSubTicket.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteSubTicket_NotFound() throws Exception {
        mockMvc.perform(delete("/api/subtickets/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testExceptionHandler_SubTicketNotFound() throws Exception {
        // Test that the exception handler properly handles SubTicketNotFoundException
        mockMvc.perform(get("/api/subtickets/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetSubTicketsByTicket_Success() throws Exception {
        // First create a parent ticket
        Ticket parentTicket = createSampleTicket();
        Ticket savedParentTicket = ticketRepository.save(parentTicket);

        // Create and save multiple subTickets
        SubTicket subTicket1 = createSampleSubTicket();
        subTicket1.setTitle("SubTicket 1");
        subTicket1.setParentTicket(savedParentTicket);
        subTicketRepository.save(subTicket1);

        SubTicket subTicket2 = createSampleSubTicket();
        subTicket2.setTitle("SubTicket 2");
        subTicket2.setParentTicket(savedParentTicket);
        subTicketRepository.save(subTicket2);

        mockMvc.perform(get("/api/subtickets/by-ticket/" + savedParentTicket.getSno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("SubTicket 1"))
                .andExpect(jsonPath("$[1].title").value("SubTicket 2"));
    }

    @Test
    void testGetSubTicketsByTicket_NotFound() throws Exception {
        mockMvc.perform(get("/api/subtickets/by-ticket/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateSubTicket_WithOptionalFields() throws Exception {
        // First create a parent ticket
        Ticket parentTicket = createSampleTicket();
        Ticket savedParentTicket = ticketRepository.save(parentTicket);

        SubTicket subTicket = createSampleSubTicket();
        subTicket.setSubTask("Optional subtask");
        subTicket.setParentTicket(savedParentTicket);
        String subTicketJson = objectMapper.writeValueAsString(subTicket);

        mockMvc.perform(post("/api/subtickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(subTicketJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subTask").value("Optional subtask"));
    }

    @Test
    void testCreateSubTicket_WithoutOptionalFields() throws Exception {
        // First create a parent ticket
        Ticket parentTicket = createSampleTicket();
        Ticket savedParentTicket = ticketRepository.save(parentTicket);

        SubTicket subTicket = createSampleSubTicket();
        subTicket.setSubTask(null);
        subTicket.setParentTicket(savedParentTicket);
        String subTicketJson = objectMapper.writeValueAsString(subTicket);

        mockMvc.perform(post("/api/subtickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(subTicketJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subTask").isEmpty());
    }

    @Test
    void testCreateSubTicket_DateSerialization() throws Exception {
        // First create a parent ticket
        Ticket parentTicket = createSampleTicket();
        Ticket savedParentTicket = ticketRepository.save(parentTicket);

        SubTicket subTicket = createSampleSubTicket();
        subTicket.setDateCurrent(LocalDate.of(2024, 1, 15));
        subTicket.setParentTicket(savedParentTicket);
        String subTicketJson = objectMapper.writeValueAsString(subTicket);

        mockMvc.perform(post("/api/subtickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(subTicketJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dateCurrent").value("2024-01-15"));
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

    private SubTicket createSampleSubTicket() {
        SubTicket subTicket = new SubTicket();
        subTicket.setTitle("Test SubTicket");
        subTicket.setAssign("Jane Smith");
        subTicket.setStatus("In Progress");
        subTicket.setPriority("Medium");
        subTicket.setDateCurrent(LocalDate.now());
        subTicket.setSubTask("Test subtask");
        return subTicket;
    }
} 