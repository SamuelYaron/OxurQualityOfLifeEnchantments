package de.oxur.qole.events;

import de.oxur.qole.QoLEModConfig;
import de.oxur.qole.enchantments.Enchantments;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;
import net.minecraft.tags.BlockTags;
import net.minecraft.item.HoeItem;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;

public class BreakEventHandler extends AbstractEventHandler {

    public static final BreakEventHandler INSTANCE = new BreakEventHandler();

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random randGen = new Random();

    /**
     * Handles the case that a Harvestable Block is destroyed while using a hoe. It only triggers if:
     * <ul>
     *     <li>HoeHarvest is enabled</li>
     *     <li>Block can be harvested by tool</li>
     *     <li>Block is a Crop</li>
     * <ul/>
     *
     * @param event BreakEvent
     */
    @SubscribeEvent
    public void handleCropBreaking(BlockEvent.BreakEvent event) {
        if (!QoLEModConfig.SERVER.hoeHarvestEnabled.get()) return;
        PlayerEntity player = event.getPlayer();
        IWorld world = event.getWorld();
        BlockPos eventPos = event.getPos();
        BlockState eventBlockState = world.getBlockState(eventPos);
        ItemStack toolStack = player.getHeldItemMainhand();
        if (world.isRemote()) return;
        if (!(eventBlockState.getBlock() instanceof CropsBlock)) return;
        if (!(toolStack.getItem() instanceof HoeItem)) return;
        if (!eventBlockState.getBlock().getTags().contains(BlockTags.CROPS.getId())) return;
        if (!eventBlockState.getBlock().canHarvestBlock(eventBlockState, world, eventPos, player)) return;
        event.setCanceled(true);
        CropsBlock cropsBlock = (CropsBlock) eventBlockState.getBlock();
        if (!cropsBlock.isMaxAge(eventBlockState)) return;

        List<ItemStack> drops = CropsBlock.getDrops(eventBlockState, (ServerWorld) world, eventPos, null, null, toolStack);
        drops.forEach((itemStack) -> {
            if (itemStack.getItem().getTags().contains(Tags.Items.SEEDS.getId())) {
                LOGGER.debug("Removing one seed from drops");
                int stackCount = itemStack.getCount();
                if (stackCount > 1) {
                    itemStack.setCount(itemStack.getCount() - 1);
                    CropsBlock.spawnAsEntity((World) world, eventPos, itemStack);
                }
            } else {
                CropsBlock.spawnAsEntity((World) world, eventPos, itemStack);
            }
        });
        ((World) world).setBlockState(eventPos, cropsBlock.withAge(0));
        toolStack.attemptDamageItem(1, randGen, null);
    }


    /**
     * Handles the Veinminer enchantment when breaking a block. It only triggers if:
     * <ul>
     *     <li>Veinminer is enabled</li>
     *     <li>Block can be harvested by tool</li>
     *     <li>Block is an ore</li>
     *     <li>Player is not sneaking</li>
     *     <li>Player is not creative</li>
     * </ul>
     *
     * @param event BreakEvent
     */
    @SubscribeEvent
    public void handleVeinminerEnchantment(BlockEvent.BreakEvent event) {
        if (!QoLEModConfig.SERVER.veinminerEnabled.get()) return;
        PlayerEntity player = event.getPlayer();
        BlockPos eventPos = event.getPos();
        IWorld world = event.getWorld();
        BlockState eventBlockState = world.getBlockState(eventPos);
        ItemStack toolStack = player.getHeldItemMainhand();
        int level = 0;
        if (!eventBlockState.getBlock().canHarvestBlock(eventBlockState, world, eventPos, player)) return;
        if (!eventBlockState.getBlock().getTags().contains(Tags.Blocks.ORES.getId())) return;
        if (player.abilities.isCreativeMode) return;
        if (player.isCrouching()) return;
        ListNBT enchantments = toolStack.getEnchantmentTagList();
        for (INBT enchantment : enchantments) {
            if (!(enchantment instanceof CompoundNBT)) return;
            CompoundNBT enchantmentNBT = (CompoundNBT) enchantment;
            if (enchantmentNBT.getString("id").equals(Enchantments.VEINMINER.getRegistryName().toString())) {
                level = enchantmentNBT.getInt("lvl");
                break;
            }
        }

        if (level == 0) return;
        List<BlockPos> toMine = Enchantments.VEINMINER.getVein(level, eventPos, world);
        if (toMine.size() == 1) return;
        event.setCanceled(true);
        for (BlockPos pos : toMine) {
            BlockState blockState = world.getBlockState(pos);
            world.getBlockState(pos).removedByPlayer((World) world, pos, player, true, null);
            blockState.getBlock().harvestBlock((World) world, player, pos, blockState, null, toolStack);
        }
        toolStack.attemptDamageItem(toMine.size(), randGen, null);
    }
}
