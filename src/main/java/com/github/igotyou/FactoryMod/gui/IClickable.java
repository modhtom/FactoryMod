package com.github.igotyou.FactoryMod.gui;

import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;

public interface IClickable {
    ItemStack getItemStack();
    void clicked(ServerPlayer p);
    void addedToInventory(ClickableInventory inv, int slot);
}
