package com.example.maths.web;

import com.example.maths.model.Learner;
import com.example.maths.repository.LearnerRepository;
import com.example.maths.repository.MasteryRepository;
import com.example.maths.service.LearningService;
import com.example.maths.web.dto.AnswerRequest;
import com.example.maths.web.dto.AnswerResult;
import com.example.maths.web.dto.LearnerRequest;
import com.example.maths.web.dto.MasteryView;
import com.example.maths.web.dto.ProgressView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/learners")
public class LearnerController {

    private final LearnerRepository learnerRepository;
    private final MasteryRepository masteryRepository;
    private final LearningService learningService;

    public LearnerController(LearnerRepository learnerRepository,
                             MasteryRepository masteryRepository,
                             LearningService learningService) {
        this.learnerRepository = learnerRepository;
        this.masteryRepository = masteryRepository;
        this.learningService = learningService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Learner create(@Valid @RequestBody LearnerRequest req) {
        return learnerRepository.save(new Learner(req.name(), req.email()));
    }

    @GetMapping
    public List<Learner> list() {
        return learnerRepository.findAll();
    }

    @GetMapping("/{id}/mastery")
    public List<MasteryView> mastery(@PathVariable Long id) {
        return masteryRepository.findByLearnerId(id).stream()
                .map(m -> new MasteryView(m.getTopic(), m.getAttempts(), m.getCorrect()))
                .toList();
    }

    @PostMapping("/{id}/answers")
    public AnswerResult answer(@PathVariable Long id, @Valid @RequestBody AnswerRequest req) {
        return learningService.submitAnswer(id, req);
    }

    @GetMapping("/{id}/progress")
    public ProgressView progress(@PathVariable Long id) {
        return learningService.progress(id);
    }
}
