package analysis;

import analysis.LogLevelCounter.LogLevel;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class LogAnalysisMain {

    public static void main(String[] args) throws Exception {
        String prefix = "app";
        Path currentDir = Paths.get(".");

        List<Path> logFiles;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(currentDir, prefix + "-*.log")) {
            logFiles = new ArrayList<>();
            stream.forEach(logFiles::add);
        }

        System.out.println("Gefundene Logdateien: " + logFiles.size());

        // --- Sequentiell ---
        Instant startSeq = Instant.now();
        Map<LogLevel, Integer> totalSeq = new EnumMap<>(LogLevel.class);

        for (Path path : logFiles) {
            Map<LogLevel, Integer> result = LogLevelCounter.countLogLevels(path);
            System.out.printf("Sequentiell (%s): %s%n", path.getFileName(), result);
            mergeCounts(totalSeq, result);
        }

        Instant endSeq = Instant.now();
        System.out.println("GESAMT (Sequentiell): " + totalSeq);
        System.out.println("Zeit (Sequentiell): " + Duration.between(startSeq, endSeq).toMillis() + " ms");

        // --- Parallel mit ExecutorService ---
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<Map<LogLevel, Integer>>> futures = new ArrayList<>();

        Instant startPar = Instant.now();

        for (Path path : logFiles) {
            futures.add(executor.submit(new LogAnalyzerTask(path)));
        }

        Map<LogLevel, Integer> totalPar = new EnumMap<>(LogLevel.class);
        for (int i = 0; i < futures.size(); i++) {
            Map<LogLevel, Integer> result = futures.get(i).get();
            System.out.printf("Parallel (%s): %s%n", logFiles.get(i).getFileName(), result);
            mergeCounts(totalPar, result);
        }

        Instant endPar = Instant.now();
        executor.shutdown();

        System.out.println("GESAMT (Parallel): " + totalPar);
        System.out.println("Zeit (Parallel): " + Duration.between(startPar, endPar).toMillis() + " ms");
    }

    private static void mergeCounts(Map<LogLevel, Integer> total, Map<LogLevel, Integer> toAdd) {
        for (Map.Entry<LogLevel, Integer> entry : toAdd.entrySet()) {
            total.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
    }
}
