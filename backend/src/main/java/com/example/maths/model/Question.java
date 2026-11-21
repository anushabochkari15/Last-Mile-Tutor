package com.example.maths.model;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "question")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String topic;

    @NotBlank
    @Column(length = 1000)
    private String text;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "question_option", joinColumns = @JoinColumn(name = "question_id"))
    @OrderColumn(name = "option_index")
    private List<String> options = new ArrayList<>();

    private int correctIndex;

    @Column(length = 1000)
    private String hint;

    @Column(length = 1000)
    private String explanation;

    protected Question() {
    }

    public Question(String topic, String text, List<String> options, int correctIndex,
                    String hint, String explanation) {
        this.topic = topic;
        this.text = text;
        this.options = options;
        this.correctIndex = correctIndex;
        this.hint = hint;
        this.explanation = explanation;
    }

    @PrePersist
    @PreUpdate
    void validateState() {
        if (options == null || options.size() != 4) {
            throw new IllegalStateException("Question must have exactly 4 options");
        }
        if (correctIndex < 0 || correctIndex > 3) {
            throw new IllegalStateException("correctIndex must be between 0 and 3 inclusive");
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public int getCorrectIndex() {
        return correctIndex;
    }

    public void setCorrectIndex(int correctIndex) {
        this.correctIndex = correctIndex;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
