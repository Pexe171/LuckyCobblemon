package br.com.ikezn.luckycobblemon;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public final class LuckyBlock extends BlockWithEntity {
    public static final MapCodec<LuckyBlock> CODEC = createCodec(LuckyBlock::new);
    // Match the compact, stepped shell instead of using an invisible full cube.
    private static final VoxelShape SHAPE = VoxelShapes.union(
        createCuboidShape(3, 0, 3, 13, 1.2, 13),
        createCuboidShape(2, 1.2, 3, 14, 12, 13),
        createCuboidShape(3, 1.2, 2, 13, 12, 14),
        createCuboidShape(3, 12, 3, 13, 13.1, 13),
        createCuboidShape(5.4, 13.1, 5.4, 10.6, 13.7, 10.6),
        createCuboidShape(5.1, 3.2, 1.02, 10.9, 9.8, 2),
        createCuboidShape(5.1, 3.2, 14, 10.9, 9.8, 14.98),
        createCuboidShape(1.02, 3.2, 5.1, 2, 9.8, 10.9),
        createCuboidShape(14, 3.2, 5.1, 14.98, 9.8, 10.9)
    );

    public LuckyBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        // Vanilla client-side display ticks: no server ticker, packets or animation library.
        if (random.nextInt(5) == 0) {
            double angle = random.nextDouble() * Math.PI * 2;
            world.addParticle(ParticleTypes.END_ROD,
                pos.getX() + 0.5 + Math.cos(angle) * 0.34,
                pos.getY() + 0.7 + random.nextDouble() * 0.15,
                pos.getZ() + 0.5 + Math.sin(angle) * 0.34,
                0, 0.009, 0);
        }
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new LuckyBlockEntity(pos, state);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (world.getBlockEntity(pos) instanceof LuckyBlockEntity blockEntity) {
            blockEntity.setLuck(LuckyBlockItem.getLuck(itemStack));
        }
    }
}
