package de.oxur.qole.enchantments;

import de.oxur.qole.QoLEModConfig;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class VeinminerEnchantment extends Enchantment {

    private static Random randGen = new Random();

    VeinminerEnchantment(Rarity rarityIn, EnchantmentType typeIn, EquipmentSlotType... slots) {
        super(rarityIn, typeIn, slots);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinEnchantability(int enchantmentLevel) {
        return 5 + 10 * (enchantmentLevel - 1);
    }

    @Override
    public boolean canApply(ItemStack stack) {
        if (!QoLEModConfig.SERVER.veinminerEnabled.get()) return false;
        return stack.getItem() instanceof PickaxeItem;
    }

    /**
     * Calculates a vein of (same) ore block starting from origin and containing a maximum number of blocks depending on level.
     * @param level The level factor (usually the enchantment level)
     * @param origin The position to calculate the vein from
     * @param world The world to calculate the vein in
     * @return List of all BlockPos considered part of the vein.
     */
    public List<BlockPos> getVein(int level, BlockPos origin, IWorld world) {
        List<BlockPos> workStack = new ArrayList<>();
        List<BlockPos> toMine = new ArrayList<>();
        workStack.add(origin);
        final ResourceLocation blockType = world.getBlockState(origin).getBlock().getRegistryName();
        final int limit = level * QoLEModConfig.SERVER.veinminerSizeFactor.get();
        while (toMine.size() < limit && workStack.size() > 0) {
            int fetchIndex = randGen.nextInt(workStack.size());
            BlockPos currentPos = workStack.get(fetchIndex);
            workStack.remove(fetchIndex);
            toMine.add(currentPos);
            BlockPos[] positions = {currentPos.up(), currentPos.down(), currentPos.east(), currentPos.west(), currentPos.north(), currentPos.south()};
            for (BlockPos pos : positions) {
                if (!toMine.contains(pos) && world.getBlockState(pos).getBlock().getRegistryName() == blockType) {
                    workStack.add(pos);
                }
            }
        }
        return toMine;
    }
}
