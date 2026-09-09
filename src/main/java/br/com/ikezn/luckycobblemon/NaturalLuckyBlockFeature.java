package br.com.ikezn.luckycobblemon;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/** Places one Lucky Block and assigns luck that belongs only to that block. */
public final class NaturalLuckyBlockFeature extends Feature<DefaultFeatureConfig> {
    public NaturalLuckyBlockFeature() {
        super(DefaultFeatureConfig.CODEC);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> context) {
        StructureWorldAccess world = context.getWorld();
        BlockPos pos = context.getOrigin();
        BlockState current = world.getBlockState(pos);
        BlockState floor = world.getBlockState(pos.down());

        if (!current.isAir() || floor.isAir() || !current.getFluidState().isEmpty()) {
            return false;
        }

        if (!world.setBlockState(pos, LuckyCobblemonMod.LUCKY_BLOCK.getDefaultState(), Block.NOTIFY_ALL)) {
            return false;
        }

        if (world.getBlockEntity(pos) instanceof LuckyBlockEntity blockEntity) {
            blockEntity.setLuck(rollNaturalLuck(context.getRandom()));
            return true;
        }

        // A failed block-entity creation should never leave a broken natural block behind.
        world.removeBlock(pos, false);
        return false;
    }

    static int rollNaturalLuck(Random random) {
        int special = random.nextInt(1000);
        if (special < 25) {
            return 100;
        }
        if (special < 50) {
            return -100;
        }

        // A triangular distribution makes moderate values common while still allowing -90..+90.
        return (random.nextInt(19) + random.nextInt(19) - 18) * 5;
    }
}
