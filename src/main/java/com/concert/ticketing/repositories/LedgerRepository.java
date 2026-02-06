package com.concert.ticketing.repositories;

import com.concert.ticketing.model.LedgerEntriesModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LedgerRepository extends JpaRepository<LedgerEntriesModel, UUID> {
    List<LedgerEntriesModel> findByConcertId(UUID concertId);
}
