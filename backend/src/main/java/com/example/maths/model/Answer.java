package com.example.maths.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "answer")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "learner_id")
    private Learner learner;

    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id")
    private Question question;

    private boolean correct;

    protected Answer() {
    }

    public Answer(Learner learner, Question question, boolean correct) {
        this.learner = learner;
        this.question = question;
        this.correct = correct;
    }

    public Long getId() {
        return id;
    }

    public Learner getLearner() {
        return learner;
    }

    public Question getQuestion() {
        return question;
    }

    public boolean isCorrect() {
        return correct;
    }
}
