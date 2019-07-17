package de.oxur.qole.events;

import de.oxur.qole.QoLEModConfig;
import de.oxur.qole.enchantments.Enchantments;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
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
     * Handles the Veinminer enchantment when breaking a block. It only triggers if:
     * <ul>
     *     <li>Veinminer is enabled</li>
     *     <li>Block can be harvested by tool</li>
     *     <li>Block is an ore</li>
     *     <li>Player is not sneaking</li>
     *     <li>Player is not creative</li>
     * </ul>
     * @param event BreakEvent
     */
    @SubscribeEvent
    public void handleVeinminerEnchantment(BlockEvent.BreakEvent event) {
        if(!QoLEModConfig.SERVER.veinminerEnabled.get()) return;
        PlayerEntity player = event.getPlayer();
        BlockPos eventPos = event.getPos();
        IWorld world = event.getWorld();
        BlockState eventBlockState = world.getBlockState(eventPos);
        ItemStack toolStack = player.getHeldItemMainhand();
        int level = 0;
        if (!eventBlockState.getBlock().canHarvestBlock(eventBlockState, world, eventPos, player)) return;
        if (!eventBlockState.getBlock().getTags().contains(Tags.Blocks.ORES.getId())) return;
        if (player.abilities.isCreativeMode) return;
        if (player.isSneaking()) return;
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
            blockState.getBlock().harvestBlock((World)world, player, pos, blockState, null, toolStack);
        }
        toolStack.attemptDamageItem(toMine.size(), randGen, null);
    }
}
