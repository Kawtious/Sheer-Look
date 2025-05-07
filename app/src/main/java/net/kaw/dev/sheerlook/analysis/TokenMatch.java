package net.kaw.dev.sheerlook.analysis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TokenMatch {
    public static double match(String a, String b) {
        Set<String> setA = new HashSet<>(Arrays.asList(a.toLowerCase().split("\\W+")));
        Set<String> setB = new HashSet<>(Arrays.asList(b.toLowerCase().split("\\W+")));

        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);

        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }
}
