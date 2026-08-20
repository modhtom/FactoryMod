package com.github.igotyou.FactoryMod.gui;

import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import java.util.function.Consumer;

public class LClickable extends Clickable {
    private Consumer<ServerPlayer> action;
    
    public LClickable(ItemStack item, Consumer<ServerPlayer> action) {
        super(item);
        this.action = action;
    }
    
    public LClickable(ItemStack item, String name, Consumer<ServerPlayer> action) {
        super(item);
        this.action = action;
    }

    public LClickable(net.minecraft.world.item.Item item, String name, Consumer<ServerPlayer> action) {
        super(new ItemStack(item));
        this.action = action;
    }
    
    @Override
    public void clicked(ServerPlayer p) {
        if (action != null) {
            action.accept(p);
        }
    }
}
