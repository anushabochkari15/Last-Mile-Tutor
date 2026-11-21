package com.example.maths.service;

import com.example.maths.model.Question;
import com.example.maths.repository.QuestionRepository;
import com.example.maths.web.dto.HintView;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class HintService {

    private static final String SYSTEM_PROMPT =
            "You are a maths tutor giving a hint. Never state the final answer, never name which "
                    + "option is correct, and never list the options. Give one short nudge in at most 40 words.";

    private final LlmClient llmClient;
    private final QuestionRepository questionRepository;

    public HintService(LlmClient llmClient, QuestionRepository questionRepository) {
        this.llmClient = llmClient;
        this.questionRepository = questionRepository;
    }

    public HintView hint(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        String userPrompt = "Question: " + question.getText()
                + " Topic: " + question.getTopic() + ". Give a hint only.";

        Optional<String> generated = llmClient.complete(SYSTEM_PROMPT, userPrompt);
        if (generated.isPresent() && !generated.get().isBlank()) {
            return new HintView(questionId, generated.get(), "llm");
        }
        return new HintView(questionId, question.getHint(), "stored");
    }
}
