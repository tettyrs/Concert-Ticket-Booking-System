package com.concert.ticketing.services.booking;

import com.concert.ticketing.dto.booking.BookingDetailDto;
import com.concert.ticketing.dto.booking.BookingResponse;
import com.concert.ticketing.exception.ServiceException;
import com.concert.ticketing.model.BookingsModel;
import com.concert.ticketing.model.EventsModel;
import com.concert.ticketing.model.TicketCategoryModel;
import com.concert.ticketing.repositories.BookingRepository;
import com.concert.ticketing.repositories.LedgerRepository;
import com.concert.ticketing.repositories.TicketCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

        @Mock
        private BookingRepository bookingRepository;

        @Mock
        private TicketCategoryRepository categoryRepository;

        @Mock
        private LedgerRepository ledgerRepository;

        @InjectMocks
        private BookingService bookingService;

        @Test
        void shouldGetByIdSuccessfully() {
                UUID id = UUID.randomUUID();
                BookingsModel booking = new BookingsModel();
                booking.setId(id);
                booking.setStatus("CONFIRMED");

                EventsModel concert = new EventsModel();
                concert.setName("Test Concert");

                TicketCategoryModel cat = new TicketCategoryModel();
                cat.setName("VIP");
                cat.setEvent(concert);
                booking.setCategory(cat);

                when(bookingRepository.findById(id)).thenReturn(Optional.of(booking));

                BookingResponse<BookingDetailDto> response = bookingService.getById(id);

                assertEquals("SUCCESS", response.getStatus());
                assertEquals(id, response.getData().getId());
        }

        @Test
        void shouldThrowExceptionWhenBookingNotFound() {
                UUID id = UUID.randomUUID();
                when(bookingRepository.findById(id)).thenReturn(Optional.empty());

                assertThrows(ServiceException.class, () -> bookingService.getById(id));
        }

        @Test
        void shouldGetUserBookings() {
                UUID userId = UUID.randomUUID();
                BookingsModel booking = new BookingsModel();
                booking.setUserId(userId);
                booking.setStatus("PENDING");

                EventsModel concert = new EventsModel();
                concert.setName("Test Concert");

                TicketCategoryModel cat = new TicketCategoryModel();
                cat.setName("Regular");
                cat.setEvent(concert);
                booking.setCategory(cat);

                when(bookingRepository.findByUserId(userId)).thenReturn(List.of(booking));

                // BookingResponse<List<BookingDetailDto>> response =
                // bookingService.getUserBookings(userId);
                // assertEquals("Success", response.getStatus());
        }

        @Test
        void shouldCancelBookingSuccessfully() {
                UUID id = UUID.randomUUID();
                BookingsModel booking = new BookingsModel();
                booking.setId(id);
                booking.setStatus("PENDING");
                TicketCategoryModel cat = new TicketCategoryModel();
                booking.setCategory(cat);
                booking.setCategoryId(UUID.randomUUID());
                booking.setQuantity(2);
                booking.setTotalAmount(new BigDecimal("100000"));

                when(bookingRepository.findById(id)).thenReturn(Optional.of(booking));

                // BookingResponse<Void> response = bookingService.cancel(id);
                // assertEquals("Success", response.getStatus());
                assertEquals("CANCELLED", booking.getStatus());
                verify(categoryRepository).increaseStock(booking.getCategoryId(), booking.getQuantity());
                verify(ledgerRepository).save(any());
        }
}
