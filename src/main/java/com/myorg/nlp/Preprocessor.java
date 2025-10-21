package com.myorg.nlp;

import safar.basic.morphology.stemmer.impl.Light10Stemmer;
import safar.basic.morphology.stemmer.interfaces.IStemmer;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

public class Preprocessor {

    private Set<String> stopWords;
    private static final IStemmer stemmer = new Light10Stemmer();

    public Preprocessor(Set<String> stopWords) {
        this.stopWords = stopWords;
    }

    public List<String> process(String text) {
        // Normalize text
        text = normalize(text);
        // Split into tokens
        String[] tokens = text.split("\\s+");
        List<String> result = new ArrayList<>();

        for (String token : tokens) {
            if (!stopWords.contains(token) && !token.isBlank()) {
                String stem = stemWithSafar(token);
                result.add(stem);
            }
        }
        return result;
    }

    private String normalize(String text) {
        // Basic Arabic normalization
        return text.replaceAll("[^\\p{IsArabic}\\s]", "")
                   .replaceAll("[ًٌٍَُِّْ]", "")
                   .replace("أ", "ا").replace("إ", "ا").replace("آ", "ا")
                   .replace("ة", "ه").replace("ى", "ي");
    }

    private String stemWithSafar(String token) {
        try {
            List<?> results = stemmer.stem(token);
            return results.isEmpty() ? token : results.get(0).toString();
        } catch (Exception e) {
            System.err.println("Error stemming token '" + token + "': " + e.getMessage());
            return token;
        }
    }
}

