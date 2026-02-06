package com.concert.ticketing.services.ledger;

import com.concert.ticketing.constant.BookingStatus;
import com.concert.ticketing.dto.payment.PaymentRequest;
import com.concert.ticketing.model.BookingsModel;
import com.concert.ticketing.model.LedgerEntriesModel;
import com.concert.ticketing.model.PaymentModel;
import com.concert.ticketing.repositories.BookingRepository;
import com.concert.ticketing.repositories.LedgerRepository;
import com.concert.ticketing.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepo;
    private final BookingRepository bookingRepo;
    private final LedgerRepository ledgerRepo;

    @Transactional
    public void processSuccessfulPayment(PaymentRequest request) {
        BookingsModel booking = bookingRepo.findById(request.bookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (!BookingStatus.CONFIRMED.name().equals(booking.getStatus()) &&
                !BookingStatus.PENDING.name().equals(booking.getStatus())) {
            throw new IllegalStateException(
                    "Booking is not in a valid state for payment. Current status: " + booking.getStatus());
        }

        PaymentModel payment = new PaymentModel();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentMethod(request.method());
        payment.setGatewayTransactionId(request.gatewayTransactionId());
        payment.setPaidAt(LocalDateTime.now());
        paymentRepo.save(payment);

        booking.setStatus(BookingStatus.PAID.name());
        bookingRepo.save(booking);

        LedgerEntriesModel ledger = new LedgerEntriesModel();
        ledger.setBookingId(booking.getId());
        ledger.setConcertId(booking.getCategory().getEvent().getId());
        ledger.setAmount(booking.getTotalAmount());
        ledger.setType("CREDIT");
        ledger.setRecordedAt(LocalDateTime.now());
        ledgerRepo.save(ledger);
    }
}