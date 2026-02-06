package com.concert.ticketing.repositories;

import com.concert.ticketing.model.TicketCategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface TicketCategoryRepository extends JpaRepository<TicketCategoryModel, UUID> {
    @Query("SELECT tc FROM TicketCategoryModel tc WHERE tc.event.id = :eventId")
    List<TicketCategoryModel> findByEventId(@Param("eventId") UUID eventId);

    @Modifying
    @Transactional
    @Query("""
                UPDATE TicketCategoryModel c
                SET c.availableStock = c.availableStock - :qty
                WHERE c.id = :id
                  AND c.availableStock >= :qty
            """)
    int decreaseStock(
            @Param("id") UUID id,
            @Param("qty") Integer qty);

    @Modifying
    @Transactional
    @Query("UPDATE TicketCategoryModel c SET c.availableStock = c.availableStock + :qty WHERE c.id = :id")
    int increaseStock(@Param("id") UUID id, @Param("qty") Integer qty);
}
