package com.example.maths.service;

import com.example.maths.model.Learner;
import com.example.maths.model.Mastery;
import com.example.maths.model.Question;
import com.example.maths.repository.LearnerRepository;
import com.example.maths.repository.MasteryRepository;
import com.example.maths.repository.QuestionRepository;
import com.example.maths.web.dto.AnswerRequest;
import com.example.maths.web.dto.AnswerResult;
import com.example.maths.web.dto.ProgressView;
import com.example.maths.web.dto.QuestionView;
import com.example.maths.web.dto.TopicProgress;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class LearningService {

    private final LearnerRepository learnerRepository;
    private final QuestionRepository questionRepository;
    private final MasteryRepository masteryRepository;

    public LearningService(LearnerRepository learnerRepository,
                           QuestionRepository questionRepository,
                           MasteryRepository masteryRepository) {
        this.learnerRepository = learnerRepository;
        this.questionRepository = questionRepository;
        this.masteryRepository = masteryRepository;
    }

    private Learner requireLearner(Long id) {
        return learnerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public List<QuestionView> diagnosticQuiz() {
        List<QuestionView> all = new ArrayList<>();
        for (String topic : questionRepository.findDistinctTopics()) {
            List<Question> copy = new ArrayList<>(questionRepository.findByTopic(topic));
            Collections.shuffle(copy);
            copy.stream()
                    .limit(2)
                    .map(QuestionView::from)
                    .forEach(all::add);
        }
        return all;
    }

    @Transactional
    public AnswerResult submitAnswer(Long learnerId, AnswerRequest req) {
        Learner learner = requireLearner(learnerId);
        Question question = questionRepository.findById(req.questionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        boolean isCorrect = req.selectedIndex() == question.getCorrectIndex();

        Mastery mastery = masteryRepository.findByLearnerIdAndTopic(learnerId, question.getTopic())
                .orElseGet(() -> new Mastery(learner, question.getTopic()));
        mastery.recordAnswer(isCorrect);
        masteryRepository.save(mastery);

        LocalDate today = LocalDate.now();
        LocalDate lastActive = learner.getLastActiveDate();
        if (today.equals(lastActive)) {
            // already active today: streak unchanged
        } else if (today.minusDays(1).equals(lastActive)) {
            learner.setCurrentStreak(learner.getCurrentStreak() + 1);
        } else {
            learner.setCurrentStreak(1);
        }
        learner.setLastActiveDate(today);
        learnerRepository.save(learner);

        return new AnswerResult(isCorrect, question.getExplanation(), question.getHint());
    }

    @Transactional(readOnly = true)
    public ProgressView progress(Long learnerId) {
        Learner learner = requireLearner(learnerId);

        List<TopicProgress> topics = new ArrayList<>();
        int totalAttempts = 0;
        int totalCorrect = 0;
        for (String topic : questionRepository.findDistinctTopics()) {
            Mastery mastery = masteryRepository.findByLearnerIdAndTopic(learnerId, topic).orElse(null);
            int attempts = mastery == null ? 0 : mastery.getAttempts();
            int correct = mastery == null ? 0 : mastery.getCorrect();
            topics.add(new TopicProgress(topic, attempts, correct, scorePercent(attempts, correct)));
            totalAttempts += attempts;
            totalCorrect += correct;
        }

        String weakestTopic = topics.stream()
                .filter(t -> t.attempts() > 0)
                .min(Comparator.comparingInt(TopicProgress::scorePercent)
                        .thenComparing(TopicProgress::topic))
                .map(TopicProgress::topic)
                .orElse(null);

        return new ProgressView(topics, totalAttempts, totalCorrect,
                scorePercent(totalAttempts, totalCorrect),
                learner.getCurrentStreak(), weakestTopic);
    }

    private static int scorePercent(int attempts, int correct) {
        return attempts == 0 ? 0 : (int) Math.round(correct * 100.0 / attempts);
    }
}
