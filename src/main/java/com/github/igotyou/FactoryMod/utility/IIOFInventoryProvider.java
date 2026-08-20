package com.github.igotyou.FactoryMod.utility;

import net.minecraft.world.Container;
import org.jetbrains.annotations.Nullable;

/**
 * Utility interface for classes that provide separate inventories for inputs, outputs, and fuel.
 */
public interface IIOFInventoryProvider {

    Container getInputInventory();

    Container getOutputInventory();

    @Nullable
    Container getFuelInventory();

    int getInputCount();

    int getOutputCount();

    int getFuelCount();

    default int getTotalIOFCount() {
        return getInputCount() + getOutputCount() + getFuelCount();
    }

}
