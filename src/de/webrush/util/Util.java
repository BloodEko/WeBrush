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
    /** The highest block excluded of the world.*/
    public static final int WORLD_HEIGHT = 320;
    
    /** The lowest block included in the world. */
    public static final int WORLD_DEPTH = -64;
    
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
    
    /**
     * Returns the string after the given token.
     * Returns an empty string, if it doesn't contain the token.
     * Otherwise returns the original string.
     */
    public static String after(String str, String token) {
        int at = str.indexOf(token);
        if (at == -1) {
            return "";
        }
        return str.substring(at + token.length());
    }
    
    /**
     * Returns the string before the last occurrence of the token.
     * Or the original string.
     */
    public static String beforeLast(String str, String token) {
        int at = str.lastIndexOf(token);
        if (at == -1) {
            return str;
        }
        return str.substring(0, at);
    }
}
