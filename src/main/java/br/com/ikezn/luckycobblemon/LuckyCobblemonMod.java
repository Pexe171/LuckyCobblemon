package br.com.ikezn.luckycobblemon;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
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
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LuckyCobblemonMod implements ModInitializer {
    public static final String MOD_ID = "luckycobblemon";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Identifier LUCKY_BLOCK_ID = Identifier.of(MOD_ID, "lucky_block");
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
        FabricBlockEntityTypeBuilder.create(LuckyBlockEntity::new, LUCKY_BLOCK).build()
    );

    public static final Item LUCKY_BLOCK_ITEM = Registry.register(
        Registries.ITEM,
        LUCKY_BLOCK_ID,
        new LuckyBlockItem(LUCKY_BLOCK, new Item.Settings())
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

    private LuckyConfig config;

    @Override
    public void onInitialize() {
        config = LuckyConfig.load(FabricLoader.getInstance().getConfigDir().resolve("luckycobblemon.json"));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> entries.add(LUCKY_BLOCK_ITEM));
        PlayerBlockBreakEvents.AFTER.register(this::afterBlockBroken);
        BiomeModifications.addFeature(
            BiomeSelectors.foundInOverworld(),
            GenerationStep.Feature.VEGETAL_DECORATION,
            NATURAL_LUCKY_BLOCK_PLACED_KEY
        );

        LOGGER.info("Lucky Cobblemon {} initialized with {} base weighted outcomes and natural Overworld generation", "0.5.0", config.totalWeight());
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
            && state.isOf(LUCKY_BLOCK)) {
            int luck = blockEntity instanceof LuckyBlockEntity luckyBlockEntity ? luckyBlockEntity.getLuck() : 0;
            LuckyEffects.roll(serverWorld, serverPlayer, pos, config, luck);
        }
    }
}
