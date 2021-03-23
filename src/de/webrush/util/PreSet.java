package de.webrush.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.webrush.WeBrush;

/**
 * Represents a Collection of Slots with <br>
 * predefined items and commands to execute.
 */
public class PreSet {

    private final List<SlotOperation> slots;
    private final int delay;
    
    public PreSet(List<SlotOperation> slots, int delay) {
        this.slots = slots;
        this.delay = delay;
    }
    
    /**
     * Loads all slots in the Set as the
     * player and executes the commands.
     */
    public void loadAll(Runnable callback, Player player) {
        HotbarLoader queue = HotbarLoader.valueOf(slots, delay, callback, player);
        queue.run();
    }
    
    /**
     * POJO class which represents a single slot
     * with item and commands to execute.
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
    
    /**
     * Mutable Queue which iterates over the flattened preset and delays 
     * between actions, to ensure correct order for async commands.
     */
    private static class HotbarLoader implements Runnable {
        private final long delay;
        private final List<Object> cmds;
        private final Player player;
        private final Runnable callback;
        private final WeBrush plugin;
        
        private int index;
        
        private HotbarLoader(List<Object> list, int delay, WeBrush plugin, Runnable callback, Player player) {
            this.cmds   = list;
            this.delay  = delay;
            this.plugin = plugin;
            this.callback = callback;
            this.player = player;
        }
        
        /**
         * Creates a new flattened Queue from the preset for a player
         * with callback code to execute if the queue finishes.
         */
        private static HotbarLoader valueOf(List<SlotOperation> slots, int delay, Runnable callback, Player player) {
            HotbarLoader queue = new HotbarLoader(new ArrayList<>(), delay, WeBrush.getInstance(), callback, player);
            queue.addSlots(slots);
            return queue;
        }
        
        private void addSlots(List<SlotOperation> slots) {
            for (SlotOperation slot : slots) {
                addOperation(slot);
                addCommands(slot.cmds);
            }
        }
        
        private void addCommands(List<String> cmds) {
            for (String cmd : cmds) {
                addOperation(cmd);
            }
        }
        
        private void addOperation(Object cmd) {
            this.cmds.add(cmd);
        }
        
        @Override
        public void run() {
            runDelayed();
        }
        
        private void runDelayed() {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                if (index == cmds.size()) {
                    callback.run();
                    return;
                }
                execute(cmds.get(index));
                index++;
                runDelayed();
            }, delay);
        }
        
        private void execute(Object cmd) {
            if (cmd instanceof String) {
                player.performCommand((String) cmd);
            }
            else if (cmd instanceof SlotOperation) {
                SlotOperation op = (SlotOperation) cmd;
                player.getInventory().setHeldItemSlot(op.slot);
                player.getInventory().setItem(op.slot, op.item);
            }
        }
    }
}
