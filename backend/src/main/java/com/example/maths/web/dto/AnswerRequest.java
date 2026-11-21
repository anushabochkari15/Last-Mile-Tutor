package com.example.maths.web.dto;

import jakarta.validation.constraints.NotNull;

public record AnswerRequest(@NotNull Long questionId, @NotNull Integer selectedIndex) {
}
