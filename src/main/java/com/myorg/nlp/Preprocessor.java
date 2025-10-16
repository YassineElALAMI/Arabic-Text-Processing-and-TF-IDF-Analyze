package com.myorg.nlp;

import java.text.Normalizer;
import java.util.*;

public class Preprocessor {
    private final Set<String> stopWords;

    public Preprocessor(Set<String> stopWords) {
        this.stopWords = stopWords;
    }

    public List<String> process(String text) {
        text = normalizeArabic(text);
        text = text.toLowerCase();
        String[] raw = text.split("[^\\p{L}]+");
        List<String> tokens = new ArrayList<>();

        for (String t : raw) {
            if (t.isEmpty() || stopWords.contains(t)) continue;
            String stem = stemWithSafar(t);
            tokens.add(stem);
        }
        return tokens;
    }

    private String normalizeArabic(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFKC)
                .replaceAll("[إأآا]", "ا")
                .replaceAll("ى", "ي")
                .replaceAll("ؤ", "و")
                .replaceAll("ئ", "ي")
                .replaceAll("ً|ٌ|ٍ|َ|ُ|ِ|ّ|ْ", ""); // remove diacritics
    }

    private String stemWithSafar(String token) {
        // TODO: Replace with real SAFAR call, e.g. SafarStemmer.stem(token)
        return token;
    }
}
