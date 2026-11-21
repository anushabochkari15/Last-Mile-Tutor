package com.example.maths.web;

import com.example.maths.model.Question;
import com.example.maths.repository.QuestionRepository;
import com.example.maths.web.dto.QuestionView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TopicsController {

    private final QuestionRepository questionRepository;

    public TopicsController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @GetMapping("/topics")
    public List<String> topics() {
        return questionRepository.findDistinctTopics();
    }

    @GetMapping("/questions")
    public List<QuestionView> questions(@RequestParam String topic) {
        return questionRepository.findByTopic(topic).stream()
                .map(TopicsController::toView)
                .toList();
    }

    private static QuestionView toView(Question q) {
        return new QuestionView(q.getId(), q.getTopic(), q.getText(), q.getOptions());
    }
}
