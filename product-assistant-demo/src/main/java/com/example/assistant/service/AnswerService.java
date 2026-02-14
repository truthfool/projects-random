package com.example.assistant.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnswerService {
    private final GuardService guard;
    private final RetrievalService retrieval;
    private final OllamaClient ollama;

    public AnswerService(GuardService guard, RetrievalService retrieval, OllamaClient ollama) {
        this.guard = guard;
        this.retrieval = retrieval;
        this.ollama = ollama;
    }

    public boolean isAllowed(String question) {
        return guard.isAllowed(question);
    }

    public String answer(String question) {
        List<String> ctx = retrieval.topK(question, 3);
        if (ctx.isEmpty())
            return "Not allowed. Ask only about product functionality.";
        String prompt = buildPrompt(ctx, question);
        String out = ollama.generate(prompt);
        if (out == null || out.isBlank()) {
            return "Not allowed. Ask only about product functionality.";
        }
        return out.trim();
    }

    private String buildPrompt(List<String> ctx, String question) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are ProductAssistant for a PDF Editor.\n");
        sb.append("Answer ONLY about the product's usage, navigation, and features using the context.\n");
        sb.append(
                "If out of scope or insufficient context, respond exactly: Not allowed. Ask only about product functionality.\n\n");
        sb.append("Context:\n");
        for (String c : ctx)
            sb.append("---\n").append(c).append("\n");
        sb.append("\nUser: ").append(question).append("\nAnswer: ");
        return sb.toString();
    }
}
