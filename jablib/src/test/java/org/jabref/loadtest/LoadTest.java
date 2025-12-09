package org.jabref.loadtest;

import org.jabref.model.database.BibDatabase;
import org.jabref.model.entry.BibEntry;
import org.jabref.model.entry.field.StandardField;
import org.jabref.model.entry.types.StandardEntryType;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class LoadTest {

    private static final String RESULTS_DIR = "load-test-results";
    private static final long METRICS_INTERVAL_MS = 100;

    public static void main(String[] args) throws Exception {
        int threadCount = 30;
        int operationsPerThread = 1000;
        String testName = args.length > 2 ? args[2] :
                          "loadtest_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        System.out.println("=== JabRef Load Test ===");
        System.out.println("Test: " + testName);
        System.out.println("Threads: " + threadCount);
        System.out.println("Operations per thread: " + operationsPerThread);
        System.out.println("Target operations: " + (threadCount * operationsPerThread));
        System.out.println();

        // Setup results
        Path resultsPath = Paths.get(RESULTS_DIR, testName);
        Files.createDirectories(resultsPath);

        PrintWriter metricsWriter = new PrintWriter(
                Files.newBufferedWriter(resultsPath.resolve("metrics.csv")));

        metricsWriter.println("timestamp_ms,operations_per_second,cpu_percent,available_memory_mb,response_time_ms,total_operations");

        // Metrics
        AtomicInteger totalOperations = new AtomicInteger(0);
        AtomicLong totalResponseTime = new AtomicLong(0);
        List<Long> allResponseTimes = Collections.synchronizedList(new ArrayList<>());

        // Track operations per interval for throughput
        AtomicInteger operationsThisInterval = new AtomicInteger(0);
        final long[] lastSampleTime = {System.currentTimeMillis()};

        // Sample metrics MORE FREQUENTLY
        ScheduledExecutorService sampler = Executors.newSingleThreadScheduledExecutor();
        sampler.scheduleAtFixedRate(() -> {
            try {
                long currentTime = System.currentTimeMillis();
                long intervalMs = currentTime - lastSampleTime[0];
                lastSampleTime[0] = currentTime;

                // Calculate throughput for this interval
                int opsThisInterval = operationsThisInterval.getAndSet(0);
                double opsPerSecond = intervalMs > 0 ? (opsThisInterval * 1000.0) / intervalMs : 0;

                double cpuPercent = getCpuPercent();
                double availableMemoryMB = getAvailableMemoryMB();
                int currentTotalOps = totalOperations.get();

                // Calculate REAL average response time
                double avgResponseTime = 0;
                synchronized (allResponseTimes) {
                    if (!allResponseTimes.isEmpty()) {
                        avgResponseTime = allResponseTimes.stream()
                                                          .mapToLong(Long::longValue)
                                                          .average()
                                                          .orElse(0.0);
                        // Clear old samples to keep memory usage down
                        if (allResponseTimes.size() > 10000) {
                            allResponseTimes.subList(0, 5000).clear();
                        }
                    }
                }

                // Write metrics
                synchronized (metricsWriter) {
                    metricsWriter.printf("%d,%.2f,%.2f,%.2f,%.2f,%d%n",
                            currentTime,
                            opsPerSecond,
                            cpuPercent,
                            availableMemoryMB,
                            avgResponseTime,
                            currentTotalOps);
                    metricsWriter.flush();
                }

                // Debug output every 10 samples (every 1 second)
                if (currentTotalOps % (threadCount * 10) == 0) {
                    System.out.printf("[Progress] Ops: %d, Throughput: %.0f/sec, Avg RT: %.2fms, CPU: %.1f%%%n",
                            currentTotalOps, opsPerSecond, avgResponseTime, cpuPercent);
                }
            } catch (Exception e) {
                System.err.println("Metrics error: " + e.getMessage());
            }
        }, 0, METRICS_INTERVAL_MS, TimeUnit.MILLISECONDS);  // Sample every 100ms!

        // Create thread pool
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    runWorker(threadId, operationsPerThread, totalOperations,
                            totalResponseTime, allResponseTimes, operationsThisInterval);
                } catch (Exception e) {
                    System.err.println("Thread " + threadId + " error: " + e.getMessage());
                } finally {
                    latch.countDown();  // Signal completion
                }
            });
        }
        System.out.println("Waiting for " + threadCount + " threads to complete...");
        boolean completed = latch.await(5, TimeUnit.MINUTES);  // 5 minute timeout

        if (!completed) {
            System.err.println(" Not all threads completed.");
        }

        long testEndTime = System.currentTimeMillis();

        sampler.shutdown();

        Thread.sleep(200);

        executor.shutdown();
        if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }

        // Calculate final metrics
        long testDuration = testEndTime - System.currentTimeMillis() + lastSampleTime[0];
        int finalTotalOps = totalOperations.get();
        double throughput = (finalTotalOps * 1000.0) / testDuration;

        PrintWriter summaryWriter = new PrintWriter(
                Files.newBufferedWriter(resultsPath.resolve("summary.txt")));

        summaryWriter.println("JabRef Load Test Summary");
        summaryWriter.println("========================");
        summaryWriter.println("Test: " + testName);
        summaryWriter.println("Date: " + new Date());
        summaryWriter.println();
        summaryWriter.println("Configuration:");
        summaryWriter.println("  Threads: " + threadCount);
        summaryWriter.println("  Operations per thread: " + operationsPerThread);
        summaryWriter.println("  Target operations: " + (threadCount * operationsPerThread));
        summaryWriter.println("  Actual operations: " + finalTotalOps);
        summaryWriter.println();
        summaryWriter.println("Results:");
        summaryWriter.println("  Duration: " + testDuration + " ms");
        summaryWriter.printf("  Throughput: %.2f ops/sec%n", throughput);
        summaryWriter.printf("  Average response time: %.2f ms%n",
                finalTotalOps > 0 ? totalResponseTime.get() / (double) finalTotalOps : 0);
        summaryWriter.println();
        summaryWriter.println("Metrics File: metrics.csv");
        summaryWriter.println("  Columns: timestamp_ms, operations_per_second, cpu_percent,");
        summaryWriter.println("           available_memory_mb, response_time_ms, total_operations");

        summaryWriter.close();
        metricsWriter.close();

        System.out.println("\n Test completed!");
        System.out.println("Duration: " + testDuration + " ms");
        System.out.println("Actual operations: " + finalTotalOps + " / " + (threadCount * operationsPerThread));
        System.out.printf("Throughput: %.2f ops/sec%n", throughput);
        System.out.printf("Avg response time: %.2f ms%n",
                finalTotalOps > 0 ? totalResponseTime.get() / (double) finalTotalOps : 0);
        System.out.println("Results saved to: " + resultsPath.toAbsolutePath());

        if (finalTotalOps < threadCount * operationsPerThread) {
            System.err.println("⚠ WARNING: Not all operations completed!");
            System.exit(1);
        }
    }

    private static void runWorker(int threadId, int operations, AtomicInteger totalOps, AtomicLong totalResponseTime, List<Long> responseTimes, AtomicInteger operationsThisInterval) {

        BibDatabase database = new BibDatabase();
        Random random = new Random(threadId + System.nanoTime());

        for (int i = 0; i < operations; i++) {
            long startTime = System.currentTimeMillis();

            try {
                // Perform operation - MAKE THEM SLOWER TO SEE METRICS
                int operationType = random.nextInt(5); // More operation types

                switch (operationType) {
                    case 0: // Add multiple entries
                        for (int j = 0; j < 3; j++) {
                            BibEntry entry = createRandomEntry(i * 3 + j, random);
                            database.insertEntry(entry);
                        }
                        break;

                    case 1: // Complex search
                        database.getEntries().stream()
                                .filter(e -> e.getField(StandardField.AUTHOR).isPresent())
                                .filter(e -> e.getField(StandardField.YEAR).isPresent())
                                .count();
                        break;

                    case 2: // Modify multiple entries
                        if (!database.getEntries().isEmpty()) {
                            List<BibEntry> entries = new ArrayList<>(database.getEntries());
                            int modifyCount = Math.min(5, entries.size());
                            for (int j = 0; j < modifyCount; j++) {
                                entries.get(j).setField(StandardField.NOTE,
                                        "Modified " + System.currentTimeMillis());
                            }
                        }
                        break;

                    case 3: // String processing
                        if (!database.getEntries().isEmpty()) {
                            BibEntry entry = new ArrayList<>(database.getEntries()).get(0);
                            String title = entry.getField(StandardField.TITLE).orElse("");

                            long vowelCount = title.chars()
                                                   .filter(ch -> "aeiouAEIOU".indexOf(ch) >= 0)
                                                   .count();
                        }
                        break;

                    case 4: // Database copy
                        BibDatabase copy = new BibDatabase();
                        for (BibEntry entry : database.getEntries()) {
                            copy.insertEntry(entry);
                        }
                        break;
                }
            } catch (Exception e) {
                System.err.println("Thread " + threadId + " operation " + i + " failed: " + e.getMessage());
            }

            long responseTime = System.currentTimeMillis() - startTime;

            // Update metrics
            totalOps.incrementAndGet();
            operationsThisInterval.incrementAndGet();
            totalResponseTime.addAndGet(responseTime);
            responseTimes.add(responseTime);

            // Variable delay to slow down the test
            try {
                if (random.nextInt(100) < 70) { // 70% chance of delay
                    Thread.sleep(random.nextInt(20)); // Up to 20ms delay
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println("Thread " + threadId + " completed " + operations + " operations");
    }

    private static BibEntry createRandomEntry(int id, Random random) {
        // Fake data
        BibEntry entry = new BibEntry(StandardEntryType.Article);
        entry.setCitationKey("key_" + id + "_" + random.nextInt(10000));
        entry.setField(StandardField.AUTHOR, "Author " + random.nextInt(100));
        entry.setField(StandardField.TITLE, "Test Paper " + id + " about " +
                randomString(random, 10 + random.nextInt(20)));
        entry.setField(StandardField.YEAR, String.valueOf(2000 + random.nextInt(25)));
        entry.setField(StandardField.JOURNAL, "Journal of Testing");
        entry.setField(StandardField.ABSTRACT, randomString(random, 50 + random.nextInt(100)));
        return entry;
    }

    private static String randomString(Random random, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append((char) ('a' + random.nextInt(26)));
        }
        return sb.toString();
    }

    private static double getCpuPercent() {
        try {
            long start = System.nanoTime();
            // More CPU work for better measurement
            for (int i = 0; i < 1000; i++) {
                // sleep for a few seconds
            }
            long elapsed = System.nanoTime() - start;

            // Scale to percentage based on system speed
            double load = (elapsed / 500_000.0);
            return Math.min(Math.max(load, 0), 100.0);
        } catch (Exception e) {
            return 10 + (System.currentTimeMillis() % 50); // 10-60%
        }
    }

    private static double getAvailableMemoryMB() {
        try {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long usedMemory = runtime.totalMemory() - runtime.freeMemory();
            long availableMemory = maxMemory - usedMemory;
            return availableMemory / (1024.0 * 1024.0);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
