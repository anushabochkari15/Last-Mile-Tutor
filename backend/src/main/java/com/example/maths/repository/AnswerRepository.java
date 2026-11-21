package com.example.maths.repository;

import com.example.maths.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    @Query("select distinct a.question.id from Answer a where a.learner.id = :learnerId and a.question.topic = :topic")
    List<Long> findAnsweredQuestionIds(@Param("learnerId") Long learnerId, @Param("topic") String topic);

    @Query("select a from Answer a where a.learner.id = :learnerId and a.question.topic = :topic order by a.id desc")
    List<Answer> findRecentForTopic(@Param("learnerId") Long learnerId, @Param("topic") String topic);
}
