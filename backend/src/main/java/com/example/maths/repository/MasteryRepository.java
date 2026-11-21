package com.example.maths.repository;

import com.example.maths.model.Mastery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MasteryRepository extends JpaRepository<Mastery, Long> {

    Optional<Mastery> findByLearnerIdAndTopic(Long learnerId, String topic);

    List<Mastery> findByLearnerId(Long learnerId);
}
