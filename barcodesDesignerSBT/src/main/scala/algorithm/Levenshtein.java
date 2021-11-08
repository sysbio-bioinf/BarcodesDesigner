package main.code.algorithm;

public class Levenshtein {

    /**
     * Calculates the levenshtein distance between two strings
     * @param a first string
     * @param b second string
     * @return levenshtein distance as int
     */
    static int levenshtein(String a, String b) {
        int[][] d = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    d[i][j] = j;
                } else if (j == 0) {
                    d[i][j] = i;
                } else {
                    int min = Integer.MAX_VALUE;
                    min = Math.min(min, d[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1));
                    min = Math.min(min, d[i - 1][j] + 1);
                    d[i][j] = Math.min(min, d[i][j - 1] + 1);
                }
            }
        }
        return d[a.length()][b.length()];
    }

}
