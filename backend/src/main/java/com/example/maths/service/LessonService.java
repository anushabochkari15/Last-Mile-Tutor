package com.example.maths.service;

import com.example.maths.model.Question;
import com.example.maths.repository.QuestionRepository;
import com.example.maths.web.dto.LessonView;
import com.example.maths.web.dto.TopicProgress;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LessonService {

    private static final String BASE_SYSTEM_PROMPT =
            "You are a patient maths tutor. Explain the topic clearly in %s. "
                    + "Give exactly one worked example. Use plain text, at most 200 words.";
    private static final String SIMPLE_SUFFIX =
            " Use very simple words, short sentences, and a concrete everyday example.";

    private final LlmClient llmClient;
    private final LearningService learningService;
    private final QuestionRepository questionRepository;

    public LessonService(LlmClient llmClient,
                         LearningService learningService,
                         QuestionRepository questionRepository) {
        this.llmClient = llmClient;
        this.learningService = learningService;
        this.questionRepository = questionRepository;
    }

    public LessonView lesson(String topic, String lang, Long learnerId) {
        String languageName = switch (lang == null ? "" : lang) {
            case "en" -> "English";
            case "te" -> "Telugu";
            case "hi" -> "Hindi";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        };

        List<Question> questions = questionRepository.findByTopic(topic).stream()
                .sorted(Comparator.comparing(Question::getId))
                .collect(Collectors.toList());
        if (questions.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        boolean simple = false;
        if (learnerId != null) {
            Optional<TopicProgress> entry = learningService.progress(learnerId).topics().stream()
                    .filter(t -> t.topic().equals(topic))
                    .findFirst();
            if (entry.isPresent() && entry.get().attempts() > 0 && entry.get().scorePercent() < 50) {
                simple = true;
            }
        }

        String system = String.format(BASE_SYSTEM_PROMPT, languageName);
        if (simple) {
            system = system + SIMPLE_SUFFIX;
        }
        String user = "Topic: " + topic + ". Explain the core idea and include one worked example.";

        Optional<String> generated = llmClient.complete(system, user);
        if (generated.isPresent()) {
            return new LessonView(topic, lang, simple, "llm", generated.get());
        }

        String stored = questions.stream()
                .map(Question::getExplanation)
                .filter(e -> e != null && !e.isBlank())
                .collect(Collectors.joining("\n\n"));
        return new LessonView(topic, lang, simple, "stored", stored);
    }
}
