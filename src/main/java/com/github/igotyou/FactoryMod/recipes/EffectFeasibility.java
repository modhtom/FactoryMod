package com.github.igotyou.FactoryMod.recipes;

import org.jetbrains.annotations.Nullable;

public record EffectFeasibility(boolean isFeasible, @Nullable String reasonSnippet) {

}
