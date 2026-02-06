package com.concert.ticketing.controller;

import com.concert.ticketing.AbstractIntegrationTest;
import com.concert.ticketing.model.EventsModel;
import com.concert.ticketing.model.TicketCategoryModel;
import com.concert.ticketing.model.VenuesModel;
import com.concert.ticketing.repositories.EventRepository;
import com.concert.ticketing.repositories.TicketCategoryRepository;
import com.concert.ticketing.repositories.VenueRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class ConcertPricingControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private TicketCategoryRepository categoryRepository;
    @Autowired
    private VenueRepository venueRepository;

    private UUID eventId;

    @BeforeEach
    void setUp() {
        VenuesModel venue = new VenuesModel();
        venue.setName("Pricing Venue");
        venue.setCapacity(2000);
        venue = venueRepository.save(venue);

        EventsModel concert = new EventsModel();
        concert.setName("Pricing Concert");
        concert.setArtist("Pricing Artist");
        concert.setVenue(venue);
        concert.setEventDate(ZonedDateTime.now().plusDays(5));
        concert.setTimezone("UTC");
        concert = eventRepository.save(concert);
        eventId = concert.getId();

        TicketCategoryModel cat1 = new TicketCategoryModel();
        cat1.setName("VIP");
        cat1.setPrice(new BigDecimal("1000000"));
        cat1.setTotalAllocation(50);
        cat1.setAvailableStock(50);
        cat1.setEvent(concert);
        categoryRepository.save(cat1);

        TicketCategoryModel cat2 = new TicketCategoryModel();
        cat2.setName("Regular");
        cat2.setPrice(new BigDecimal("500000"));
        cat2.setTotalAllocation(100);
        cat2.setAvailableStock(100);
        cat2.setEvent(concert);
        categoryRepository.save(cat2);
    }

    @Test
    public void shouldGetPricing() throws Exception {
        mockMvc.perform(get("/api/v1/concerts/" + eventId + "/pricing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("Success")))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }

    @Test
    public void shouldGetAvailability() throws Exception {
        mockMvc.perform(get("/api/v1/concerts/" + eventId + "/availability"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("Success")))
                .andExpect(jsonPath("$.data", hasSize(2)));
    }
}
