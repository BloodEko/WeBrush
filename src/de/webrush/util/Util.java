package de.webrush.util;

import org.bukkit.entity.Player;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;

/**
 * Bundles various utilities.
 */
public class Util {
    
    /**
     * Sends a message in WorldEdit color.
     */
    public static void printMessage(BukkitPlayer player, String message) {
        player.print(TextComponent.of(message, TextColor.LIGHT_PURPLE));
    }

    /**
     * Sends a message in error color.
     */
    public static void printError(BukkitPlayer player, String message) {
        player.print(TextComponent.of(message, TextColor.RED));
    }

    /**
     * Sets the held item slot to the index.
     */
    public static void setHeldItemSlot(BukkitPlayer player, int slot) {
        Player pl = BukkitAdapter.adapt(player);
        pl.getInventory().setHeldItemSlot(slot);
    }
}
