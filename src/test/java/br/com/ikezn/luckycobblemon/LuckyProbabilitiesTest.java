package br.com.ikezn.luckycobblemon;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LuckyProbabilitiesTest {
    @Test
    void probabilitiesStayNormalizedAtLuckExtremes() {
        LuckyConfig config = new LuckyConfig();
        for (int luck : new int[] {-100, 0, 100}) {
            LuckyProbabilities probabilities = LuckyProbabilities.forLuck(config, luck);
            assertEquals(probabilities.total(),
                probabilities.outcomes().stream().mapToInt(LuckyProbabilities.WeightedOutcome::weight).sum());
            assertEquals(100.0D,
                probabilities.outcomes().stream().mapToDouble(probabilities::percentage).sum(), 0.000_001D);
        }
    }

    @Test
    void luckChangesRareAndUnluckyOutcomesInExpectedDirection() {
        LuckyConfig config = new LuckyConfig();
        LuckyProbabilities negative = LuckyProbabilities.forLuck(config, -100);
        LuckyProbabilities positive = LuckyProbabilities.forLuck(config, 100);

        assertTrue(weight(positive, LuckyProbabilities.Outcome.LEGENDARY)
            > weight(negative, LuckyProbabilities.Outcome.LEGENDARY));
        assertTrue(weight(negative, LuckyProbabilities.Outcome.UNLUCKY)
            > weight(positive, LuckyProbabilities.Outcome.UNLUCKY));
    }

    @Test
    void luckInputIsClampedAndRollBoundariesAreSafe() {
        LuckyConfig config = new LuckyConfig();
        assertEquals(LuckyProbabilities.forLuck(config, -100), LuckyProbabilities.forLuck(config, -500));
        assertEquals(LuckyProbabilities.forLuck(config, 100), LuckyProbabilities.forLuck(config, 500));

        LuckyProbabilities probabilities = LuckyProbabilities.forLuck(config, 0);
        assertEquals(LuckyProbabilities.Outcome.COMMON, probabilities.outcomeForRoll(0));
        assertEquals(LuckyProbabilities.Outcome.LEGENDARY, probabilities.outcomeForRoll(probabilities.total() - 1));
        assertThrows(IllegalArgumentException.class, () -> probabilities.outcomeForRoll(-1));
        assertThrows(IllegalArgumentException.class, () -> probabilities.outcomeForRoll(probabilities.total()));
    }

    @Test
    void zeroWeightDisablesAnOutcome() {
        LuckyConfig config = new LuckyConfig();
        config.commonWeight = 0;
        config.uncommonWeight = 0;
        LuckyProbabilities probabilities = LuckyProbabilities.forLuck(config, 100);

        assertEquals(0, weight(probabilities, LuckyProbabilities.Outcome.COMMON));
        assertEquals(0, weight(probabilities, LuckyProbabilities.Outcome.UNCOMMON));
    }

    private static int weight(LuckyProbabilities probabilities, LuckyProbabilities.Outcome outcome) {
        return probabilities.outcomes().stream()
            .filter(weighted -> weighted.outcome() == outcome)
            .findFirst()
            .orElseThrow()
            .weight();
    }
}
