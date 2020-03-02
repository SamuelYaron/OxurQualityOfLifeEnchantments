package de.oxur.qole.events;

import de.oxur.qole.QoLEModConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class RightClickBlockEventHandler extends AbstractEventHandler {
    public static final RightClickBlockEventHandler INSTANCE = new RightClickBlockEventHandler();

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Handles the case that a Crop Block is right clicked. It only triggers if:
     * <ul>
     *     <li>HoeHarvest is enabled</li>
     *     <li>Block is a Crop</li>
     *     <li>Block is maxAge</li>
     * <ul/>
     *
     * @param event BreakEvent
     */
    @SubscribeEvent
    public void handleCropBreaking(PlayerInteractEvent.RightClickBlock event) {
        if (!QoLEModConfig.SERVER.hoeHarvestEnabled.get()) return;
        PlayerEntity player = event.getPlayer();
        World world = event.getWorld();
        BlockPos eventPos = event.getPos();
        BlockState eventBlockState = world.getBlockState(eventPos);
        ItemStack toolStack = player.getHeldItemMainhand();
        if (world.isRemote()) return;
        if (!(eventBlockState.getBlock() instanceof CropsBlock)) return;
        if (!eventBlockState.getBlock().getTags().contains(BlockTags.CROPS.getId())) return;
        CropsBlock cropsBlock = (CropsBlock) eventBlockState.getBlock();
        if (!cropsBlock.isMaxAge(eventBlockState)) return;
        event.setCanceled(true);
        List<ItemStack> drops = CropsBlock.getDrops(eventBlockState, (ServerWorld) world, eventPos, null, null, toolStack);
        AtomicBoolean seedsPresent = new AtomicBoolean(false);
        drops.forEach((itemStack) -> {
            if (itemStack.getItem().getTags().contains(Tags.Items.SEEDS.getId())) {
                LOGGER.debug("Removing one seed from drops");
                itemStack.shrink(1);
                seedsPresent.set(true);
            }
        });
        if (!seedsPresent.get()) {
            LOGGER.debug("Removing a crop since no seeds were found.");
            drops.get(0).shrink(1);
        }
        drops.forEach((itemStack) -> {
            CropsBlock.spawnAsEntity(world, eventPos, itemStack);
        });
        world.setBlockState(eventPos, cropsBlock.withAge(0));
    }
}
