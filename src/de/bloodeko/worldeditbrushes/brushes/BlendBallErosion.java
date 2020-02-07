package de.bloodeko.worldeditbrushes.brushes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;

/**
 * Performs an erosion and smoothing operation in one. <br>
 * The blendradius will be added to the size, 1 seems to have best results.
 */
public class BlendBallErosion implements Brush {

    private BlendBallBrush blendBrush;
    private ErosionBrush   erosionBrush;
    private int            blendradius;
    
    public BlendBallErosion(BlendBallBrush blend, ErosionBrush erosion, int blendradius) {
        this.blendBrush   = blend;
        this.erosionBrush = erosion;
        this.blendradius  = blendradius;
    }
    
    @Override
    public void build(EditSession session, BlockVector3 position, Pattern pattern, double size)
            throws MaxChangedBlocksException {
        
        erosionBrush.build(session, position, pattern, size);
        session.flushSession();
        
        blendBrush.build(session, position, pattern, size + blendradius);
    }
    
}
