package br.com.ikezn.luckycobblemon;

import java.util.List;

public record LuckyProbabilities(List<WeightedOutcome> outcomes, int total) {
    public LuckyProbabilities {
        outcomes = List.copyOf(outcomes);
        if (total <= 0 || outcomes.stream().mapToInt(WeightedOutcome::weight).sum() != total) {
            throw new IllegalArgumentException("probability weights must have a positive matching total");
        }
    }

    public static LuckyProbabilities forLuck(LuckyConfig config, int luck) {
        int clampedLuck = Math.clamp(luck, -100, 100);
        int positive = Math.max(0, clampedLuck);
        int negative = Math.max(0, -clampedLuck);
        List<WeightedOutcome> outcomes = List.of(
            weighted(Outcome.COMMON, decrease(config.commonWeight, positive, 125)),
            weighted(Outcome.UNCOMMON, decrease(config.uncommonWeight, positive, 200)),
            weighted(Outcome.RARE, config.rareWeight + positive / 10),
            weighted(Outcome.ITEM_BUNDLE, config.itemBundleWeight + positive / 20 + negative / 20),
            weighted(Outcome.EPIC, config.epicWeight + positive / 12),
            weighted(Outcome.RAID, config.raidWeight + positive / 20),
            weighted(Outcome.TRIO, config.trioWeight + positive / 25),
            weighted(Outcome.SHRINE, config.shrineWeight + positive / 25),
            weighted(Outcome.MYTHICAL, config.mythicalWeight + positive / 25),
            weighted(Outcome.UNLUCKY, Math.max(0, config.unluckyWeight - positive / 25) + negative / 4),
            weighted(Outcome.LEGENDARY, config.legendaryJackpotWeight + positive / 20)
        );
        return new LuckyProbabilities(outcomes, outcomes.stream().mapToInt(WeightedOutcome::weight).sum());
    }

    public Outcome outcomeForRoll(int roll) {
        if (roll < 0 || roll >= total) {
            throw new IllegalArgumentException("roll must be between 0 and total - 1");
        }
        int remaining = roll;
        for (WeightedOutcome outcome : outcomes) {
            remaining -= outcome.weight();
            if (remaining < 0) {
                return outcome.outcome();
            }
        }
        throw new IllegalStateException("weighted outcome selection exhausted unexpectedly");
    }

    public double percentage(WeightedOutcome outcome) {
        return outcome.weight() * 100.0D / total;
    }

    private static WeightedOutcome weighted(Outcome outcome, int weight) {
        return new WeightedOutcome(outcome, weight);
    }

    private static int decrease(int weight, int positiveLuck, int divisor) {
        return weight == 0 ? 0 : Math.max(1, weight - weight * positiveLuck / divisor);
    }

    public record WeightedOutcome(Outcome outcome, int weight) {
        public WeightedOutcome {
            if (weight < 0) {
                throw new IllegalArgumentException("weight cannot be negative");
            }
        }
    }

    public enum Outcome {
        COMMON("common"), UNCOMMON("uncommon"), RARE("rare"), ITEM_BUNDLE("item_bundle"),
        EPIC("epic"), RAID("raid"), TRIO("trio"), SHRINE("shrine"), MYTHICAL("mythical"),
        UNLUCKY("unlucky"), LEGENDARY("legendary");

        private final String key;

        Outcome(String key) {
            this.key = key;
        }

        public String translationKey() {
            return "outcome.luckycobblemon." + key;
        }
    }
}
