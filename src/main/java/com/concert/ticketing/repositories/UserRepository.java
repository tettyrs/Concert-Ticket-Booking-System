package com.concert.ticketing.repositories;

import com.concert.ticketing.model.UsersModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UsersModel, UUID> {
    Optional<UsersModel> findByUsername(String username);
}
