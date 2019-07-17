package de.oxur.qole.enchantments;

import de.oxur.qole.QoLEMod;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ObjectHolder(QoLEMod.MODID)
public class Enchantments {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final VeinminerEnchantment VEINMINER = null;

    @Mod.EventBusSubscriber(modid = QoLEMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistrationHandler {

        @SubscribeEvent
        public static void registerEnchantments(final RegistryEvent.Register<Enchantment> event) {
            LOGGER.debug("Registering enchantments of {}", QoLEMod.MODID);
            IForgeRegistry<Enchantment> registry = event.getRegistry();
            Enchantment[] enchantments = {
                    new VeinminerEnchantment(Enchantment.Rarity.UNCOMMON, EnchantmentType.DIGGER, EquipmentSlotType.MAINHAND).setRegistryName("veinminer")
            };
            for (Enchantment enchantment : enchantments) {
                registry.register(enchantment);
            }
        }
    }
}
