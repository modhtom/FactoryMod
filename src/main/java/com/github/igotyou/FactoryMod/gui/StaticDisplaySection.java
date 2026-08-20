package com.github.igotyou.FactoryMod.gui;

public class StaticDisplaySection implements InventoryComponent {
    public StaticDisplaySection(int size) {}
    public StaticDisplaySection(IClickable... clicks) {}
    
    public void set(IClickable click, int slot) {}
    
    @Override
    public int getSize() { return 0; }
    
    protected void rebuild() {}
}
