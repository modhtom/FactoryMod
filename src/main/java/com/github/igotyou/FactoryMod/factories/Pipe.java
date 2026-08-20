package com.github.igotyou.FactoryMod.factories;
import com.github.igotyou.FactoryMod.structures.MultiBlockStructure;
import net.minecraft.server.level.ServerPlayer;
public class Pipe extends Factory { public Pipe(String n, MultiBlockStructure m, int u) { super(n, m, u); } public void attemptToActivate(ServerPlayer p, boolean b) {} public String getLogData() { return null; } }
