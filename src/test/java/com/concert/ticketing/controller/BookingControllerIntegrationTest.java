package com.concert.ticketing.controller;

import com.concert.ticketing.AbstractIntegrationTest;
import com.concert.ticketing.dto.booking.BookingRequest;
import com.concert.ticketing.model.EventsModel;
import com.concert.ticketing.model.TicketCategoryModel;
import com.concert.ticketing.model.VenuesModel;
import com.concert.ticketing.repositories.*;
import com.concert.ticketing.model.UsersModel;
import com.concert.ticketing.model.BookingsModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
public class BookingControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private TicketCategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VenueRepository venueRepository;
    @Autowired
    private BookingRepository bookingRepository;

    private UUID userId;
    private UUID eventId;
    private UUID categoryId;
    private UUID bookingId;

    @BeforeEach
    void setUp() {
        // Cleanup repositories to ensure a clean state
        bookingRepository.deleteAll();
        categoryRepository.deleteAll();
        eventRepository.deleteAll();
        venueRepository.deleteAll();
        userRepository.deleteAll();

        UsersModel user = new UsersModel();
        user.setUsername("bookinguser");
        user.setEmail("booking@example.com");
        user.setPassword("pass");
        user.setFullName("Booking User");
        user.setRole("USER");
        userId = userRepository.save(user).getId();

        VenuesModel venue = new VenuesModel();
        venue.setName("Booking Venue");
        venue.setCapacity(1000);
        venue = venueRepository.save(venue);

        EventsModel concert = new EventsModel();
        concert.setName("Booking Concert");
        concert.setArtist("Booking Artist");
        concert.setVenue(venue);
        concert.setEventDate(ZonedDateTime.now().plusDays(5));
        concert.setTimezone("UTC");
        eventId = eventRepository.save(concert).getId();

        TicketCategoryModel cat = new TicketCategoryModel();
        cat.setName("General");
        cat.setPrice(new BigDecimal("500000"));
        cat.setTotalAllocation(100);
        cat.setAvailableStock(100);
        cat.setEvent(concert);
        categoryId = categoryRepository.save(cat).getId();

        BookingsModel booking = new BookingsModel();
        booking.setUserId(userId);
        booking.setCategory(cat);
        booking.setQuantity(1);
        booking.setTotalAmount(cat.getPrice());
        booking.setStatus("PENDING");
        booking.setCreatedAt(LocalDateTime.now());
        booking.setIdempotencyKey(UUID.randomUUID().toString());
        bookingId = bookingRepository.save(booking).getId();
    }

    @Test
    @WithMockUser(username = "bookinguser", roles = { "USER" })
    public void shouldCreateBooking() throws Exception {
        BookingRequest request = new BookingRequest(userId, eventId, categoryId, 2);

        mockMvc.perform(post("/api/v1/bookings")
                .header("X-Idempotency-Key", UUID.randomUUID().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status", is("Success")));
    }

    @Test
    @WithMockUser(username = "bookinguser", roles = { "USER" })
    public void shouldGetBookingById() throws Exception {
        mockMvc.perform(get("/api/v1/bookings/" + bookingId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("Success")))
                .andExpect(jsonPath("$.data.id", is(bookingId.toString())));
    }

    @Test
    @WithMockUser(username = "bookinguser", roles = { "USER" })
    public void shouldListUserBookings() throws Exception {
        mockMvc.perform(get("/api/v1/bookings")
                .param("userId", userId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("Success")))
                .andExpect(jsonPath("$.data", hasSize(1)));
    }

    @Test
    @WithMockUser(username = "bookinguser", roles = { "USER" })
    public void shouldCancelBooking() throws Exception {
        mockMvc.perform(post("/api/v1/bookings/" + bookingId + "/cancel")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("Success")));
    }
}
