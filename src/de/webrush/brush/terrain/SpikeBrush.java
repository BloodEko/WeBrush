package de.webrush.brush.terrain;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;

import de.webrush.util.Shaper;

/**
 * Top-down brush which raises the land upon solid blocks. <br>
 * There is no cap. 
 */
public class SpikeBrush implements Brush {
    
    final int height;
    final int blocks;
    
    public SpikeBrush(int height, int blocks) {
        this.height  = height;
        this.blocks  = blocks;
    }
    
    
    @Override
    public void build(EditSession session, BlockVector3 pos, Pattern block, double radius) throws MaxChangedBlocksException {
        
        pos     = getBlock(session, pos);
        block   = session.getBlock(pos);
        pos     = BlockVector3.at(pos.getX(), pos.getY() + blocks, pos.getZ());
        
        int min = pos.getY() - height;
        int max = pos.getY() + blocks;
        
        for (BlockVector3 vec : Shaper.getDisc(pos, radius)) {
            int x = vec.getX();
            int z = vec.getZ();
            
            for (int y = vec.getY() - 1; y > min; y--) { //scan to find solid block
                if (isSolid(session.getBlock(BlockVector3.at(x, y, z)))) {
                    
                    for (int i = 0; i < blocks; i++) { // build up on solid block
                        int height = y + i + 1;
                        if (height > max) {
                            break;
                        }
                        if (isSolid(session.getBlock(BlockVector3.at(x, height + 1, z)))) {
                            break;
                        }
                        
                        session.setBlock(BlockVector3.at(x, height, z), block);
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * Scans down the height, to find a solid BlockVector
     * to build upon. Adds support for water.
     */
    private BlockVector3 getBlock(EditSession session, BlockVector3 pos) {
        for (int i = 0; i < height; i++) {
            
            BlockVector3 block = pos.subtract(0, i, 0);
            if (isSolid(session.getBlock(block))) {
                return block;
            }
        }
        return pos;
    }
    
    private boolean isSolid(BlockState state) {
        return state.getBlockType().getMaterial().isSolid();
    }
    
}
