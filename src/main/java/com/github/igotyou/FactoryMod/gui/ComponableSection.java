package com.github.igotyou.FactoryMod.gui;

public class ComponableSection implements InventoryComponent {
    public ComponableSection(int size) {}
    
    public void addComponent(InventoryComponent component, Object slotPredicate) {}
    
    @Override
    public int getSize() { return 0; }
}
