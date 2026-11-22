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
import com.example.maths.web.dto.QuestionView;
import com.example.maths.web.dto.RecommendationView;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

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
    public ResponseEntity<Learner> create(@Valid @RequestBody LearnerRequest req) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        String name = req.name().trim();
        Optional<Learner> existing = learnerRepository.findByEmail(email);
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get());
        }
        Learner saved = learnerRepository.save(new Learner(name, email));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<Learner> list() {
        return learnerRepository.findAll();
    }

    @GetMapping("/by-email")
    public Learner byEmail(@RequestParam String email) {
        String normalised = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
        if (normalised.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please enter an email address");
        }
        return learnerRepository.findByEmail(normalised)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No learner found with that email"));
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

    @GetMapping("/{id}/recommendations")
    public List<RecommendationView> recommendations(@PathVariable Long id) {
        return learningService.recommendations(id);
    }

    @GetMapping("/{id}/practice")
    public List<QuestionView> practice(@PathVariable Long id, @RequestParam String topic) {
        return learningService.practiceQuestions(id, topic);
    }
}
