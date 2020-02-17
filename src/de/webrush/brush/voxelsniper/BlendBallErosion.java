package de.webrush.brush.voxelsniper;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;

import de.webrush.ChangeTracker;

/**
 * Performs an erosion and smoothing operation in one. <br>
 * The blendradius will be added to the size, 1 seems to have best results.
 */
public class BlendBallErosion implements Brush {

    private BlendBallBrush blendBrush;
    private ErosionBrush   erosionBrush;
    private int            blendEdge;
    
    public BlendBallErosion(BlendBallBrush blend, ErosionBrush erosion, int blendEdge) {
        this.blendBrush   = blend;
        this.erosionBrush = erosion;
        this.blendEdge    = blendEdge;
    }
    
    @Override
    public void build(EditSession session, BlockVector3 position, Pattern pattern, double size)
            throws MaxChangedBlocksException {
        
        ChangeTracker tracker = new ChangeTracker(session);
        
        erosionBrush.build(tracker, position, pattern, size);
        blendBrush.build(tracker, position, pattern, size + blendEdge);
        
        tracker.writeToSession();
    }
    
}
