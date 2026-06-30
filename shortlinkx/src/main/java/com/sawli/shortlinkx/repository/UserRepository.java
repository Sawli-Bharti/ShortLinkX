package com.sawli.shortlinkx.repository;

import com.sawli.shortlinkx.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository interface for performing database operations
 * on the {@link User} entity.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by email address.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user already exists with the given email address.
     */
    boolean existsByEmail(String email);
}
