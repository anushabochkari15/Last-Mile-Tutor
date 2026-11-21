package com.example.maths.web.dto;

import com.example.maths.model.Question;

import java.util.List;

/** Question as exposed to learners: deliberately omits correctIndex. */
public record QuestionView(Long id, String topic, String text, List<String> options) {

    public static QuestionView from(Question q) {
        return new QuestionView(q.getId(), q.getTopic(), q.getText(), q.getOptions());
    }
}
