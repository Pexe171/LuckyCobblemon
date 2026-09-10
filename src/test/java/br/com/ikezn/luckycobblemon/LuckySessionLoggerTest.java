package br.com.ikezn.luckycobblemon;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LuckySessionLoggerTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void writesReadableSessionAndRollInformation() throws Exception {
        LuckySessionLogger.initialize(temporaryDirectory, "test", true, 20);
        long rollId = LuckySessionLogger.recordRoll(
            "Tester", "uuid", "minecraft:overworld", 10, 64, -4,
            "luckycobblemon:rare_lucky_block", 35, "rare"
        );
        LuckySessionLogger.recordResult(rollId, "pokemon_spawn", "eevee level=25 shiny=false", true);

        String log = Files.readString(LuckySessionLogger.currentFile());
        assertTrue(log.contains("SESSION_START"));
        assertTrue(log.contains("category=\"rare\""));
        assertTrue(log.contains("block=\"luckycobblemon:rare_lucky_block\""));
        assertTrue(log.contains("event=\"pokemon_spawn\""));
        assertTrue(log.contains("success=\"true\""));
    }

    @Test
    void escapesValuesThatCouldBreakOneEntryPerLine() {
        String line = LuckySessionLogger.format("TEST", Map.of("detail", "line one\n\"line two\""));
        assertTrue(line.contains("detail=\"line one\\n\\\"line two\\\"\""));
        assertFalse(line.contains(System.lineSeparator()));
    }

    @Test
    void keepsOnlyConfiguredNumberOfSessionLogs() throws Exception {
        Path logDirectory = temporaryDirectory.resolve("logs").resolve("luckycobblemon");
        Files.createDirectories(logDirectory);
        Files.writeString(logDirectory.resolve("session-2020-01-01_00-00-00-000.log"), "old");
        Files.writeString(logDirectory.resolve("session-2021-01-01_00-00-00-000.log"), "old");

        LuckySessionLogger.initialize(temporaryDirectory, "test", true, 2);

        try (var logs = Files.list(logDirectory)) {
            assertEquals(2, logs.filter(path -> path.getFileName().toString().endsWith(".log")).count());
        }
    }
}
