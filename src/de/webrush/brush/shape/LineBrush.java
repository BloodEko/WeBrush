package de.webrush.brush.shape;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.RegionSelector;

import net.md_5.bungee.api.ChatColor;

/**
 * Brushes a line from the first selected point
 * to the clicked location.
 */
public class LineBrush implements Brush {

    private final RegionSelector selector;
    private final BukkitPlayer   player;
    
    
    public LineBrush(BukkitPlayer player, LocalSession session) {
        this.selector = session.getRegionSelector(player.getWorld());
        this.player   = player;
    }
    
    
    @Override
    public void build(EditSession session, BlockVector3 click, Pattern pattern, double size)
            throws MaxChangedBlocksException { 
        try {
            session.drawLine(pattern, selector.getPrimaryPosition(), click, size, true);
        } 
        catch (IncompleteRegionException ex) {
            player.print(ChatColor.RED + "First position undefined: " + ex.getMessage());
        }
    }
    
}
