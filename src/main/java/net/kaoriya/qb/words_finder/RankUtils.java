package net.kaoriya.qb.words_finder;

import java.util.HashMap;

public class RankUtils {

    /** Get string rank. */
    public static int rank(String s) {
        if (s == null) {
            return 0;
        }
        int max = 0;
        HashMap<Character, Integer> m = new HashMap<>();
        for (int i = 0; i < s.length(); ++i) {
            char ch = s.charAt(i);
            Integer curr = m.get(ch);
            int next = curr != null ? curr + 1 : 1;
            m.put(ch, next);
            if (next > max) {
                max = next;
            }
        }
        return max;
    }
}
