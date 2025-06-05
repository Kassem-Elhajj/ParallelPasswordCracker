package cracker;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    private static final int RUNS = 5;

    public static void main(String[] args) {
        String targetPassword = "84429446";  // Known password for test
        String targetHash = HashUtil.sha256(targetPassword);
        String[] prefixes = {"03", "70", "71", "76", "78", "79", "81", "82", "83", "84"};

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // === Sequential Cracker
        long seqTotalTime = 0;
        double seqMemTotal = 0;
        double seqCpuTotal = 0;
        String seqResult = null;

        for (int i = 0; i < RUNS; i++) {
            double memBefore = getUsedMemoryInMB();
            double cpuBefore = getCPUTimeSeconds();
            long start = System.nanoTime();

            seqResult = SequentialCracker.crackPassword(prefixes, targetHash);

            long end = System.nanoTime();
            double cpuAfter = getCPUTimeSeconds();
            double memAfter = getUsedMemoryInMB();

            seqTotalTime += (end - start);
            seqMemTotal += (memAfter - memBefore);
            seqCpuTotal += (cpuAfter - cpuBefore);
        }

        double seqAvgTime = seqTotalTime / (RUNS * 1e9);
        double seqAvgMem = seqMemTotal / RUNS;
        double seqAvgCPU = seqCpuTotal / RUNS;

        // === Recursive Parallel Cracker
        long recParTotalTime = 0;
        double recParMemTotal = 0;
        double recParCpuTotal = 0;
        String recParResult = null;

        for (int i = 0; i < RUNS; i++) {
            double memBefore = getUsedMemoryInMB();
            double cpuBefore = getCPUTimeSeconds();
            long start = System.nanoTime();

            recParResult = ParallelCracker.crackPassword(prefixes, targetHash);

            long end = System.nanoTime();
            double cpuAfter = getCPUTimeSeconds();
            double memAfter = getUsedMemoryInMB();

            recParTotalTime += (end - start);
            recParMemTotal += (memAfter - memBefore);
            recParCpuTotal += (cpuAfter - cpuBefore);
        }

        double recParAvgTime = recParTotalTime / (RUNS * 1e9);
        double recParAvgMem = recParMemTotal / RUNS;
        double recParAvgCPU = recParCpuTotal / RUNS;

        // === Full Parallel Cracker
        long fullParTotalTime = 0;
        double fullParMemTotal = 0;
        double fullParCpuTotal = 0;
        String fullParResult = null;

        for (int i = 0; i < RUNS; i++) {
            double memBefore = getUsedMemoryInMB();
            double cpuBefore = getCPUTimeSeconds();
            long start = System.nanoTime();

            fullParResult = FullParallelCracker.crackPassword(prefixes, targetHash);

            long end = System.nanoTime();
            double cpuAfter = getCPUTimeSeconds();
            double memAfter = getUsedMemoryInMB();

            fullParTotalTime += (end - start);
            fullParMemTotal += (memAfter - memBefore);
            fullParCpuTotal += (cpuAfter - cpuBefore);
        }

        double fullParAvgTime = fullParTotalTime / (RUNS * 1e9);
        double fullParAvgMem = fullParMemTotal / RUNS;
        double fullParAvgCPU = fullParCpuTotal / RUNS;

        // === Speed-up
        double recSpeedup = seqAvgTime / recParAvgTime;
        double fullSpeedup = seqAvgTime / fullParAvgTime;

        // === Console Output
        System.out.println("=== Sequential Cracker ===");
        System.out.println("Password found: " + seqResult);
        System.out.printf("Average time: %.3f s | Mem: %.2f MB | CPU: %.3f s%n%n", seqAvgTime, seqAvgMem, seqAvgCPU);

        System.out.println("=== Recursive Parallel Cracker ===");
        System.out.println("Password found: " + recParResult);
        System.out.printf("Average time: %.3f s | Mem: %.2f MB | CPU: %.3f s%n", recParAvgTime, recParAvgMem, recParAvgCPU);
        System.out.printf("Speed-up: %.2fx%n%n", recSpeedup);

        System.out.println("=== Full Parallel Cracker ===");
        System.out.println("Password found: " + fullParResult);
        System.out.printf("Average time: %.3f s | Mem: %.2f MB | CPU: %.3f s%n", fullParAvgTime, fullParAvgMem, fullParAvgCPU);
        System.out.printf("Speed-up: %.2fx | Threads: %d%n%n", fullSpeedup, FullParallelCracker.getThreadCount());

        // === PDF Report
        PDFReportGenerator.generate(
                timestamp,
                seqResult, seqAvgTime, seqAvgMem, seqAvgCPU,
                recParResult, recParAvgTime, recParAvgMem, recParAvgCPU, recSpeedup,
                fullParResult, fullParAvgTime, fullParAvgMem, fullParAvgCPU, fullSpeedup,
                FullParallelCracker.getThreadCount()
        );

        System.out.println("📄 PDF report generated as CrackReport.pdf");
    }

    // === Memory usage helper
    public static double getUsedMemoryInMB() {
        Runtime runtime = Runtime.getRuntime();
        return (runtime.totalMemory() - runtime.freeMemory()) / (1024.0 * 1024.0);
    }

    // === CPU time helper
    public static double getCPUTimeSeconds() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported()
                ? bean.getCurrentThreadCpuTime() / 1_000_000_000.0
                : 0.0;
    }
}
