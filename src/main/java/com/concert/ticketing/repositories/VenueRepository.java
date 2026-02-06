package com.concert.ticketing.repositories;

import com.concert.ticketing.model.VenuesModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VenueRepository extends JpaRepository<VenuesModel, UUID> {

    Optional<VenuesModel> findByNameIgnoreCase(String name);
}
