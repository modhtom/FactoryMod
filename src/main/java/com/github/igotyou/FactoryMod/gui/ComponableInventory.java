package com.github.igotyou.FactoryMod.gui;

import net.minecraft.server.level.ServerPlayer;

public class ComponableInventory {
    public ComponableInventory(String name, int rows, ServerPlayer player) {}
    
    public void addComponent(InventoryComponent component, Object slotPredicate) {}
    public void removeComponent(InventoryComponent component) {}
    public void clear() {}
    public void show() {}
    public void update() {}
    public void updatePlayerView() {}
}
