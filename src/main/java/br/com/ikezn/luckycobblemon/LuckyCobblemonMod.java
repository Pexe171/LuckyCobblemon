package br.com.ikezn.luckycobblemon;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.nio.file.Path;
import java.util.Locale;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.literal;

public final class LuckyCobblemonMod implements ModInitializer {
    public static final String MOD_ID = "luckycobblemon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Identifier LUCKY_BLOCK_ID = Identifier.of(MOD_ID, "lucky_block");
    public static final Identifier RARE_LUCKY_BLOCK_ID = Identifier.of(MOD_ID, "rare_lucky_block");
    public static final Identifier LEGENDARY_LUCKY_BLOCK_ID = Identifier.of(MOD_ID, "legendary_lucky_block");
    public static final Identifier CURSED_LUCKY_BLOCK_ID = Identifier.of(MOD_ID, "cursed_lucky_block");
    public static final Identifier LUCK_ID = Identifier.of(MOD_ID, "luck");
    public static final Identifier NATURAL_LUCKY_BLOCK_ID = Identifier.of(MOD_ID, "natural_lucky_block");
    public static final RegistryKey<PlacedFeature> NATURAL_LUCKY_BLOCK_PLACED_KEY = RegistryKey.of(
        RegistryKeys.PLACED_FEATURE,
        NATURAL_LUCKY_BLOCK_ID
    );

    public static final Block LUCKY_BLOCK = Registry.register(
        Registries.BLOCK,
        LUCKY_BLOCK_ID,
        new LuckyBlock(AbstractBlock.Settings.create()
            .mapColor(MapColor.RED)
            .strength(0.8F)
            .nonOpaque()
            .sounds(BlockSoundGroup.METAL)
            .luminance(state -> 7))
    );

    public static final Block RARE_LUCKY_BLOCK = Registry.register(
        Registries.BLOCK,
        RARE_LUCKY_BLOCK_ID,
        new LuckyBlock(AbstractBlock.Settings.create()
            .mapColor(MapColor.BLUE)
            .strength(1.2F)
            .nonOpaque()
            .sounds(BlockSoundGroup.METAL)
            .luminance(state -> 9))
    );

    public static final Block LEGENDARY_LUCKY_BLOCK = Registry.register(
        Registries.BLOCK,
        LEGENDARY_LUCKY_BLOCK_ID,
        new LuckyBlock(AbstractBlock.Settings.create()
            .mapColor(MapColor.GOLD)
            .strength(1.6F)
            .nonOpaque()
            .sounds(BlockSoundGroup.METAL)
            .luminance(state -> 13))
    );

    public static final Block CURSED_LUCKY_BLOCK = Registry.register(
        Registries.BLOCK,
        CURSED_LUCKY_BLOCK_ID,
        new LuckyBlock(AbstractBlock.Settings.create()
            .mapColor(MapColor.PURPLE)
            .strength(1.0F)
            .nonOpaque()
            .sounds(BlockSoundGroup.DEEPSLATE)
            .luminance(state -> 5))
    );

    public static final ComponentType<Integer> LUCK_COMPONENT = Registry.register(
        Registries.DATA_COMPONENT_TYPE,
        LUCK_ID,
        ComponentType.<Integer>builder()
            .codec(com.mojang.serialization.Codec.intRange(-100, 100))
            .packetCodec(PacketCodecs.VAR_INT)
            .build()
    );

    public static final BlockEntityType<LuckyBlockEntity> LUCKY_BLOCK_ENTITY = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        LUCKY_BLOCK_ID,
        FabricBlockEntityTypeBuilder.create(
            LuckyBlockEntity::new,
            LUCKY_BLOCK,
            RARE_LUCKY_BLOCK,
            LEGENDARY_LUCKY_BLOCK,
            CURSED_LUCKY_BLOCK
        ).build()
    );

    public static final Item LUCKY_BLOCK_ITEM = Registry.register(
        Registries.ITEM,
        LUCKY_BLOCK_ID,
        new LuckyBlockItem(LUCKY_BLOCK, new Item.Settings())
    );

    public static final Item RARE_LUCKY_BLOCK_ITEM = Registry.register(
        Registries.ITEM,
        RARE_LUCKY_BLOCK_ID,
        new LuckyBlockItem(RARE_LUCKY_BLOCK, new Item.Settings().component(LUCK_COMPONENT, 35))
    );

    public static final Item LEGENDARY_LUCKY_BLOCK_ITEM = Registry.register(
        Registries.ITEM,
        LEGENDARY_LUCKY_BLOCK_ID,
        new LuckyBlockItem(LEGENDARY_LUCKY_BLOCK, new Item.Settings().component(LUCK_COMPONENT, 75))
    );

    public static final Item CURSED_LUCKY_BLOCK_ITEM = Registry.register(
        Registries.ITEM,
        CURSED_LUCKY_BLOCK_ID,
        new LuckyBlockItem(CURSED_LUCKY_BLOCK, new Item.Settings().component(LUCK_COMPONENT, -65))
    );

    public static final RecipeSerializer<LuckyTuningRecipe> LUCK_TUNING_RECIPE = Registry.register(
        Registries.RECIPE_SERIALIZER,
        Identifier.of(MOD_ID, "luck_tuning"),
        new SpecialRecipeSerializer<>(LuckyTuningRecipe::new)
    );

    public static final Feature<net.minecraft.world.gen.feature.DefaultFeatureConfig> NATURAL_LUCKY_BLOCK_FEATURE = Registry.register(
        Registries.FEATURE,
        NATURAL_LUCKY_BLOCK_ID,
        new NaturalLuckyBlockFeature()
    );

    private final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("luckycobblemon.json");
    private volatile LuckyConfig config;

    @Override
    public void onInitialize() {
        config = LuckyConfig.load(configPath);

        String version = FabricLoader.getInstance().getModContainer(MOD_ID)
            .map(container -> container.getMetadata().getVersion().getFriendlyString())
            .orElse("unknown");
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(LUCKY_BLOCK_ITEM);
            entries.add(RARE_LUCKY_BLOCK_ITEM);
            entries.add(LEGENDARY_LUCKY_BLOCK_ITEM);
            entries.add(CURSED_LUCKY_BLOCK_ITEM);
        });
        PlayerBlockBreakEvents.AFTER.register(this::afterBlockBroken);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(literal("luckycobblemon")
                .then(literal("reload")
                    .requires(source -> source.hasPermissionLevel(2))
                    .executes(context -> reloadConfig(context.getSource())))
                .then(literal("chances")
                    .executes(context -> showChances(context.getSource())))
                .then(literal("logs")
                    .executes(context -> showLogPath(context.getSource()))))
        );
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            config = LuckyConfig.load(configPath, true);
            LuckySessionLogger.initialize(
                FabricLoader.getInstance().getGameDir(),
                version,
                config.eventLogging,
                config.maxLogFiles
            );
            LOGGER.info("Cobblemon species pools validated successfully");
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> LuckySessionLogger.shutdown());
        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Feature.VEGETAL_DECORATION,
            NATURAL_LUCKY_BLOCK_PLACED_KEY
        );

        boolean raidDensAvailable = FabricLoader.getInstance().isModLoaded("cobblemonraiddens");
        LOGGER.info("Lucky Cobblemon: Fortune Blocks {} initialized with {} base weight; Raid Dens: {}",
            version, config.totalWeight(), raidDensAvailable ? "available" : "fallback enabled");
    }

    private void afterBlockBroken(
        World world,
        net.minecraft.entity.player.PlayerEntity player,
        BlockPos pos,
        BlockState state,
        BlockEntity blockEntity
    ) {
        if (world instanceof ServerWorld serverWorld
            && player instanceof ServerPlayerEntity serverPlayer
            && state.getBlock() instanceof LuckyBlock) {
            if (serverPlayer.isCreative() && !config.allowCreativeActivation) {
                serverPlayer.sendMessage(Text.translatable("message.luckycobblemon.creative_disabled").formatted(Formatting.YELLOW), true);
                return;
            }
            int luck = blockEntity instanceof LuckyBlockEntity luckyBlockEntity ? luckyBlockEntity.getLuck() : 0;
            LuckyEffects.roll(
                serverWorld,
                serverPlayer,
                pos,
                config,
                luck,
                Registries.BLOCK.getId(state.getBlock()).toString()
            );
        }
    }

    private int reloadConfig(ServerCommandSource source) {
        try {
            LuckyConfig reloaded = LuckyConfig.reload(configPath);
            config = reloaded;
            LuckySessionLogger.configure(reloaded.eventLogging, reloaded.maxLogFiles);
            source.sendFeedback(() -> Text.translatable("command.luckycobblemon.reload.success"), true);
            LOGGER.info("Lucky Cobblemon configuration reloaded with {} base weight", reloaded.totalWeight());
            return Command.SINGLE_SUCCESS;
        } catch (Exception error) {
            LOGGER.warn("Rejected invalid Lucky Cobblemon configuration; keeping the previous settings", error);
            source.sendError(Text.translatable("command.luckycobblemon.reload.failure", error.getMessage()));
            return 0;
        }
    }

    private int showChances(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = source.getPlayerOrThrow();
        ItemStack stack = player.getMainHandStack();
        if (!(stack.getItem() instanceof LuckyBlockItem)) {
            source.sendError(Text.translatable("command.luckycobblemon.chances.no_block"));
            return 0;
        }

        int luck = LuckyBlockItem.getLuck(stack);
        LuckyProbabilities probabilities = LuckyProbabilities.forLuck(config, luck);
        String luckLabel = luck > 0 ? "+" + luck : Integer.toString(luck);
        source.sendFeedback(() -> Text.translatable("command.luckycobblemon.chances.header", luckLabel), false);
        for (LuckyProbabilities.WeightedOutcome outcome : probabilities.outcomes()) {
            String percentage = String.format(Locale.ROOT, "%.2f%%", probabilities.percentage(outcome));
            source.sendFeedback(() -> Text.translatable("command.luckycobblemon.chances.line",
                Text.translatable(outcome.outcome().translationKey()), percentage), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private int showLogPath(ServerCommandSource source) {
        Path logFile = LuckySessionLogger.currentFile();
        if (logFile == null || !config.eventLogging) {
            source.sendError(Text.translatable("command.luckycobblemon.logs.disabled"));
            return 0;
        }
        source.sendFeedback(
            () -> Text.translatable("command.luckycobblemon.logs.path", logFile.toAbsolutePath().normalize().toString()),
            false
        );
        return Command.SINGLE_SUCCESS;
    }
}
