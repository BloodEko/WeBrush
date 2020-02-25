package de.webrush.brush.own;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitPlayer;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.ConvexPolyhedralRegion;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;

/**
 * Draws a Kochanek–Bartels spline through a convex selection. <br>
 * For Details on parameters check out: 
 * <a href="https://en.wikipedia.org/wiki/Kochanek%E2%80%93Bartels_spline">Wikipedia</a>
 */
public class BartelLine {

    final BukkitPlayer player;
    final LocalSession session;
    final EditSession  edit;
    
    final Pattern pattern;
    final double  size;
    final double  tension;
    final double  bias;
    final double  continuity;
    final double  quality;
    final boolean fill;
    
    public BartelLine(BukkitPlayer player, LocalSession session, Pattern pattern, 
                       double size, double tension, double bias, double continuity, double quality, boolean fill) {
        this.player  = player;
        this.session = session;
        this.edit    = getSession();
        this.pattern = pattern;
        this.size       = size;
        this.tension    = tension;
        this.bias       = bias;
        this.continuity = continuity;
        this.quality    = quality;
        this.fill       = fill;
    }

    public void build() throws MaxChangedBlocksException, IncompleteRegionException {
        Region selection = session.getSelection(player.getWorld());
        
        if (selection instanceof CuboidRegion) {
            drawLine((CuboidRegion) selection);
        }
        else if (selection instanceof ConvexPolyhedralRegion) {
            drawLine((ConvexPolyhedralRegion) selection);
        }
        
        edit.close();
        session.remember(edit);
    }
    
    private EditSession getSession() {
        return WorldEdit.getInstance().getEditSessionFactory().
                getEditSession(player.getWorld(), session.getBlockChangeLimit(), player);
    }
    
    private void drawLine(CuboidRegion region) throws MaxChangedBlocksException {
        List<BlockVector3> list = ImmutableList.of(region.getPos1(), region.getPos2());
        edit.drawSpline(pattern, list, tension, bias, continuity, quality, size, fill);
    }
    
    private void drawLine(ConvexPolyhedralRegion region) throws MaxChangedBlocksException {
        List<BlockVector3> list = new ArrayList<>();
        list.addAll(region.getVertices());
        edit.drawSpline(pattern, list, tension, bias, continuity, quality, size, fill);
    }
}
