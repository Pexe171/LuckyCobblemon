package br.com.ikezn.luckycobblemon;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public final class LuckySessionLogger {
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter
        .ofPattern("yyyy-MM-dd_HH-mm-ss-SSS")
        .withZone(ZoneOffset.UTC);
    private static final AtomicLong NEXT_ROLL_ID = new AtomicLong();

    private static volatile boolean enabled;
    private static volatile Path gameDirectory;
    private static volatile String version;
    private static volatile Path logDirectory;
    private static volatile Path currentFile;

    private LuckySessionLogger() {
    }

    public static synchronized void initialize(Path gameDirectory, String version, boolean loggingEnabled, int maxLogFiles) {
        LuckySessionLogger.gameDirectory = gameDirectory;
        LuckySessionLogger.version = version;
        enabled = loggingEnabled;
        logDirectory = gameDirectory.resolve("logs").resolve("luckycobblemon");
        currentFile = null;
        if (!enabled) {
            return;
        }

        try {
            Files.createDirectories(logDirectory);
            String timestamp = FILE_TIME.format(Instant.now());
            currentFile = uniqueSessionFile(timestamp);
            append(format("SESSION_START", Map.of(
                "version", version,
                "format", "1",
                "note", "Attach this file when reporting a Lucky Cobblemon playtest"
            )));
            removeOldLogs(maxLogFiles);
        } catch (IOException error) {
            enabled = false;
            currentFile = null;
            LuckyCobblemonMod.LOGGER.error("Could not initialize Lucky Cobblemon session logging", error);
        }
    }

    public static synchronized void configure(boolean loggingEnabled, int maxLogFiles) {
        if (!loggingEnabled) {
            if (enabled && currentFile != null) {
                try {
                    append(format("LOGGING_DISABLED", Map.of()));
                } catch (IOException error) {
                    LuckyCobblemonMod.LOGGER.warn("Could not finish Lucky Cobblemon session logging", error);
                }
            }
            enabled = false;
            return;
        }
        if (currentFile == null && gameDirectory != null) {
            initialize(gameDirectory, version, true, maxLogFiles);
            return;
        }

        enabled = currentFile != null;
        if (enabled) {
            try {
                removeOldLogs(maxLogFiles);
                append(format("CONFIG_RELOADED", Map.of("eventLogging", true, "maxLogFiles", maxLogFiles)));
            } catch (IOException error) {
                LuckyCobblemonMod.LOGGER.warn("Could not update Lucky Cobblemon session logging", error);
            }
        }
    }

    public static synchronized void shutdown() {
        if (enabled && currentFile != null) {
            try {
                append(format("SESSION_END", Map.of()));
            } catch (IOException error) {
                LuckyCobblemonMod.LOGGER.warn("Could not finish Lucky Cobblemon session logging", error);
            }
        }
        enabled = false;
        currentFile = null;
    }

    public static long recordRoll(
        String player,
        String playerUuid,
        String dimension,
        int x,
        int y,
        int z,
        String block,
        int luck,
        String category
    ) {
        long rollId = NEXT_ROLL_ID.incrementAndGet();
        record("ROLL", Map.of(
            "id", rollId,
            "player", player,
            "playerUuid", playerUuid,
            "dimension", dimension,
            "position", x + "," + y + "," + z,
            "block", block,
            "luck", luck,
            "category", category
        ));
        return rollId;
    }

    public static void recordResult(long rollId, String event, String detail, boolean success) {
        record("RESULT", Map.of(
            "id", rollId,
            "event", event,
            "detail", detail,
            "success", success
        ));
    }

    public static Path currentFile() {
        return currentFile;
    }

    static String format(String type, Map<String, ?> fields) {
        StringBuilder line = new StringBuilder()
            .append('[').append(Instant.now()).append("] ")
            .append(type);
        fields.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> line.append(' ')
                .append(entry.getKey())
                .append("=\"")
                .append(escape(String.valueOf(entry.getValue())))
                .append('"'));
        return line.toString();
    }

    private static synchronized void record(String type, Map<String, ?> fields) {
        if (!enabled || currentFile == null) {
            return;
        }
        try {
            append(format(type, fields));
        } catch (IOException error) {
            LuckyCobblemonMod.LOGGER.warn("Could not append to Lucky Cobblemon session log {}", currentFile, error);
        }
    }

    private static void append(String line) throws IOException {
        Files.writeString(
            currentFile,
            line + System.lineSeparator(),
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        );
    }

    private static Path uniqueSessionFile(String timestamp) {
        Path candidate = logDirectory.resolve("session-" + timestamp + ".log");
        int suffix = 2;
        while (Files.exists(candidate)) {
            candidate = logDirectory.resolve("session-" + timestamp + "-" + suffix++ + ".log");
        }
        return candidate;
    }

    private static void removeOldLogs(int maxLogFiles) throws IOException {
        try (Stream<Path> paths = Files.list(logDirectory)) {
            Path[] logs = paths
                .filter(path -> path.getFileName().toString().startsWith("session-"))
                .filter(path -> path.getFileName().toString().endsWith(".log"))
                .sorted(Comparator.comparing((Path path) -> path.getFileName().toString()).reversed())
                .toArray(Path[]::new);
            for (int index = maxLogFiles; index < logs.length; index++) {
                Files.deleteIfExists(logs[index]);
            }
        }
    }

    private static String escape(String value) {
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n");
    }
}
