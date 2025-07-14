package cracker;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    private static final int RUNS = 1;

    public static void main(String[] args) {
        String targetPassword = "84429446";  // Known password for test
        String targetHash = HashUtil.sha256(targetPassword);
        String[] prefixes = {"03", "70", "71", "76", "78", "79", "81", "82", "83", "84"};

        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        if (!bean.isThreadCpuTimeSupported()) {
            System.err.println("Thread CPU time is not supported on this JVM.");
            return;
        }
        bean.setThreadCpuTimeEnabled(true);

        // Helper lambdas to get memory and CPU usage
        Runtime runtime = Runtime.getRuntime();

        // Total CPU time across all threads
        java.util.function.Supplier<Long> getCpuTime = () -> {
            long[] threadIds = bean.getAllThreadIds();
            long totalCpu = 0;
            for (long id : threadIds) {
                long time = bean.getThreadCpuTime(id);
                if (time != -1) totalCpu += time;
            }
            return totalCpu;
        };

        // Get used memory in MB
        java.util.function.Supplier<Double> getUsedMemoryMB = () -> (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0);

        // --- Sequential benchmark ---
        long seqTotalTime = 0;
        long seqCpuTotal = 0;
        double seqMemTotal = 0;
        String seqResult = null;

        for (int i = 0; i < RUNS; i++) {
            System.gc();
            long memBefore = (runtime.totalMemory() - runtime.freeMemory());
            long cpuBefore = getCpuTime.get();
            long start = System.nanoTime();

            seqResult = SequentialCracker.crackPassword(prefixes, targetHash);

            long end = System.nanoTime();
            long cpuAfter = getCpuTime.get();
            long memAfter = (runtime.totalMemory() - runtime.freeMemory());

            seqTotalTime += (end - start);
            seqCpuTotal += (cpuAfter - cpuBefore);
            seqMemTotal += (memAfter - memBefore) / (1024.0 * 1024.0); // bytes to MB
        }

        double seqAvgTime = seqTotalTime / (RUNS * 1e9);
        double seqAvgCPU = seqCpuTotal / (RUNS * 1e9);
        double seqAvgMem = seqMemTotal / RUNS;

        // --- Recursive Parallel benchmark ---
        long recParTotalTime = 0;
        long recParCpuTotal = 0;
        double recParMemTotal = 0;
        String recParResult = null;

        for (int i = 0; i < RUNS; i++) {
            System.gc();
            long memBefore = (runtime.totalMemory() - runtime.freeMemory());
            long cpuBefore = getCpuTime.get();
            long start = System.nanoTime();

            recParResult = ParallelCracker.crackPassword(prefixes, targetHash);

            long end = System.nanoTime();
            long cpuAfter = getCpuTime.get();
            long memAfter = (runtime.totalMemory() - runtime.freeMemory());

            recParTotalTime += (end - start);
            recParCpuTotal += (cpuAfter - cpuBefore);
            recParMemTotal += (memAfter - memBefore) / (1024.0 * 1024.0);
        }

        double recParAvgTime = recParTotalTime / (RUNS * 1e9);
        double recParAvgCPU = recParCpuTotal / (RUNS * 1e9);
        double recParAvgMem = recParMemTotal / RUNS;  //Highest because of overhead

        // --- Full Parallel benchmark ---
        long fullParTotalTime = 0;
        long fullParCpuTotal = 0;
        double fullParMemTotal = 0;
        String fullParResult = null;

        for (int i = 0; i < RUNS; i++) {
            System.gc();
            long memBefore = (runtime.totalMemory() - runtime.freeMemory());
            long cpuBefore = getCpuTime.get();
            long start = System.nanoTime();

            fullParResult = FullParallelCracker.crackPassword(prefixes, targetHash);

            long end = System.nanoTime();
            long cpuAfter = getCpuTime.get();
            long memAfter = (runtime.totalMemory() - runtime.freeMemory());

            fullParTotalTime += (end - start);
            fullParCpuTotal += (cpuAfter - cpuBefore);
            fullParMemTotal += (memAfter - memBefore) / (1024.0 * 1024.0);
        }

        double fullParAvgTime = fullParTotalTime / (RUNS * 1e9);
        double fullParAvgCPU = fullParCpuTotal / (RUNS * 1e9);
        double fullParAvgMem = fullParMemTotal / RUNS;

        // Threads used by full parallel
        int fullParThreads = FullParallelCracker.getThreadCount();

        // Speed-up calculations
        double recSpeedup = seqAvgTime > 0 ? seqAvgTime / recParAvgTime : 0;
        double fullSpeedup = seqAvgTime > 0 ? seqAvgTime / fullParAvgTime : 0;

        // Timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Print results
        System.out.println("=== Sequential Cracker ===");
        System.out.printf("Password found: %s%nAverage time: %.3f s | Memory: %.2f MB | CPU: %.3f s%n%n", seqResult, seqAvgTime, seqAvgMem, seqAvgCPU);

        System.out.println("=== Recursive Parallel Cracker ===");
        System.out.printf("Password found: %s%nAverage time: %.3f s | Memory: %.2f MB | CPU: %.3f s%n%n", recParResult, recParAvgTime, recParAvgMem, recParAvgCPU);

        System.out.println("=== Full Parallel Cracker ===");
        System.out.printf("Password found: %s%nAverage time: %.3f s | Memory: %.2f MB | CPU: %.3f s | Threads: %d%n%n", fullParResult, fullParAvgTime, fullParAvgMem, fullParAvgCPU, fullParThreads);

        System.out.printf("Speed-up: Recursive=%.2fx | Full Parallel=%.2fx%n", recSpeedup, fullSpeedup);

        // Generate PDF report
        PDFReportGenerator.generate(
                timestamp,
                seqResult, seqAvgTime, seqAvgMem, seqAvgCPU,
                recParResult, recParAvgTime, recParAvgMem, recParAvgCPU, recSpeedup,
                fullParResult, fullParAvgTime, fullParAvgMem, fullParAvgCPU, fullSpeedup,
                fullParThreads
        );

        System.out.println("📄 PDF report generated as CrackReport.pdf");
    }
}
