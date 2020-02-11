package de.webrush.brush.voxelsniper;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

/**
 * Replaces existing blocks by a specific depth. <br>
 * Similar to /b over d3 from VoxelSniper. <br>
 * <br>
 * Specify natural to overlay with dirt and grass simultaneously.
 */
public class OverlayBrush implements Brush {

    private final BlockState grassblock = BlockTypes.GRASS_BLOCK.getDefaultState();
    
    private final int     depth;
    private final boolean natural;
    
    public OverlayBrush(int depth , boolean natural) {
        this.depth   = depth;
        this.natural = natural;
    }
    
    @Override
    public void build(EditSession session, BlockVector3 pos, Pattern pattern, double size)
            throws MaxChangedBlocksException {
        
        final int    brushSize   = (int) size;
        final double brushSizeSq = Math.pow(brushSize + 0.5, 2);
        
        for (int z = brushSize; z >= -brushSize; z--) {
            for (int x = brushSize; x >= -brushSize; x--) {
                
                BlockType materialId = session.getBlock(BlockVector3.at(pos.getX() + x, pos.getY() + 1, pos.getZ() + z)).getBlockType();
                if (isSolid(materialId)) { // check if column is valid
                    continue;              // column is valid if it has no solid block right above the clicked layer
                }
                if ((Math.pow(x, 2) + Math.pow(z, 2)) > brushSizeSq) {
                    continue;
                }
                
                for (int y = pos.getY(); y > 0; y--) {
                    BlockType layerBlockId = session.getBlock(BlockVector3.at(pos.getX() + x, y, pos.getZ() + z)).getBlockType();
                    if (isUnsolid(layerBlockId)) { // check for surface
                        continue;
                    }
                    
                    for (int currentDepth = y; y - currentDepth < depth; currentDepth--) {
                        BlockVector3 currentVec = BlockVector3.at(pos.getX() + x, normalizeY(currentDepth), pos.getZ() + z);
                        if (natural && y == currentDepth) { //top layer
                            session.setBlock(currentVec, grassblock);
                            continue;
                        }
                        BlockType currentBlockId = session.getBlock(currentVec).getBlockType();
                        if (isSolid(currentBlockId)) {
                            session.setBlock(currentVec, pattern);
                        }
                    }
                    break;
                }
            }
        }
          
    }
    
    private int normalizeY(int y) {
        if (y < 0) {
            return 0;
        }
        if (y > 256) {
            return 256;
        }
        return y;
    }
    
    private boolean isUnsolid(BlockType type) {
        return !type.getMaterial().isSolid();
    }
    
    private boolean isSolid(BlockType type) {
        return type.getMaterial().isSolid();
    }

}
