package analysis;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class LogLevelCounter {

    public enum LogLevel {
        TRACE, DEBUG, INFO, WARN, ERROR
    }

    public static Map<LogLevel, Integer> countLogLevels(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);
        Map<LogLevel, Integer> counts = new EnumMap<>(LogLevel.class);

        for (String line : lines) {
            for (LogLevel level : LogLevel.values()) {
                if (line.contains(" " + level.name() + " ")) {
                    counts.merge(level, 1, Integer::sum);
                    break;
                }
            }
        }
        return counts;
    }
}
