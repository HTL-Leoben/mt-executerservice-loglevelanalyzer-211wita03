package analysis;

import analysis.LogLevelCounter.LogLevel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;

public class LogAnalyzerTask implements Callable<Map<LogLevel, Integer>> {

    private final Path logFile;

    public LogAnalyzerTask(Path logFile) {
        this.logFile = logFile;
    }

    @Override
    public Map<LogLevel, Integer> call() throws Exception {
        return LogLevelCounter.countLogLevels(logFile);
    }
}
