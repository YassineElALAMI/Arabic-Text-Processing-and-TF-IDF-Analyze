package com.myorg.nlp;

import java.util.*;

public class TfIdfCalculator {

    public static Map<String,Integer> termCounts(List<String> tokens) {
        Map<String,Integer> map = new HashMap<>();
        for (String t : tokens)
            map.put(t, map.getOrDefault(t,0)+1);
        return map;
    }

    public static Map<String,Double> computeTf(Map<String,Integer> counts) {
        double total = counts.values().stream().mapToInt(i->i).sum();
        Map<String,Double> tf = new HashMap<>();
        for (var e : counts.entrySet())
            tf.put(e.getKey(), e.getValue()/total);
        return tf;
    }

    public static Map<String,Double> computeIdf(List<Map<String,Integer>> docs) {
        int N = docs.size();
        Map<String,Integer> df = new HashMap<>();
        for (Map<String,Integer> doc : docs)
            for (String term : doc.keySet())
                df.put(term, df.getOrDefault(term,0)+1);

        Map<String,Double> idf = new HashMap<>();
        for (var e : df.entrySet())
            idf.put(e.getKey(), Math.log((double)(N+1)/(e.getValue()+1))+1);
        return idf;
    }

    public static Map<String,Double> computeTfIdf(Map<String,Double> tf, Map<String,Double> idf) {
        Map<String,Double> tfidf = new HashMap<>();
        for (var e : tf.entrySet())
            tfidf.put(e.getKey(), e.getValue()*idf.getOrDefault(e.getKey(),1.0));
        return tfidf;
    }
}
