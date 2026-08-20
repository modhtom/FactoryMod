package com.github.igotyou.FactoryMod.gui;

import java.util.List;

public class Scrollbar implements InventoryComponent {
    public Scrollbar(List<IClickable> items, int size, int rowLength, Object aligner) {}
    public Scrollbar(List<IClickable> items, int size) {}
    
    public void setBackwardsClickSlot(int slot) {}
    
    @Override
    public int getSize() { return 0; }
}
