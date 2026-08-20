package com.github.igotyou.FactoryMod.eggs;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import com.github.igotyou.FactoryMod.factories.Factory;
import net.minecraft.server.level.ServerPlayer;
public class PipeEgg implements IFactoryEgg {
    public String getName() { return null; }
    public Factory hatch(MultiBlockStructure m, ServerPlayer p) { return null; }
    public Factory revive(java.util.List<net.minecraft.core.BlockPos> b, int h, String r, int t) { return null; }
    public Class<? extends MultiBlockStructure> getMultiBlockStructure() { return null; }
    public double getReturnRate() { return 0.0; }
}
