package com.github.igotyou.FactoryMod.events;
import com.github.igotyou.FactoryMod.factories.Factory;
import net.minecraft.server.level.ServerPlayer;
public class FactoryActivateEvent {
    public FactoryActivateEvent(Factory f, ServerPlayer p) {}
    public boolean isCancelled() { return false; }
    public void setCancelled(boolean c) {}
}
