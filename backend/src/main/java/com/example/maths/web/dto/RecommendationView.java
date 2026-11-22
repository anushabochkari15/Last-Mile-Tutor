package com.example.maths.web.dto;

public record RecommendationView(String topic, int scorePercent, int attempts,
                                 String lessonPath, String practicePath) {}
