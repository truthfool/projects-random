package com.example.assistant.service;

import org.springframework.stereotype.Service;

@Service
public class GuardService {
    public boolean isAllowed(String question) {
        if (question == null || question.isBlank())
            return false;
        String q = question.toLowerCase();
        if (q.contains("password") || q.contains("secret") || q.contains("api key") || q.contains("license key")
                || q.contains("credit card"))
            return false;
        return q.contains("pdf editor") || q.contains("upload") || q.contains("edit") || q.contains("split")
                || q.contains("convert") || q.contains("navigate") || q.contains("feature");
    }
}
