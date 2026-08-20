package com.github.igotyou.FactoryMod;

import java.util.UUID;

public class FactoryModPlayerSettings {

    private final FactoryMod plugin;

    public FactoryModPlayerSettings(FactoryMod plugin) {
        this.plugin = plugin;
    }

    public IoConfigDirectionMode getIoDirectionMode(UUID id) {
        return IoConfigDirectionMode.VISUAL_RELATIVE;
    }

    public enum IoConfigDirectionMode {

        VISUAL_RELATIVE(
            "Relative Directions",
            new String[]{
                "The furnace shows the",
                "front of the factory."
            }),
        CARDINAL(
            "Cardinal Directions",
            new String[]{
                "Cardinals (for those too",
                "familiar with map mods.)"
            });

        public final String simpleDescription;
        public final String[] fullDescription;

        private IoConfigDirectionMode(String simpleDescription, String[] fullDescription) {
            this.simpleDescription = simpleDescription;
            this.fullDescription = fullDescription;
        }
    }

}
