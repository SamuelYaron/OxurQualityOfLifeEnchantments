package de.oxur.qole.events;

import net.minecraftforge.common.MinecraftForge;

public abstract class AbstractEventHandler {

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }
}
