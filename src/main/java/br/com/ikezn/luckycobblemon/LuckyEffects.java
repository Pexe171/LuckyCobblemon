package br.com.ikezn.luckycobblemon;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public final class LuckyEffects {
    private LuckyEffects() {
    }

    public static void roll(ServerWorld world, ServerPlayerEntity player, BlockPos pos, LuckyConfig config, int luck) {
        Random random = world.getRandom();
        AdjustedWeights weights = AdjustedWeights.forLuck(config, luck);
        int roll = random.nextInt(weights.total());

        if ((roll -= weights.common()) < 0) {
            pokemon(world, player, pos, random, config.commonSpecies, config.commonMinLevel, config.commonMaxLevel, false, "outcome.luckycobblemon.common", Formatting.GREEN);
        } else if ((roll -= weights.uncommon()) < 0) {
            pokemon(world, player, pos, random, config.uncommonSpecies, config.uncommonMinLevel, config.uncommonMaxLevel, false, "outcome.luckycobblemon.uncommon", Formatting.AQUA);
        } else if ((roll -= weights.rare()) < 0) {
            pokemon(world, player, pos, random, config.rareSpecies, config.rareMinLevel, config.rareMaxLevel, false, "outcome.luckycobblemon.rare", Formatting.BLUE);
        } else if ((roll -= weights.itemBundle()) < 0) {
            itemBundle(world, player, pos, random, luck);
        } else if ((roll -= weights.epic()) < 0) {
            pokemon(world, player, pos, random, config.epicSpecies, config.epicMinLevel, config.epicMaxLevel, random.nextInt(20) == 0, "outcome.luckycobblemon.epic", Formatting.LIGHT_PURPLE);
        } else if ((roll -= weights.raid()) < 0) {
            raidDen(world, player, pos, random, config);
        } else if ((roll -= weights.trio()) < 0) {
            trio(world, player, pos, random, config);
        } else if ((roll -= weights.shrine()) < 0) {
            shrine(world, player, pos, random, config);
        } else if ((roll -= weights.mythical()) < 0) {
            pokemon(world, player, pos, random, config.mythicalSpecies, config.mythicalMinLevel, config.mythicalMaxLevel, random.nextInt(10) == 0, "outcome.luckycobblemon.mythical", Formatting.GOLD);
        } else if ((roll -= weights.unlucky()) < 0) {
            unlucky(world, player, pos, random);
        } else {
            pokemon(world, player, pos, random, config.legendarySpecies, config.legendaryMinLevel, config.legendaryMaxLevel, true, "outcome.luckycobblemon.legendary", Formatting.GOLD);
            drop(world, pos, resolveCobblemonItem("master_ball", Items.DIAMOND), 1);
        }

        String luckLabel = luck > 0 ? "+" + luck : Integer.toString(luck);
        player.sendMessage(Text.translatable("message.luckycobblemon.luck_used", luckLabel).formatted(luck > 0 ? Formatting.GREEN : luck < 0 ? Formatting.RED : Formatting.GRAY), true);
        world.spawnParticles(luck < 0 ? ParticleTypes.LARGE_SMOKE : ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 28, 0.45, 0.45, 0.45, 0.08);
        world.playSound(null, pos, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.BLOCKS, 1.0F, 0.8F + random.nextFloat() * 0.5F);
    }

    private static void pokemon(
        ServerWorld world,
        ServerPlayerEntity player,
        BlockPos pos,
        Random random,
        List<String> species,
        int minLevel,
        int maxLevel,
        boolean shiny,
        String tierTranslationKey,
        Formatting color
    ) {
        String selected = species.get(random.nextInt(species.size()));
        int level = between(random, minLevel, maxLevel);
        String command = "spawnpokemon " + selected + " level=" + level + (shiny ? " shiny=true" : "");
        int result;
        try {
            result = world.getServer().getCommandManager().getDispatcher().execute(
                command,
                player.getCommandSource().withLevel(4).withSilent()
            );
        } catch (CommandSyntaxException error) {
            result = 0;
            LuckyCobblemonMod.LOGGER.warn("Cobblemon command syntax failed: {}", command, error);
        }

        if (result > 0) {
            String displayName = selected.replace('-', ' ');
            player.sendMessage(Text.translatable("message.luckycobblemon.pokemon",
                Text.translatable(tierTranslationKey), displayName, level,
                shiny ? Text.translatable("message.luckycobblemon.shiny") : Text.empty()
            ).formatted(color, Formatting.BOLD), false);
        } else {
            LuckyCobblemonMod.LOGGER.warn("Cobblemon command failed: {}", command);
            player.sendMessage(Text.translatable("message.luckycobblemon.pokemon_fallback").formatted(Formatting.YELLOW), false);
            drop(world, pos, resolveCobblemonItem("rare_candy", Items.EMERALD), 3);
        }
    }

    private static void itemBundle(ServerWorld world, ServerPlayerEntity player, BlockPos pos, Random random, int luck) {
        switch (random.nextInt(7)) {
            case 0 -> ballBundle(world, player, pos, random, luck);
            case 1 -> {
                drop(world, pos, resolveCobblemonItem("red_apricorn", Items.APPLE), between(random, 6, 14));
                drop(world, pos, resolveCobblemonItem("blue_apricorn", Items.SWEET_BERRIES), between(random, 6, 14));
                drop(world, pos, resolveCobblemonItem("yellow_apricorn", Items.GLOW_BERRIES), between(random, 6, 14));
                player.sendMessage(Text.translatable("message.luckycobblemon.apricorn_harvest").formatted(Formatting.RED), false);
            }
            case 2 -> {
                drop(world, pos, resolveCobblemonItem("oran_berry", Items.SWEET_BERRIES), between(random, 4, 10));
                drop(world, pos, resolveCobblemonItem("sitrus_berry", Items.GLOW_BERRIES), between(random, 2, 6));
                drop(world, pos, resolveCobblemonItem("lum_berry", Items.GOLDEN_CARROT), between(random, 1, 3));
                player.sendMessage(Text.translatable("message.luckycobblemon.berry_picnic").formatted(Formatting.GREEN), false);
            }
            case 3 -> {
                Item[] stones = {
                    resolveCobblemonItem("fire_stone", Items.BLAZE_POWDER),
                    resolveCobblemonItem("water_stone", Items.PRISMARINE_CRYSTALS),
                    resolveCobblemonItem("thunder_stone", Items.GLOWSTONE_DUST),
                    resolveCobblemonItem("leaf_stone", Items.MOSS_BLOCK)
                };
                drop(world, pos, stones[random.nextInt(stones.length)], between(random, 1, 2));
                player.sendMessage(Text.translatable("message.luckycobblemon.evolution_cache").formatted(Formatting.LIGHT_PURPLE), false);
            }
            case 4 -> {
                drop(world, pos, resolveCobblemonItem("potion", Items.HONEY_BOTTLE), between(random, 3, 7));
                drop(world, pos, resolveCobblemonItem("revive", Items.GOLDEN_APPLE), between(random, 1, 3));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1));
                player.sendMessage(Text.translatable("message.luckycobblemon.medicine_kit").formatted(Formatting.RED), false);
            }
            case 5 -> {
                drop(world, pos, Items.DIAMOND, between(random, 1, 4));
                drop(world, pos, Items.EMERALD, between(random, 3, 8));
                drop(world, pos, Items.GOLD_INGOT, between(random, 5, 12));
                player.sendMessage(Text.translatable("message.luckycobblemon.mineral_cache").formatted(Formatting.GOLD), false);
            }
            case 6 -> {
                drop(world, pos, resolveCobblemonItem("fossilized_bird", Items.BONE), between(random, 1, 2));
                drop(world, pos, resolveCobblemonItem("fossilized_fish", Items.NAUTILUS_SHELL), between(random, 1, 2));
                player.sendMessage(Text.translatable("message.luckycobblemon.fossil_cache").formatted(Formatting.GRAY), false);
            }
            default -> throw new IllegalStateException("Unexpected item event");
        }
    }

    private static void ballBundle(ServerWorld world, ServerPlayerEntity player, BlockPos pos, Random random, int luck) {
        Item ball;
        int count;
        int quality = Math.max(0, random.nextInt(100) - Math.max(0, luck) / 4);
        if (quality < 3) {
            ball = resolveCobblemonItem("master_ball", Items.DIAMOND);
            count = 1;
        } else if (quality < 30) {
            ball = resolveCobblemonItem("ultra_ball", Items.GOLD_INGOT);
            count = between(random, 3, 8);
        } else if (quality < 65) {
            ball = resolveCobblemonItem("great_ball", Items.IRON_INGOT);
            count = between(random, 5, 12);
        } else {
            ball = resolveCobblemonItem("poke_ball", Items.REDSTONE);
            count = between(random, 8, 20);
        }
        drop(world, pos, ball, count);
        drop(world, pos, resolveCobblemonItem("rare_candy", Items.EXPERIENCE_BOTTLE), between(random, 1, 4));
        player.sendMessage(Text.translatable("message.luckycobblemon.item_bundle").formatted(Formatting.AQUA), false);
    }

    private static void trio(ServerWorld world, ServerPlayerEntity player, BlockPos pos, Random random, LuckyConfig config) {
        switch (random.nextInt(4)) {
            case 0 -> {
                player.sendMessage(Text.translatable("message.luckycobblemon.trio").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD), false);
                for (int index = 0; index < 3; index++) {
                    pokemon(world, player, pos, random, config.epicSpecies, 35, 55, false, "outcome.luckycobblemon.trio", Formatting.LIGHT_PURPLE);
                }
            }
            case 1 -> {
                player.sendMessage(Text.translatable("message.luckycobblemon.common_swarm").formatted(Formatting.GREEN, Formatting.BOLD), false);
                for (int index = 0; index < 5; index++) {
                    pokemon(world, player, pos, random, config.commonSpecies, 10, 25, false, "message.luckycobblemon.swarm_member", Formatting.GREEN);
                }
            }
            case 2 -> {
                player.sendMessage(Text.translatable("message.luckycobblemon.rare_duo").formatted(Formatting.BLUE, Formatting.BOLD), false);
                for (int index = 0; index < 2; index++) {
                    pokemon(world, player, pos, random, config.rareSpecies, config.rareMinLevel, config.rareMaxLevel, false,
                        "message.luckycobblemon.duo_member", Formatting.BLUE);
                }
            }
            case 3 -> {
                player.sendMessage(Text.translatable("message.luckycobblemon.starter_parade").formatted(Formatting.GOLD, Formatting.BOLD), false);
                List<String> starters = List.of(
                    "bulbasaur", "charmander", "squirtle", "chikorita", "cyndaquil", "totodile",
                    "treecko", "torchic", "mudkip", "turtwig", "chimchar", "piplup",
                    "snivy", "tepig", "oshawott", "chespin", "fennekin", "froakie",
                    "rowlet", "litten", "popplio", "grookey", "scorbunny", "sobble",
                    "sprigatito", "fuecoco", "quaxly"
                );
                for (int index = 0; index < 3; index++) {
                    pokemon(world, player, pos, random, starters, 15, 30, false,
                        "message.luckycobblemon.starter", Formatting.GOLD);
                }
            }
            default -> throw new IllegalStateException("Unexpected group encounter");
        }
    }

    private static void raidDen(ServerWorld world, ServerPlayerEntity player, BlockPos pos, Random random, LuckyConfig config) {
        if (!FabricLoader.getInstance().isModLoaded("cobblemonraiddens")) {
            player.sendMessage(Text.translatable("message.luckycobblemon.raid_missing").formatted(Formatting.YELLOW), false);
            pokemon(world, player, pos, random, config.rareSpecies, config.rareMinLevel, config.rareMaxLevel, false, "outcome.luckycobblemon.rare", Formatting.BLUE);
            return;
        }

        String command = "crd dens " + pos.getX() + " " + pos.getY() + " " + pos.getZ() + " random";
        int result;
        try {
            result = world.getServer().getCommandManager().getDispatcher().execute(
                command,
                player.getCommandSource().withLevel(4).withSilent()
            );
        } catch (CommandSyntaxException error) {
            result = 0;
            LuckyCobblemonMod.LOGGER.warn("Raid Dens command syntax failed: {}", command, error);
        }

        if (result > 0) {
            player.sendMessage(Text.translatable("message.luckycobblemon.raid_success").formatted(Formatting.RED, Formatting.BOLD), false);
            world.spawnParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 45, 0.65, 0.8, 0.65, 0.04);
        } else {
            player.sendMessage(Text.translatable("message.luckycobblemon.raid_fallback").formatted(Formatting.YELLOW), false);
            pokemon(world, player, pos, random, config.rareSpecies, config.rareMinLevel, config.rareMaxLevel, false, "outcome.luckycobblemon.rare", Formatting.BLUE);
        }
    }

    private static void shrine(ServerWorld world, ServerPlayerEntity player, BlockPos pos, Random random, LuckyConfig config) {
        int shrineType = random.nextInt(3);
        if (shrineType == 1) {
            crystalAltar(world, player, pos, random, config);
            return;
        }
        if (shrineType == 2) {
            healingGarden(world, player, pos, random);
            return;
        }

        BlockPos floor = pos.down();
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos target = floor.add(x, 0, z);
                if (world.getBlockState(target).isReplaceable()) {
                    boolean edge = Math.abs(x) == 2 || Math.abs(z) == 2;
                    world.setBlockState(target, edge ? Blocks.QUARTZ_BRICKS.getDefaultState() : Blocks.GOLD_BLOCK.getDefaultState());
                }
            }
        }
        if (world.getBlockState(pos).isAir()) {
            world.setBlockState(pos, Blocks.CHEST.getDefaultState());
            if (world.getBlockEntity(pos) instanceof net.minecraft.block.entity.ChestBlockEntity chest) {
                chest.setStack(13, new ItemStack(resolveCobblemonItem("rare_candy", Items.EMERALD), between(random, 3, 8)));
                chest.setStack(12, new ItemStack(resolveCobblemonItem("ultra_ball", Items.GOLD_INGOT), between(random, 5, 12)));
                chest.setStack(14, new ItemStack(Items.DIAMOND, between(random, 2, 5)));
            }
        }
        pokemon(world, player, pos.up(), random, config.epicSpecies, config.epicMinLevel, config.epicMaxLevel, false, "message.luckycobblemon.shrine_guardian", Formatting.LIGHT_PURPLE);
        player.sendMessage(Text.translatable("message.luckycobblemon.shrine").formatted(Formatting.GOLD, Formatting.BOLD), false);
    }

    private static void crystalAltar(ServerWorld world, ServerPlayerEntity player, BlockPos pos, Random random, LuckyConfig config) {
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 && Math.abs(z) == 2) {
                    BlockPos target = pos.add(x, 0, z);
                    if (world.getBlockState(target).isAir()) {
                        world.setBlockState(target, Blocks.AMETHYST_BLOCK.getDefaultState());
                    }
                }
            }
        }
        drop(world, pos, Items.AMETHYST_SHARD, between(random, 8, 20));
        drop(world, pos, resolveCobblemonItem("rare_candy", Items.EXPERIENCE_BOTTLE), between(random, 2, 5));
        pokemon(world, player, pos.up(), random, config.rareSpecies, config.rareMinLevel, config.rareMaxLevel,
            random.nextInt(20) == 0, "message.luckycobblemon.crystal_guardian", Formatting.AQUA);
        world.spawnParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
            60, 1.5, 0.8, 1.5, 0.03);
        player.sendMessage(Text.translatable("message.luckycobblemon.crystal_altar").formatted(Formatting.AQUA, Formatting.BOLD), false);
    }

    private static void healingGarden(ServerWorld world, ServerPlayerEntity player, BlockPos pos, Random random) {
        Block[] flowers = {Blocks.DANDELION, Blocks.POPPY, Blocks.AZURE_BLUET, Blocks.OXEYE_DAISY};
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                BlockPos target = pos.add(x, 0, z);
                if ((Math.abs(x) + Math.abs(z)) % 2 == 0 && world.getBlockState(target).isAir()) {
                    world.setBlockState(target, flowers[random.nextInt(flowers.length)].getDefaultState());
                }
            }
        }
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 400, 1));
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 400, 0));
        drop(world, pos, resolveCobblemonItem("revive", Items.GOLDEN_APPLE), between(random, 1, 3));
        world.spawnParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
            45, 1.4, 0.6, 1.4, 0.04);
        player.sendMessage(Text.translatable("message.luckycobblemon.healing_garden").formatted(Formatting.GREEN, Formatting.BOLD), false);
    }

    private static void unlucky(ServerWorld world, ServerPlayerEntity player, BlockPos pos, Random random) {
        switch (random.nextInt(6)) {
            case 0 -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 160, 1));
                player.sendMessage(Text.translatable("message.luckycobblemon.unlucky_fog").formatted(Formatting.DARK_PURPLE), false);
                world.spawnParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 55, 1.0, 0.7, 1.0, 0.02);
            }
            case 1 -> {
                drop(world, pos, Items.POISONOUS_POTATO, between(random, 12, 32));
                player.sendMessage(Text.translatable("message.luckycobblemon.unlucky_potatoes").formatted(Formatting.YELLOW), false);
            }
            case 2 -> {
                world.playSound(null, pos, SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 2.0F, 1.3F);
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    50, 0.8, 1.0, 0.8, 0.12);
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 160, 0));
                player.sendMessage(Text.translatable("message.luckycobblemon.unlucky_thunder").formatted(Formatting.YELLOW), false);
            }
            case 3 -> {
                for (int y = 0; y <= 1; y++) {
                    BlockPos target = pos.up(y);
                    if (world.getBlockState(target).isAir()) {
                        world.setBlockState(target, Blocks.COBWEB.getDefaultState());
                    }
                }
                player.sendMessage(Text.translatable("message.luckycobblemon.unlucky_web").formatted(Formatting.WHITE), false);
            }
            case 4 -> {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 240, 1));
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 0));
                player.sendMessage(Text.translatable("message.luckycobblemon.unlucky_hunger").formatted(Formatting.DARK_GREEN), false);
            }
            case 5 -> {
                drop(world, pos, Items.YELLOW_DYE, between(random, 16, 32));
                world.spawnParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                    35, 0.5, 0.5, 0.5, 0.05);
                player.sendMessage(Text.translatable("message.luckycobblemon.unlucky_fake_gold").formatted(Formatting.GOLD), false);
            }
            default -> throw new IllegalStateException("Unexpected unlucky event");
        }
    }

    private static Item resolveCobblemonItem(String path, Item fallback) {
        Item item = Registries.ITEM.get(Identifier.of("cobblemon", path));
        return item == Items.AIR ? fallback : item;
    }

    private static void drop(ServerWorld world, BlockPos pos, Item item, int count) {
        ItemEntity entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.35, pos.getZ() + 0.5, new ItemStack(item, count));
        entity.setToDefaultPickupDelay();
        world.spawnEntity(entity);
    }

    private static int between(Random random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private record AdjustedWeights(
        int common,
        int uncommon,
        int rare,
        int itemBundle,
        int epic,
        int raid,
        int trio,
        int shrine,
        int mythical,
        int unlucky,
        int legendary
    ) {
        private static AdjustedWeights forLuck(LuckyConfig config, int luck) {
            List<LuckyProbabilities.WeightedOutcome> weights = LuckyProbabilities.forLuck(config, luck).outcomes();
            return new AdjustedWeights(
                weights.get(0).weight(), weights.get(1).weight(), weights.get(2).weight(),
                weights.get(3).weight(), weights.get(4).weight(), weights.get(5).weight(),
                weights.get(6).weight(), weights.get(7).weight(), weights.get(8).weight(),
                weights.get(9).weight(), weights.get(10).weight()
            );
        }

        private int total() {
            return common + uncommon + rare + itemBundle + epic + raid + trio + shrine + mythical + unlucky + legendary;
        }
    }
}
