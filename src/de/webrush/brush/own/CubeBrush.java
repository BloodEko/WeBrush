package de.webrush.brush.own;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;

/**
 * Creates an precise cube with an volume of size³.
 */
public class CubeBrush implements Brush {
    
    @Override
    public void build(EditSession session, BlockVector3 pos, Pattern pattern, double size)
            throws MaxChangedBlocksException {
        
        int width = (int) size;
        
        int startX = pos.getX() - width / 2;
        int startY = pos.getY() - width / 2;
        int startZ = pos.getZ() - width / 2;
        
        int endX   = pos.getX() + width / 2;
        int endY   = pos.getY() + width / 2;
        int endZ   = pos.getZ() + width / 2;
        
        if (width % 2 == 1) {
            endX++;
            endY++;
            endZ++;
        }
        
        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                for (int z = startZ; z < endZ; z++) {
                    session.setBlock(BlockVector3.at(x, y, z), pattern);
                }
            }
        }
        
    }

}
