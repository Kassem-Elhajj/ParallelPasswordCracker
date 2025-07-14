package cracker;

import java.util.concurrent.RecursiveTask;
import java.util.concurrent.ForkJoinPool;

//crack(0,10)
//├── fork -> crack(0,5)  ← runs on new thread
//└── compute -> crack(5,10) ← runs on main thread
//      ├── fork -> crack(5,7)     ← new thread (or queued)
//      └── compute -> crack(7,10) ← main thread again
//          ├── fork -> crack(7,8) ← ...
//        └── compute -> crack(8,10) ← main thread still

public class ParallelCracker extends RecursiveTask<String> {
    private final String[] prefixes;
    private final int startIdx;
    private final int endIdx;
    private final String targetHash;

    private static final int THRESHOLD = 2; // Tune this for performance

    public ParallelCracker(String[] prefixes, int startIdx, int endIdx, String targetHash) {
        this.prefixes = prefixes;
        this.startIdx = startIdx;
        this.endIdx = endIdx;
        this.targetHash = targetHash;
    }

    @Override
    protected String compute() {
        if (endIdx - startIdx <= THRESHOLD) {
            // Sequential scan over this prefix chunk
            for (int i = startIdx; i < endIdx; i++) {
                String prefix = prefixes[i];
                for (int j = 0; j <= 999999; j++) {
                    String candidate = prefix + String.format("%06d", j);
                    if (HashUtil.sha256(candidate).equals(targetHash)) {
                        return candidate;
                    }
                }
            }
            return null;
        } else {
            // Split the task
            int mid = (startIdx + endIdx) / 2;
            ParallelCracker left = new ParallelCracker(prefixes, startIdx, mid, targetHash);
            ParallelCracker right = new ParallelCracker(prefixes, mid, endIdx, targetHash);
            left.fork();
            String rightResult = right.compute();
            String leftResult = left.join();

            
            return leftResult != null ? leftResult : rightResult;
        }
    }

    public static String crackPassword(String[] prefixes, String targetHash) {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        ParallelCracker task = new ParallelCracker(prefixes, 0, prefixes.length, targetHash);
        return pool.invoke(task);
    }
}


