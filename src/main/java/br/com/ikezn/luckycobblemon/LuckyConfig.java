package br.com.ikezn.luckycobblemon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

public final class LuckyConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int MAX_WEIGHT = 1_000_000;

    public boolean allowCreativeActivation = false;
    public boolean eventLogging = true;
    public int maxLogFiles = 20;

    public int commonWeight = 32;
    public int uncommonWeight = 23;
    public int rareWeight = 16;
    public int itemBundleWeight = 10;
    public int epicWeight = 7;
    public int raidWeight = 4;
    public int trioWeight = 4;
    public int shrineWeight = 3;
    public int mythicalWeight = 2;
    public int unluckyWeight = 2;
    public int legendaryJackpotWeight = 1;

    public int commonMinLevel = 5;
    public int commonMaxLevel = 20;
    public int uncommonMinLevel = 15;
    public int uncommonMaxLevel = 35;
    public int rareMinLevel = 30;
    public int rareMaxLevel = 50;
    public int epicMinLevel = 45;
    public int epicMaxLevel = 65;
    public int mythicalMinLevel = 50;
    public int mythicalMaxLevel = 70;
    public int legendaryMinLevel = 60;
    public int legendaryMaxLevel = 80;

    public List<String> commonSpecies = List.of(
        "caterpie", "pidgey", "rattata", "zubat", "magikarp", "sentret",
        "zigzagoon", "bidoof", "patrat", "fletchling", "lechonk", "wooloo"
    );
    public List<String> uncommonSpecies = List.of(
        "pikachu", "eevee", "growlithe", "vulpix", "machop", "riolu",
        "ralts", "shinx", "rockruff", "tinkatink", "charcadet", "dreepy"
    );
    public List<String> rareSpecies = List.of(
        "dratini", "larvitar", "bagon", "beldum", "gible", "deino",
        "goomy", "jangmo-o", "duraludon", "frigibax", "rotom", "ditto"
    );
    public List<String> epicSpecies = List.of(
        "charizard", "lucario", "garchomp", "metagross", "tyranitar",
        "dragonite", "greninja", "gengar", "volcarona", "dragapult"
    );
    public List<String> mythicalSpecies = List.of(
        "mew", "celebi", "jirachi", "manaphy", "darkrai", "shaymin",
        "victini", "meloetta", "diancie", "marshadow"
    );
    public List<String> legendarySpecies = List.of(
        "articuno", "zapdos", "moltres", "mewtwo", "lugia", "ho-oh",
        "rayquaza", "dialga", "palkia", "giratina", "reshiram", "zekrom",
        "xerneas", "yveltal", "solgaleo", "lunala", "zacian", "zamazenta",
        "koraidon", "miraidon"
    );

    public static LuckyConfig load(Path path) {
        return load(path, false);
    }

    public static LuckyConfig load(Path path, boolean validateSpecies) {
        LuckyConfig defaults = new LuckyConfig();
        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) {
                Files.writeString(path, GSON.toJson(defaults));
                return defaults;
            }

            return read(path, validateSpecies);
        } catch (Exception error) {
            LuckyCobblemonMod.LOGGER.error("Could not load {}. Using defaults.", path, error);
            try {
                if (Files.exists(path)) {
                    Path backup = path.resolveSibling(path.getFileName() + ".invalid-" + Instant.now().toEpochMilli());
                    Files.copy(path, backup, StandardCopyOption.REPLACE_EXISTING);
                }
                Files.writeString(path, GSON.toJson(defaults));
            } catch (IOException backupError) {
                LuckyCobblemonMod.LOGGER.error("Could not back up or rewrite invalid config", backupError);
            }
            return defaults;
        }
    }

    public static LuckyConfig reload(Path path) throws IOException {
        return read(path, true);
    }

    private static LuckyConfig read(Path path, boolean validateSpecies) throws IOException {
        LuckyConfig loaded = GSON.fromJson(Files.readString(path), LuckyConfig.class);
        if (loaded == null) {
            throw new IllegalArgumentException("configuration is empty");
        }
        loaded.validate(validateSpecies);
        return loaded;
    }

    public int totalWeight() {
        return Math.toIntExact(totalWeightLong());
    }

    public void validate(boolean validateSpecies) {
        if (maxLogFiles < 1 || maxLogFiles > 100) {
            throw new IllegalArgumentException("maxLogFiles must stay between 1 and 100");
        }
        if (!validWeights() || totalWeightLong() <= 0) {
            throw new IllegalArgumentException("event weights must be between 0 and " + MAX_WEIGHT + " and total more than 0");
        }
        if (!validRange(commonMinLevel, commonMaxLevel)
            || !validRange(uncommonMinLevel, uncommonMaxLevel)
            || !validRange(rareMinLevel, rareMaxLevel)
            || !validRange(epicMinLevel, epicMaxLevel)
            || !validRange(mythicalMinLevel, mythicalMaxLevel)
            || !validRange(legendaryMinLevel, legendaryMaxLevel)) {
            throw new IllegalArgumentException("level ranges must stay between 1 and 100");
        }
        if (speciesPools().anyMatch(pool -> !validPool(pool))) {
            throw new IllegalArgumentException("species pools cannot be empty or contain invalid identifiers");
        }
        if (validateSpecies) {
            speciesPools().flatMap(List::stream).distinct().forEach(CobblemonSpeciesValidator::requireKnown);
        }
    }

    private long totalWeightLong() {
        return (long) commonWeight + uncommonWeight + rareWeight + itemBundleWeight + epicWeight + raidWeight
            + trioWeight + shrineWeight + mythicalWeight + unluckyWeight + legendaryJackpotWeight;
    }

    private boolean validWeights() {
        return Stream.of(commonWeight, uncommonWeight, rareWeight, itemBundleWeight, epicWeight, raidWeight,
            trioWeight, shrineWeight, mythicalWeight, unluckyWeight, legendaryJackpotWeight)
            .allMatch(weight -> weight >= 0 && weight <= MAX_WEIGHT);
    }

    private Stream<List<String>> speciesPools() {
        return Stream.of(commonSpecies, uncommonSpecies, rareSpecies, epicSpecies, mythicalSpecies, legendarySpecies);
    }

    private static boolean validRange(int min, int max) {
        return min >= 1 && max <= 100 && min <= max;
    }

    private static boolean validPool(List<String> pool) {
        return pool != null && !pool.isEmpty() && pool.stream().allMatch(value -> {
            if (value == null || value.isBlank()) {
                return false;
            }
            try {
                net.minecraft.util.Identifier.of(value.contains(":") ? value : "cobblemon:" + value);
                return true;
            } catch (RuntimeException error) {
                return false;
            }
        });
    }
}
