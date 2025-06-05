package cracker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

//here each prefix run on a thread(faster)
//no work stealing like in ForkJoin(because it is unnecessary, all prefixes are independent and equal in computation)
//Each prefix is assigned a thread in contrast with ForkJoin where in each iteration only two threads are running(left and right)

public class FullParallelCracker {
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors() ;  //in my case 12, because I have 12 logical cores

    public static String crackPassword(String[] prefixes, String targetHash) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        List<Future<String>> futures = new ArrayList<>();

        for (String prefix : prefixes) {
            Callable<String> task = () -> {
                for (int i = 0; i <= 999999; i++) {
                    String candidate = prefix + String.format("%06d", i);
                    if (HashUtil.sha256(candidate).equals(targetHash)) {
                        return candidate;
                    }
                }
                return null;
            };
            futures.add(executor.submit(task));
        }

        String result = null;
        try {
            for (Future<String> future : futures) {
                try {
                    result = future.get();
                    if (result != null) {
                        break; // Found!
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } finally {
            executor.shutdownNow(); // Cancel remaining tasks
        }

        return result;
    }

    public static int getThreadCount() {
        return THREAD_COUNT;
    }
}
