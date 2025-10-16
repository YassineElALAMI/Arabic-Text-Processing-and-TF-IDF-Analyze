package com.myorg.nlp;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class StopWordsLoader {
    public static Set<String> load(String path) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(path));
        Set<String> stop = new HashSet<>();
        for (String l : lines) {
            String s = l.trim();
            if (!s.isEmpty()) stop.add(s);
        }
        return stop;
    }
}
