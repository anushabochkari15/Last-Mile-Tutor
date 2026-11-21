package com.example.maths.repository;

import com.example.maths.model.Learner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearnerRepository extends JpaRepository<Learner, Long> {
    Optional<Learner> findByEmail(String email);
}
