package de.webrush.util;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a Collection of Slots with <br>
 * predefined items and commands to execute.
 */
public class PreSet {

    private final List<SlotOperation> slots;
    
    public PreSet(List<SlotOperation> slots) {
        this.slots = slots;
    }
    
    /**
     * Loads all slots in the Set as the
     * player and executes the commands.
     */
    public void loadAll(Player player) {
        for (SlotOperation slot : slots) {
            slot.load(player);
        }
        player.getInventory().setHeldItemSlot(0);
    }
    
    
    /**
     * Represents a single Slot with the
     * commands to execute.
     */
    public static class SlotOperation {
        
        private final int          slot;
        private final ItemStack    item;
        private final List<String> cmds;
        
        public SlotOperation(int slot, ItemStack item, List<String> cmds) {
            this.slot = slot;
            this.item = item;
            this.cmds = cmds;
        }
        
        /**
         * Loads the Slot as the Player
         * and executes the commands.
         */
        public void load(Player player) {
            player.getInventory().setHeldItemSlot(slot);
            player.getInventory().setItem(slot, item);
            for (String cmd : cmds) {
                player.performCommand(cmd);
            }
        }
        
        /**
         * Deserializes the PreSlot from
         * the inner ConfigurationSection.
         */
        public static SlotOperation valueOf(String slotname, ConfigurationSection slotSection) {
            int          slot = Integer.parseInt(slotname) - 1;
            String       name = slotSection.getKeys(false).iterator().next();
            ItemStack    item = new ItemStack(Material.valueOf(name.toUpperCase()));
            List<String> cmds = slotSection.getStringList(name);
            
            return new SlotOperation(slot, item, cmds);
        }
    }
}
