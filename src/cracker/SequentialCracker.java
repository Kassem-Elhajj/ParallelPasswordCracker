package cracker;

import java.util.concurrent.ForkJoinPool;

public class SequentialCracker {

    public static String crackPassword(String[] prefixes, String targetHash) {
        //Sequential
        for (String prefix : prefixes) {
            for (int i = 0; i <= 999999; i++) {
                String candidate = prefix + String.format("%06d", i);
                if (HashUtil.sha256(candidate).equals(targetHash)) {
                    return candidate;
                }
            }
        }
        return "Password is not found";
    }
}
