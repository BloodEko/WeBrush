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
 * The height will be capped at the clicked block. 
 */
public class FlattenBrush implements Brush {
    
    final int height;
    final int blocks;
    
    public FlattenBrush(int height, int blocks) {
        this.height = height;
        this.blocks = blocks;
    }
    
    
    @Override
    public void build(EditSession session, BlockVector3 pos, Pattern block, double radius)
            throws MaxChangedBlocksException {
        
        block   = session.getBlock(pos);
        int min = pos.getY() - height;
        int max = pos.getY();
        
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
    
    private boolean isSolid(BlockState state) {
        return state.getBlockType().getMaterial().isSolid();
    }
    
}
