package br.com.ikezn.luckycobblemon;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LuckyConfigTest {
    @Test
    void defaultConfigurationIsStructurallyValid() {
        assertDoesNotThrow(() -> new LuckyConfig().validate(false));
    }

    @Test
    void rejectsNegativeAndExcessiveWeights() {
        LuckyConfig negative = new LuckyConfig();
        negative.commonWeight = -1;
        assertThrows(IllegalArgumentException.class, () -> negative.validate(false));

        LuckyConfig excessive = new LuckyConfig();
        excessive.legendaryJackpotWeight = 1_000_001;
        assertThrows(IllegalArgumentException.class, () -> excessive.validate(false));
    }

    @Test
    void rejectsZeroTotalWeight() {
        LuckyConfig config = new LuckyConfig();
        config.commonWeight = 0;
        config.uncommonWeight = 0;
        config.rareWeight = 0;
        config.itemBundleWeight = 0;
        config.epicWeight = 0;
        config.raidWeight = 0;
        config.trioWeight = 0;
        config.shrineWeight = 0;
        config.mythicalWeight = 0;
        config.unluckyWeight = 0;
        config.legendaryJackpotWeight = 0;
        assertThrows(IllegalArgumentException.class, () -> config.validate(false));
    }

    @Test
    void rejectsInvalidLevelsAndSpeciesPools() {
        LuckyConfig levels = new LuckyConfig();
        levels.rareMinLevel = 70;
        levels.rareMaxLevel = 50;
        assertThrows(IllegalArgumentException.class, () -> levels.validate(false));

        LuckyConfig emptyPool = new LuckyConfig();
        emptyPool.epicSpecies = List.of();
        assertThrows(IllegalArgumentException.class, () -> emptyPool.validate(false));

        LuckyConfig invalidIdentifier = new LuckyConfig();
        invalidIdentifier.commonSpecies = List.of("not a valid species id");
        assertThrows(IllegalArgumentException.class, () -> invalidIdentifier.validate(false));
    }
}
