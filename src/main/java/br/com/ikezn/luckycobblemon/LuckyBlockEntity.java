package br.com.ikezn.luckycobblemon;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;

public final class LuckyBlockEntity extends BlockEntity {
    private static final String LUCK_KEY = "Luck";
    private int luck;

    public LuckyBlockEntity(BlockPos pos, BlockState state) {
        super(LuckyCobblemonMod.LUCKY_BLOCK_ENTITY, pos, state);
    }

    public int getLuck() {
        return luck;
    }

    public void setLuck(int luck) {
        this.luck = Math.clamp(luck, -100, 100);
        markDirty();
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putInt(LUCK_KEY, luck);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        luck = Math.clamp(nbt.getInt(LUCK_KEY), -100, 100);
    }
}
