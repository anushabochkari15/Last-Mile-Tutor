package com.example.maths.web;

import com.example.maths.service.LearningService;
import com.example.maths.web.dto.QuestionView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final LearningService learningService;

    public QuizController(LearningService learningService) {
        this.learningService = learningService;
    }

    @GetMapping("/diagnostic")
    public List<QuestionView> diagnostic() {
        return learningService.diagnosticQuiz();
    }
}
