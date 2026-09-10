package br.com.ikezn.luckycobblemon;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;
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
        player.sendMessage(Text.translatable("message.luckycobblemon.trio").formatted(Formatting.LIGHT_PURPLE, Formatting.BOLD), false);
        for (int index = 0; index < 3; index++) {
            pokemon(world, player, pos, random, config.epicSpecies, 35, 55, false, "outcome.luckycobblemon.trio", Formatting.LIGHT_PURPLE);
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

    private static void unlucky(ServerWorld world, ServerPlayerEntity player, BlockPos pos, Random random) {
        if (random.nextBoolean()) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 160, 1));
            player.sendMessage(Text.translatable("message.luckycobblemon.unlucky_fog").formatted(Formatting.DARK_PURPLE), false);
            world.spawnParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 55, 1.0, 0.7, 1.0, 0.02);
        } else {
            drop(world, pos, Items.POISONOUS_POTATO, between(random, 12, 32));
            player.sendMessage(Text.translatable("message.luckycobblemon.unlucky_potatoes").formatted(Formatting.YELLOW), false);
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
