package com.github.igotyou.FactoryMod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FactoryMod implements ModInitializer {

    private FactoryModManager manager;
    private static FactoryMod plugin;
    
    private static final Logger LOGGER = LoggerFactory.getLogger("FactoryMod");

    @Override
    public void onInitialize() {
        plugin = this;
        com.github.igotyou.FactoryMod.utility.TaskScheduler.init();
        com.github.igotyou.FactoryMod.commands.FactoryModCommand.register();
        
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            // Parse config first — factory definitions must exist before loading saved factories
            ConfigParser cp = new ConfigParser(this, server);
            manager = cp.parse();
            manager.setServer(server);
        });

        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // Load saved factories now that the world and all chunks are ready
            if (manager != null) {
                manager.loadFactories();
            }
        });
        
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (manager != null) {
                manager.tickFactories();
            }
        });
        
        net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            onDisable();
        });
        
        registerListeners();
        LOGGER.info("FactoryMod successfully enabled");
    }

    public void onDisable() {
        if (manager != null) {
            manager.shutDown();
        }
        LOGGER.info("FactoryMod shutting down");
    }

    public FactoryModManager getManager() {
        return manager;
    }

    public static FactoryMod getInstance() {
        return plugin;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public void info(String message) {
        LOGGER.info(message);
    }
    
    public void warning(String message) {
        LOGGER.warn(message);
    }
    
    public void severe(String message) {
        LOGGER.error(message);
    }

    private void registerListeners() {
        com.github.igotyou.FactoryMod.listeners.InteractionListener.register();
        
    }
}
