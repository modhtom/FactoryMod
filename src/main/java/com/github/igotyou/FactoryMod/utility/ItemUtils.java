package com.github.igotyou.FactoryMod.utility;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

public class ItemUtils {

    public static void setLore(ItemStack is, String... lore) {
        List<Component> textList = Arrays.stream(lore).map(Component::literal).collect(Collectors.toList());
        is.set(DataComponents.LORE, new ItemLore(textList));
    }

    public static void addLore(ItemStack is, String... lore) {
        List<Component> currentLore = new ArrayList<>();
        ItemLore existing = is.get(DataComponents.LORE);
        if (existing != null) {
            currentLore.addAll(existing.lines());
        }
        for (String line : lore) {
            currentLore.add(Component.literal(line));
        }
        is.set(DataComponents.LORE, new ItemLore(currentLore));
    }

    public static void setDisplayName(ItemStack is, String name) {
        is.set(DataComponents.CUSTOM_NAME, Component.literal(name));
    }

    public static String getDisplayName(ItemStack is) {
        Component name = is.get(DataComponents.CUSTOM_NAME);
        if (name != null) return name.getString();
        return is.getHoverName().getString();
    }

    public static String getItemName(ItemStack is) {
        return getDisplayName(is);
    }
    
    public static String getItemName(Item is) {
        return is.getName(new ItemStack(is)).getString();
    }

    public static boolean isValidItem(ItemStack is) {
        return is != null && !is.isEmpty();
    }
}
