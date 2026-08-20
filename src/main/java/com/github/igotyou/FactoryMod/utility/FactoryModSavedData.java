package com.github.igotyou.FactoryMod.utility;

/**
 * Factory persistence is restored after the factory model is migrated.  Keeping
 * this as a dedicated type prevents runtime state from being confused with the
 * old Paper SQL representation during the transition.
 */
public final class FactoryModSavedData {

    public FactoryModSavedData() {}

}
