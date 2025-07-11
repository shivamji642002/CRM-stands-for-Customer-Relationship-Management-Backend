package com.ticket.integration;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SubTicketIntegrationTest {

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
    void testCompleteSubTicketLifecycle() throws Exception {
        // Step 1: Create a parent ticket
        Ticket parentTicket = createSampleTicket();
        String parentTicketJson = objectMapper.writeValueAsString(parentTicket);

        String parentResponse = mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(parentTicketJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.titleTicket").value("Test Ticket"))
                .andReturn().getResponse().getContentAsString();

        Ticket createdParentTicket = objectMapper.readValue(parentResponse, Ticket.class);
        assertNotNull(createdParentTicket.getSno());

        // Step 2: Create a subticket
        SubTicket subTicket = createSampleSubTicket();
        subTicket.setParentTicket(createdParentTicket);
        String subTicketJson = objectMapper.writeValueAsString(subTicket);

        String subTicketResponse = mockMvc.perform(post("/api/subtickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(subTicketJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test SubTicket"))
                .andExpect(jsonPath("$.assign").value("Jane Smith"))
                .andReturn().getResponse().getContentAsString();

        SubTicket createdSubTicket = objectMapper.readValue(subTicketResponse, SubTicket.class);
        assertNotNull(createdSubTicket.getId());

        // Step 3: Get all subtickets (should have 1)
        mockMvc.perform(get("/api/subtickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.hasSize(1)));

        // Step 4: Get the specific subticket
        mockMvc.perform(get("/api/subtickets/" + createdSubTicket.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test SubTicket"))
                .andExpect(jsonPath("$.status").value("In Progress"));

        // Step 5: Get subtickets by parent ticket
        mockMvc.perform(get("/api/subtickets/by-ticket/" + createdParentTicket.getSno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Test SubTicket"));

        // Step 6: Update the subticket
        SubTicket updateSubTicket = createSampleSubTicket();
        updateSubTicket.setTitle("Updated SubTicket");
        updateSubTicket.setStatus("Completed");
        updateSubTicket.setParentTicket(createdParentTicket);
        String updateJson = objectMapper.writeValueAsString(updateSubTicket);

        mockMvc.perform(put("/api/subtickets/" + createdSubTicket.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated SubTicket"))
                .andExpect(jsonPath("$.status").value("Completed"));

        // Step 7: Delete the subticket
        mockMvc.perform(delete("/api/subtickets/" + createdSubTicket.getId()))
                .andExpect(status().isNoContent());

        // Verify subticket is deleted
        mockMvc.perform(get("/api/subtickets/" + createdSubTicket.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void testMultipleSubTicketsForSameParent() throws Exception {
        // Create parent ticket
        Ticket parentTicket = createSampleTicket();
        String parentTicketJson = objectMapper.writeValueAsString(parentTicket);

        String parentResponse = mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(parentTicketJson))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Ticket createdParentTicket = objectMapper.readValue(parentResponse, Ticket.class);

        // Create multiple subtickets
        for (int i = 1; i <= 3; i++) {
            SubTicket subTicket = createSampleSubTicket();
            subTicket.setTitle("SubTicket " + i);
            subTicket.setParentTicket(createdParentTicket);
            String subTicketJson = objectMapper.writeValueAsString(subTicket);

            mockMvc.perform(post("/api/subtickets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(subTicketJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("SubTicket " + i));
        }

        // Verify all subtickets are created
        mockMvc.perform(get("/api/subtickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.hasSize(3)));

        // Verify subtickets by parent
        mockMvc.perform(get("/api/subtickets/by-ticket/" + createdParentTicket.getSno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.hasSize(3)));
    }

    @Test
    void testSubTicketsForDifferentParents() throws Exception {
        // Create two parent tickets
        Ticket parent1 = createSampleTicket();
        parent1.setTitleTicket("Parent 1");
        String parent1Json = objectMapper.writeValueAsString(parent1);

        String parent1Response = mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(parent1Json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Ticket createdParent1 = objectMapper.readValue(parent1Response, Ticket.class);

        Ticket parent2 = createSampleTicket();
        parent2.setTitleTicket("Parent 2");
        String parent2Json = objectMapper.writeValueAsString(parent2);

        String parent2Response = mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(parent2Json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Ticket createdParent2 = objectMapper.readValue(parent2Response, Ticket.class);

        // Create subtickets for each parent
        SubTicket subTicket1 = createSampleSubTicket();
        subTicket1.setTitle("SubTicket for Parent 1");
        subTicket1.setParentTicket(createdParent1);
        String subTicket1Json = objectMapper.writeValueAsString(subTicket1);

        mockMvc.perform(post("/api/subtickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(subTicket1Json))
                .andExpect(status().isCreated());

        SubTicket subTicket2 = createSampleSubTicket();
        subTicket2.setTitle("SubTicket for Parent 2");
        subTicket2.setParentTicket(createdParent2);
        String subTicket2Json = objectMapper.writeValueAsString(subTicket2);

        mockMvc.perform(post("/api/subtickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(subTicket2Json))
                .andExpect(status().isCreated());

        // Verify subtickets by each parent
        mockMvc.perform(get("/api/subtickets/by-ticket/" + createdParent1.getSno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("SubTicket for Parent 1"));

        mockMvc.perform(get("/api/subtickets/by-ticket/" + createdParent2.getSno()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("SubTicket for Parent 2"));
    }

    @Test
    void testValidationErrors() throws Exception {
        // Test missing required fields
        SubTicket invalidSubTicket = new SubTicket();
        invalidSubTicket.setTitle("Only title set");
        // Missing assign, status, priority, dateCurrent

        String invalidJson = objectMapper.writeValueAsString(invalidSubTicket);

        mockMvc.perform(post("/api/subtickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.assign").value("Assignee is required"))
                .andExpect(jsonPath("$.status").value("Status is required"))
                .andExpect(jsonPath("$.priority").value("Priority is required"));
    }

    @Test
    void testErrorScenarios() throws Exception {
        // Test getting non-existent subticket
        mockMvc.perform(get("/api/subtickets/999"))
                .andExpect(status().isNotFound());

        // Test updating non-existent subticket
        SubTicket updateSubTicket = createSampleSubTicket();
        String updateJson = objectMapper.writeValueAsString(updateSubTicket);

        mockMvc.perform(put("/api/subtickets/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isNotFound());

        // Test deleting non-existent subticket
        mockMvc.perform(delete("/api/subtickets/999"))
                .andExpect(status().isNotFound());

        // Test getting subtickets for non-existent parent
        mockMvc.perform(get("/api/subtickets/by-ticket/999"))
                .andExpect(status().isNotFound());
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