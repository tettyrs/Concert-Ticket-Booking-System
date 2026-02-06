package com.concert.ticketing.controller;

import com.concert.ticketing.AbstractIntegrationTest;
import com.concert.ticketing.dto.concert.ConcertRequest;
import com.concert.ticketing.model.VenuesModel;
import com.concert.ticketing.repositories.EventRepository;
import com.concert.ticketing.repositories.VenueRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class ConcertControllerIntegrationTest extends AbstractIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private EventRepository eventRepository;

        @Autowired
        private VenueRepository venueRepository;

        @BeforeEach
        void setUp() {
                if (venueRepository.findByNameIgnoreCase("Grand Hall").isEmpty()) {
                        VenuesModel venue = new VenuesModel();
                        venue.setName("Grand Hall");
                        venue.setCapacity(5000);
                        venue.setAddress("Test Address");
                        venueRepository.save(venue);
                }
        }

        @Test
        @WithMockUser(username = "admin", roles = { "ADMIN" })
        public void shouldCreateConcertAsAdmin() throws Exception {
                ConcertRequest.TicketCategory cat = new ConcertRequest.TicketCategory();
                cat.setCategoryName("VIP");
                cat.setBasePrice(new BigDecimal("1000000"));
                cat.setTotalAllocation(100);

                ConcertRequest request = new ConcertRequest();
                request.setName("Great Concert");
                request.setArtist("Famous Artist");
                request.setVenue("Grand Hall");
                request.setDatetime(ZonedDateTime.now().plusDays(10));
                request.setTimezone("UTC");
                request.setCategories(List.of(cat));

                mockMvc.perform(post("/api/v1/concerts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.status", is("Success")));
        }

        @Test
        @WithMockUser(username = "user", roles = { "USER" })
        public void shouldFailCreateConcertAsUser() throws Exception {
                ConcertRequest request = new ConcertRequest();
                request.setName("Forbidden Concert");
                request.setVenue("Grand Hall");

                mockMvc.perform(post("/api/v1/concerts")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isForbidden());
        }

        @Test
        public void shouldGetAllConcertsPublicly() throws Exception {
                mockMvc.perform(get("/api/v1/concerts"))
                                .andExpect(status().isOk());
        }
}
