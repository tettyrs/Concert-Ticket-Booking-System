package com.concert.ticketing.repositories;

import com.concert.ticketing.model.BookingsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<BookingsModel, UUID> {
    boolean existsByIdempotencyKey(String key);

    List<BookingsModel> findByUserId(UUID userId);

    @Query("SELECT b FROM BookingsModel b WHERE b.status = 'PENDING' AND b.expiresAt < :now")
    List<BookingsModel> findExpiredBookings(LocalDateTime now);
}