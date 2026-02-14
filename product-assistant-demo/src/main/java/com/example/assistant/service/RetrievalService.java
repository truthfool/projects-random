package com.example.assistant.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class RetrievalService {
    private static class Chunk {
        String id;
        String text;
        double[] vec;
    }

    private final List<Chunk> chunks = new ArrayList<>();

    public RetrievalService() {
        try {
            var res = new ClassPathResource("embeddings.json");
            if (res.exists()) {
                try (var r = new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8)) {
                    String json = new String(res.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    // very small and permissive JSON parse for expected structure
                    // expected: [{"id":"...","text":"...","vec":[...]}, ...]
                    // we avoid adding a dependency; for prod use Jackson
                    json = json.trim();
                    if (json.startsWith("[")) {
                        String[] items = json.substring(1, json.length()-1).split("\\},\\s*\\{");
                        for (String raw : items) {
                            String item = raw;
                            if (!item.startsWith("{")) item = "{"+item;
                            if (!item.endsWith("}")) item = item+"}";
                            Chunk c = new Chunk();
                            c.id = extract(item, "\"id\"\s*:\s*\"", "\"");
                            c.text = extract(item, "\"text\"\s*:\s*\"", "\"");
                            String vecStr = extract(item, "\"vec\"\s*:\s*\\[", "]");
                            if (vecStr != null && !vecStr.isBlank()) {
                                String[] parts = vecStr.split(",");
                                c.vec = new double[parts.length];
                                for (int i=0;i<parts.length;i++) c.vec[i] = Double.parseDouble(parts[i].trim());
                            } else {
                                c.vec = new double[0];
                            }
                            chunks.add(c);
                        }
                    }
                }
            }
        } catch (Exception ignored) {}
    }

    public List<String> topK(String question, int k) {
        if (chunks.isEmpty())
            return List.of();
        double[] q = embedNaive(question);
        List<Map.Entry<Integer, Double>> scored = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            double s = cosine(q, chunks.get(i).vec);
            scored.add(Map.entry(i, s));
        }
        scored.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        List<String> out = new ArrayList<>();
        for (int i = 0; i < Math.min(k, scored.size()); i++)
            out.add(chunks.get(scored.get(i).getKey()).text);
        return out;
    }

    private double[] embedNaive(String text) {
        // fallback embedding: bag-of-words hashed to 256 dims
        int dims = 256;
        double[] v = new double[dims];
        for (String t : text.toLowerCase(Locale.ROOT).split("\\W+")) {
            if (t.length() < 2)
                continue;
            int h = Math.abs(t.hashCode()) % dims;
            v[h] += 1.0;
        }
        double norm = 0.0;
        for (double x : v)
            norm += x * x;
        norm = Math.sqrt(norm);
        if (norm > 0)
            for (int i = 0; i < dims; i++)
                v[i] /= norm;
        return v;
    }

    private double cosine(double[] a, double[] b) {
        int n = Math.min(a.length, b.length);
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < n; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        if (na == 0 || nb == 0)
            return 0;
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    private String extract(String s, String prefixRegex, String suffix) {
        var p = java.util.regex.Pattern.compile(prefixRegex);
        var m = p.matcher(s);
        if (m.find()) {
            int start = m.end();
            int end = s.indexOf(suffix, start);
            if (end > start)
                return s.substring(start, end);
        }
        return null;
    }
}
