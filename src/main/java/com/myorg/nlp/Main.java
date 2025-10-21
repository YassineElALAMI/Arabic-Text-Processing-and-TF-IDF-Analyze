package com.myorg.nlp;


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {

        // --- Base path for resources ---
        String base = "src/main/resources/";

        // --- Load stopwords ---
        Set<String> stopWords = StopWordsLoader.load(base + "stopwords.txt");
        Preprocessor pre = new Preprocessor(stopWords);

        // --- Load documents ---
        List<Path> docs = Files.list(Paths.get(base + "docs"))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        // --- Prepare output directory ---
        Path outputDir = Paths.get(base + "output");
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }

        // --- Store document term counts ---
        List<Map<String, Integer>> docCounts = new ArrayList<>();

        for (Path p : docs) {
            String text = Files.readString(p);
            List<String> tokens = pre.process(text);
            Map<String, Integer> counts = TfIdfCalculator.termCounts(tokens);
            docCounts.add(counts);
            System.out.println("Counts for " + p.getFileName() + " : " + counts.size() + " terms");
        }

        // --- Compute IDF for all terms ---
        Map<String, Double> idf = TfIdfCalculator.computeIdf(docCounts);

        // --- Global TF-IDF Matrix: document -> (term -> value) ---
        Map<String, Map<String, Float>> tfidfMatrix = new LinkedHashMap<>();

        // --- Process each document ---
        for (int i = 0; i < docCounts.size(); i++) {
            String docName = docs.get(i).getFileName().toString();
            Map<String, Double> tf = TfIdfCalculator.computeTf(docCounts.get(i));
            Map<String, Double> tfidf = TfIdfCalculator.computeTfIdf(tf, idf);

            // Store for matrix
            Map<String, Float> floatMap = new HashMap<>();
            for (Map.Entry<String, Double> e : tfidf.entrySet()) {
                floatMap.put(e.getKey(), e.getValue().floatValue());
            }
            tfidfMatrix.put(docName, floatMap);

            // Sort top 20 terms
            List<Map.Entry<String, Double>> topTerms = tfidf.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(20)
                    .toList();

            // --- Print to console ---
            System.out.println("\nTop TF-IDF for " + docName);
            for (Map.Entry<String, Double> e : topTerms) {
                System.out.printf("%-20s %.5f%n", e.getKey(), e.getValue());
            }

            // --- Write TF-IDF file ---
            Path outPath = outputDir.resolve(docName);
            try (BufferedWriter writer = Files.newBufferedWriter(outPath, StandardCharsets.UTF_8)) {
                writer.write("Top TF-IDF terms for " + docName + "\n\n");
                for (Map.Entry<String, Double> e : topTerms) {
                    writer.write(String.format("%-20s %.5f%n", e.getKey(), e.getValue()));
                }
            } catch (IOException e) {
                System.err.println("❌ Error writing TF-IDF file for " + docName + ": " + e.getMessage());
            }

            // --- Write Occurrence file ---
            Path occPath = outputDir.resolve("occurrences_" + docName);
            try (BufferedWriter occWriter = Files.newBufferedWriter(occPath, StandardCharsets.UTF_8)) {
                occWriter.write("Map d'occurrences pour " + docName + "\n\n");
                Map<String, Integer> counts = docCounts.get(i);
                for (Map.Entry<String, Integer> e : counts.entrySet()) {
                    occWriter.write(String.format("%-20s %d%n", e.getKey(), e.getValue()));
                }
            } catch (IOException e) {
                System.err.println("❌ Error writing occurrences file for " + docName + ": " + e.getMessage());
            }
        }

        // --- Write global TF-IDF matrix ---
        Path matrixPath = outputDir.resolve("matrix_tfidf.csv");

        Set<String> allTerms = new TreeSet<>();
        for (Map<String, Float> docMap : tfidfMatrix.values()) {
            allTerms.addAll(docMap.keySet());
        }

        try (BufferedWriter writer = Files.newBufferedWriter(matrixPath, StandardCharsets.UTF_8)) {
            // Header
            writer.write("term");
            for (String docName : tfidfMatrix.keySet()) {
                writer.write("," + docName);
            }
            writer.write("\n");

            // Each term row
            for (String term : allTerms) {
                writer.write(term);
                for (String docName : tfidfMatrix.keySet()) {
                    float value = tfidfMatrix.get(docName).getOrDefault(term, 0.0f);
                    writer.write("," + value);
                }
                writer.write("\n");
            }
            System.out.println("\n✅ TF-IDF matrix saved to: " + matrixPath);
        } catch (IOException e) {
            System.err.println("❌ Error writing TF-IDF matrix: " + e.getMessage());
        }
    }
}
