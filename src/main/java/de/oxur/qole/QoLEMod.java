package de.oxur.qole;

import de.oxur.qole.events.BreakEventHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(QoLEMod.MODID)
public class QoLEMod {

    public static final String MODID = "oxur_qole";
    public static final String NAME = "Quality of Life Enchantments";

    private static final Logger LOGGER = LogManager.getLogger();

    public QoLEMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        QoLEModConfig.register(ModLoadingContext.get());
    }

    @SubscribeEvent
    public void setup(FMLCommonSetupEvent event) {
        LOGGER.debug("Setup event for {}", MODID);
        BreakEventHandler.INSTANCE.register();
    }

}
