package com.example.maths.web;

import com.example.maths.service.LessonService;
import com.example.maths.web.dto.LessonView;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @GetMapping("/api/lesson")
    public LessonView lesson(@RequestParam String topic,
                             @RequestParam(defaultValue = "en") String lang,
                             @RequestParam(required = false) Long learnerId) {
        return lessonService.lesson(topic, lang, learnerId);
    }
}
