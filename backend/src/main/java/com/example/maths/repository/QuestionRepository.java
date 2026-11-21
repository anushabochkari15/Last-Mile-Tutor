package com.example.maths.repository;

import com.example.maths.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByTopic(String topic);

    @Query("select distinct q.topic from Question q order by q.topic")
    List<String> findDistinctTopics();
}
