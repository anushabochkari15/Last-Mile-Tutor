package com.example.maths.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LearnerRequest(@NotBlank String name, @NotBlank @Email String email) {
}
