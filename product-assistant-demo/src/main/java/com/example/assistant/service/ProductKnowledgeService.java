package com.example.assistant.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ProductKnowledgeService {
    private final List<String> productDocs = new ArrayList<>();

    public ProductKnowledgeService() {
        productDocs.add("PDF Editor helps you upload, preview, edit, split and convert PDF files.");
        productDocs.add("To upload a PDF, go to the Upload tab and select files.");
        productDocs.add("Use the Edit PDF tab to annotate, highlight, and rearrange pages.");
        productDocs.add("The Split PDF tab lets you split large PDFs into smaller documents.");
        productDocs.add("The Convert tab allows converting PDFs to and from images.");
        productDocs.add("Navigation: Use the top tabs to switch between features.");
    }

    public boolean isProductQuestion(String question) {
        if (question == null)
            return false;
        String q = question.toLowerCase(Locale.ROOT);
        return q.contains("pdf editor")
                || q.contains("upload")
                || q.contains("edit pdf")
                || q.contains("split pdf")
                || q.contains("convert")
                || q.contains("navigate")
                || q.contains("how to")
                || q.contains("feature");
    }

    public String answerFromDocs(String question) {
        String q = question.toLowerCase(Locale.ROOT);
        String best = productDocs.get(0);
        int bestScore = -1;
        for (String doc : productDocs) {
            int score = overlapScore(q, doc.toLowerCase(Locale.ROOT));
            if (score > bestScore) {
                bestScore = score;
                best = doc;
            }
        }
        return best;
    }

    private int overlapScore(String a, String b) {
        int score = 0;
        for (String token : a.split("\\s+")) {
            if (token.length() < 3)
                continue;
            if (b.contains(token))
                score++;
        }
        return score;
    }
}
