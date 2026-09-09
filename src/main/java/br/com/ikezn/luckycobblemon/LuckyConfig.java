package br.com.ikezn.luckycobblemon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;

public final class LuckyConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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
        LuckyConfig defaults = new LuckyConfig();
        try {
            Files.createDirectories(path.getParent());
            if (Files.notExists(path)) {
                Files.writeString(path, GSON.toJson(defaults));
                return defaults;
            }

            LuckyConfig loaded = GSON.fromJson(Files.readString(path), LuckyConfig.class);
            if (loaded == null || !loaded.isValid()) {
                throw new IllegalArgumentException("weights, levels or species pools are invalid");
            }
            return loaded;
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

    public int totalWeight() {
        return commonWeight + uncommonWeight + rareWeight + itemBundleWeight + epicWeight + raidWeight
            + trioWeight + shrineWeight + mythicalWeight + unluckyWeight + legendaryJackpotWeight;
    }

    private boolean isValid() {
        return totalWeight() > 0
            && commonWeight >= 0 && uncommonWeight >= 0 && rareWeight >= 0
            && itemBundleWeight >= 0 && epicWeight >= 0 && raidWeight >= 0 && trioWeight >= 0
            && shrineWeight >= 0 && mythicalWeight >= 0 && unluckyWeight >= 0
            && legendaryJackpotWeight >= 0
            && validRange(commonMinLevel, commonMaxLevel)
            && validRange(uncommonMinLevel, uncommonMaxLevel)
            && validRange(rareMinLevel, rareMaxLevel)
            && validRange(epicMinLevel, epicMaxLevel)
            && validRange(mythicalMinLevel, mythicalMaxLevel)
            && validRange(legendaryMinLevel, legendaryMaxLevel)
            && validPool(commonSpecies) && validPool(uncommonSpecies) && validPool(rareSpecies)
            && validPool(epicSpecies) && validPool(mythicalSpecies) && validPool(legendarySpecies);
    }

    private static boolean validRange(int min, int max) {
        return min >= 1 && max <= 100 && min <= max;
    }

    private static boolean validPool(List<String> pool) {
        return pool != null && !pool.isEmpty() && pool.stream().allMatch(value -> value != null && !value.isBlank());
    }
}
