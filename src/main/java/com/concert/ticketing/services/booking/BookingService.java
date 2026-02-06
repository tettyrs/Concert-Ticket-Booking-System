package com.concert.ticketing.services.booking;

import com.concert.ticketing.constant.BookingStatus;
import com.concert.ticketing.constant.ErrorList;
import com.concert.ticketing.constant.Origin;
import com.concert.ticketing.dto.booking.BookingDetailDto;
import com.concert.ticketing.dto.booking.BookingMessage;
import com.concert.ticketing.dto.booking.BookingResponse;
import com.concert.ticketing.exception.ServiceException;
import com.concert.ticketing.model.BookingsModel;
import com.concert.ticketing.model.LedgerEntriesModel;
import com.concert.ticketing.model.TicketCategoryModel;
import com.concert.ticketing.model.UsersModel;
import com.concert.ticketing.repositories.BookingRepository;
import com.concert.ticketing.repositories.LedgerRepository;
import com.concert.ticketing.repositories.TicketCategoryRepository;
import com.concert.ticketing.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;
    private final TicketCategoryRepository categoryRepository;
    private final LedgerRepository ledgerRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public BookingResponse<BookingDetailDto> getById(UUID id) {
        log.info("Fetching booking details for ID: {}", id);

        BookingsModel booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ServiceException(Origin.POSTGRE, ErrorList.DATA_NOT_FOUND));

        // Validate user ownership
        validateUserAccess(booking.getUserId());

        return new BookingResponse<>(
                "Success",
                "00",
                "Booking details retrieved Successfully",
                mapToDto(booking));
    }

    @Transactional(readOnly = true)
    public BookingResponse<List<BookingDetailDto>> getUserBookings(UUID userId) {
        log.info("Fetching all bookings for User ID: {}", userId);

        // Validate user ownership
        validateUserAccess(userId);

        List<BookingsModel> bookings = bookingRepository.findByUserId(userId);

        List<BookingDetailDto> bookingDtos = bookings.stream()
                .map(this::mapToDto)
                .toList();

        return new BookingResponse<>(
                "Success",
                "00",
                "User bookings retrieved Successfully",
                bookingDtos);
    }

    @Transactional
    public BookingResponse<Void> confirm(UUID id) {
        log.info("Confirming booking for ID: {}", id);
        BookingsModel booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ServiceException(Origin.POSTGRE, ErrorList.DATA_NOT_FOUND));

        if (!BookingStatus.PENDING.name().equals(booking.getStatus())) {
            throw new ServiceException(Origin.MICROSERVICE, ErrorList.FAILED_CONNECT_TO_BACKEND);
        }

        booking.setStatus(BookingStatus.CONFIRMED.name());
        bookingRepository.save(booking);
        log.info("Booking ID: {} is now CONFIRMED", id);

        return new BookingResponse<>("Success", "00", "Booking confirmed Successfully", null);
    }

    @Transactional
    public BookingResponse<Void> deliver(UUID id) {
        log.info("Marking booking ID: {} as DELIVERED", id);
        BookingsModel booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ServiceException(Origin.POSTGRE, ErrorList.DATA_NOT_FOUND));

        if (!"PAID".equals(booking.getStatus())) {
            throw new ServiceException(Origin.MICROSERVICE, ErrorList.FAILED_CONNECT_TO_BACKEND);
        }

        booking.setStatus(BookingStatus.DELIVERED.name());
        bookingRepository.save(booking);
        log.info("Booking ID: {} is now DELIVERED", id);

        return new BookingResponse<>("Success", "00", "Booking delivered Successfully", null);
    }

    @Transactional
    public BookingResponse<Void> cancel(UUID id) {
        log.info("Initiating cancellation for booking ID: {}", id);

        BookingsModel booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ServiceException(Origin.POSTGRE, ErrorList.DATA_NOT_FOUND));

        // Validate user ownership
        validateUserAccess(booking.getUserId());

        if ("CANCELLED".equals(booking.getStatus())
                || "DELIVERED".equals(booking.getStatus())) {
            throw new ServiceException(Origin.MICROSERVICE, ErrorList.FAILED_CONNECT_TO_BACKEND);
        }

        categoryRepository.increaseStock(booking.getCategoryId(), booking.getQuantity());

        booking.setStatus(BookingStatus.CANCELLED.name());
        bookingRepository.save(booking);

        LedgerEntriesModel refundLedger = new LedgerEntriesModel();
        refundLedger.setBookingId(booking.getId());
        refundLedger.setConcertId(booking.getCategory().getEvent().getId());
        refundLedger.setAmount(booking.getTotalAmount());
        refundLedger.setType("CREDIT");
        ledgerRepository.save(refundLedger);

        log.info("Booking ID: {} has been cancelled and stock returned", id);

        return new BookingResponse<>(
                "Success",
                "00",
                "Booking cancelled Successfully and inventory updated",
                null);
    }

    /**
     * Validates that the current authenticated user has access to the booking data.
     * Users can only access their own bookings, while ADMINs can access all
     * bookings.
     */
    private void validateUserAccess(UUID bookingUserId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        // Admin can access all bookings
        if (isAdmin) {
            log.debug("Admin user {} accessing booking", currentUsername);
            return;
        }

        // Regular user can only access their own bookings
        UsersModel currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new ServiceException(Origin.POSTGRE, ErrorList.USER_NOT_FOUND));

        if (!currentUser.getId().equals(bookingUserId)) {
            log.warn("User {} attempted to access booking owned by user {}", currentUser.getId(), bookingUserId);
            throw new ServiceException(Origin.MICROSERVICE, ErrorList.DATA_NOT_FOUND); // Using 44 for consistency
        }

        log.debug("User {} accessing their own booking", currentUsername);
    }

    private BookingDetailDto mapToDto(BookingsModel b) {
        return new BookingDetailDto(
                b.getId(),
                b.getCategory().getEvent().getName(),
                b.getCategory().getName(),
                b.getQuantity(),
                b.getTotalAmount(),
                b.getStatus(),
                b.getExpiresAt(),
                b.getIdempotencyKey());
    }

    @Transactional
    public void processInternalBooking(BookingMessage msg) {
        log.info("Processing internal booking. IdempotencyKey: {}, UserId: {}, CategoryId: {}",
                msg.idempotencyKey(), msg.request().userId(), msg.request().categoryId());

        if (bookingRepository.existsByIdempotencyKey(msg.idempotencyKey())) {
            log.warn("Duplicate booking detected. Skipping. IdempotencyKey: {}", msg.idempotencyKey());
            return;
        }

        int updated = categoryRepository.decreaseStock(msg.request().categoryId(), msg.request().quantity());

        if (updated > 0) {
            log.debug("Stock reserved Successfully. categoryId: {}, quantity: {}", msg.request().categoryId(),
                    msg.request().quantity());
            TicketCategoryModel category = categoryRepository.getReferenceById(msg.request().categoryId());

            BookingsModel booking = new BookingsModel();
            booking.setUserId(msg.request().userId());
            booking.setCategory(category);
            booking.setQuantity(msg.request().quantity());
            booking.setTotalAmount(category.getPrice().multiply(BigDecimal.valueOf(msg.request().quantity())));
            booking.setStatus(BookingStatus.PENDING.name());
            booking.setExpiresAt(LocalDateTime.now().plusMinutes(5));
            booking.setIdempotencyKey(msg.idempotencyKey());

            BookingsModel savedBooking = bookingRepository.save(booking);
            log.info("Booking created Successfully. BookingId: {}, UserId: {}", savedBooking.getId(),
                    savedBooking.getUserId());

            LedgerEntriesModel ledger = new LedgerEntriesModel();
            ledger.setBookingId(savedBooking.getId());
            ledger.setConcertId(category.getEvent().getId());
            ledger.setAmount(savedBooking.getTotalAmount());
            ledger.setType("DEBIT");

            ledgerRepository.save(ledger);
        } else {
            log.warn("Failed to reserve stock. Insufficient quantity. categoryId: {}", msg.request().categoryId());
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredReservations() {
        log.info("Running scheduled cleanup for expired bookings...");
        List<BookingsModel> expired = bookingRepository.findExpiredBookings(LocalDateTime.now());

        if (expired.isEmpty()) {
            log.debug("No expired bookings found.");
            return;
        }

        log.info("Found {} expired bookings. Processing cancellations...", expired.size());
        for (BookingsModel b : expired) {
            log.info("Cancelling expired booking. BookingId: {}, CategoryId: {}, Quantity: {}", b.getId(),
                    b.getCategoryId(), b.getQuantity());
            b.setStatus(BookingStatus.CANCELLED.name());
            categoryRepository.increaseStock(b.getCategoryId(), b.getQuantity());
            bookingRepository.save(b);
        }
        log.info("Expired booking cleanup completed.");
    }

    @Transactional
    public BookingResponse<Void> partialRefund(UUID id, BigDecimal refundAmount) {
        log.info("Initiating partial refund for booking ID: {}, amount: {}", id, refundAmount);

        BookingsModel booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ServiceException(Origin.POSTGRE, ErrorList.DATA_NOT_FOUND));

        if (!"PAID".equals(booking.getStatus())
                && !"DELIVERED".equals(booking.getStatus())) {
            throw new ServiceException(Origin.MICROSERVICE, ErrorList.FAILED_CONNECT_TO_BACKEND);
        }

        if (refundAmount.compareTo(booking.getTotalAmount()) > 0) {
            throw new ServiceException(Origin.MICROSERVICE, ErrorList.FAILED_CONNECT_TO_BACKEND);
        }

        LedgerEntriesModel refundLedger = new LedgerEntriesModel();
        refundLedger.setBookingId(booking.getId());
        refundLedger.setConcertId(booking.getCategory().getEvent().getId());
        refundLedger.setAmount(refundAmount);
        refundLedger.setType("CREDIT");
        ledgerRepository.save(refundLedger);

        log.info("Partial refund of {} recorded for booking ID: {}", refundAmount, id);

        return new BookingResponse<>("Success", "00", "Partial refund recorded Successfully", null);
    }
}