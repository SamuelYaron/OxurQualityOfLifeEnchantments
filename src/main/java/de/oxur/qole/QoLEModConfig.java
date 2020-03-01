package de.oxur.qole;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

public class QoLEModConfig {

    private static final ForgeConfigSpec serverSpec;
    public static final Server SERVER;

    static {
        final Pair<Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Server::new);
        serverSpec = specPair.getRight();
        SERVER = specPair.getLeft();

    }

    public static void register(final ModLoadingContext context) {
        context.registerConfig(ModConfig.Type.SERVER, serverSpec);
    }

    public static class Server {
        public final ForgeConfigSpec.BooleanValue veinminerEnabled;
        public final ForgeConfigSpec.IntValue veinminerSizeFactor;
        public final ForgeConfigSpec.BooleanValue hoeHarvestEnabled;

        Server(final ForgeConfigSpec.Builder builder) {
            builder.comment("Enable and disable certain extra features").push("extraFeatures");
            hoeHarvestEnabled = builder
                    .comment("Enables or disables harvesting crops with the hoe.")
                    .define("hoeHarvestEnabled", true);
            builder.pop();
            builder.comment("Enable and disable enchantments").push("enchantments");
            veinminerEnabled = builder
                    .comment("Enables or disables the veinminer enchantment")
                    .define("veinminerEnabled", true);
            veinminerSizeFactor = builder
                    .comment("Specifies the veinminer size factor (e.g. 3 stands for a maximum vein size of 3*enchantment level).")
                    .defineInRange("veinminerSizeFactor", 3, 0, Integer.MAX_VALUE);
            builder.pop();
        }
    }
}
