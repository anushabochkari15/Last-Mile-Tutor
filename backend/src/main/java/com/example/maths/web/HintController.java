package com.example.maths.web;

import com.example.maths.service.HintService;
import com.example.maths.web.dto.HintView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HintController {

    private final HintService hintService;

    public HintController(HintService hintService) {
        this.hintService = hintService;
    }

    @GetMapping("/api/hint/{questionId}")
    public HintView hint(@PathVariable Long questionId) {
        return hintService.hint(questionId);
    }
}
