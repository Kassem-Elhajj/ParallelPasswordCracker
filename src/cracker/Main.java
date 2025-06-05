package cracker;

public class Main {
    private static final int RUNS = 5;

    public static void main(String[] args) {
        String targetPassword = "84429446";  // Known password for test
        String targetHash = HashUtil.sha256(targetPassword);
        String[] prefixes = {"03", "70", "71", "76", "78", "79", "81", "82", "83", "84"};

        // === Sequential benchmark
        long seqTotalTime = 0;
        String seqResult = null;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            seqResult = SequentialCracker.crackPassword(prefixes, targetHash);
            long end = System.nanoTime();
            seqTotalTime += (end - start);
        }
        double seqAvgTime = seqTotalTime / (RUNS * 1e9);

        // === Recursive Parallel benchmark
        long recParTotalTime = 0;
        String recParResult = null;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            recParResult = ParallelCracker.crackPassword(prefixes, targetHash);
            long end = System.nanoTime();
            recParTotalTime += (end - start);
        }
        double recParAvgTime = recParTotalTime / (RUNS * 1e9);

        // === Full Parallel benchmark
        long fullParTotalTime = 0;
        String fullParResult = null;
        for (int i = 0; i < RUNS; i++) {
            long start = System.nanoTime();
            fullParResult = FullParallelCracker.crackPassword(prefixes, targetHash);
            long end = System.nanoTime();
            fullParTotalTime += (end - start);
        }
        double fullParAvgTime = fullParTotalTime / (RUNS * 1e9);

        // === Results
        System.out.println("=== Sequential Cracker ===");
        System.out.println("Password found: " + seqResult);
        System.out.printf("Average time over %d runs: %.3f seconds%n%n", RUNS, seqAvgTime);

        System.out.println("=== Recursive Parallel Cracker ===");
        System.out.println("Password found: " + recParResult);
        System.out.printf("Average time over %d runs: %.3f seconds%n%n", RUNS, recParAvgTime);

        System.out.println("=== Full Parallel Cracker ===");
        System.out.println("Password found: " + fullParResult);
        System.out.printf("Average time over %d runs: %.3f seconds%n", RUNS, fullParAvgTime);
        System.out.printf("Threads used: %d%n%n", FullParallelCracker.getThreadCount());

        // === Speed-up
        if (seqAvgTime > 0) {
            System.out.printf("Recursive Speed-up: %.2fx%n", seqAvgTime / recParAvgTime);
            System.out.printf("Full Parallel Speed-up: %.2fx%n", seqAvgTime / fullParAvgTime);
        }
    }
}
