package com.example.assistant.controller;

import com.example.assistant.model.AskRequest;
import com.example.assistant.model.AskResponse;
import com.example.assistant.service.AnswerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AskController {
    private final AnswerService answerService;

    public AskController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @PostMapping("/ask")
    public ResponseEntity<AskResponse> ask(@Valid @RequestBody AskRequest request) {
        String question = request.getQuestion();
        if (!answerService.isAllowed(question)) {
            return ResponseEntity
                    .ok(new AskResponse(false, null, "Question not allowed. Ask only about product functionality."));
        }
        String answer = answerService.answer(question);
        return ResponseEntity.ok(new AskResponse(true, answer, null));
    }
}
