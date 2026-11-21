package com.example.maths.web.dto;

import java.util.List;

public record ProgressView(List<TopicProgress> topics, int overallAttempts, int overallCorrect, int overallScorePercent, int currentStreak, String weakestTopic) {}
