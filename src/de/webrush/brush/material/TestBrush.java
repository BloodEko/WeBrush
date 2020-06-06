package de.webrush.brush.material;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;

import de.webrush.util.Shaper;

/**
 * Experimental brush.
 * This is the reversed flatten brush.
 */
public class TestBrush implements Brush {
    
    final int height;
    final int blocks;
    
    public TestBrush(int height, int blocks) {
        this.height  = height;
        this.blocks  = blocks;
    }
    
    
    @Override
    public void build(EditSession session, BlockVector3 pos, Pattern block, double radius) throws MaxChangedBlocksException {

        block   = BlockTypes.AIR.getDefaultState();
        int min = pos.getY();
        int max = pos.getY() + height;
        
        for (BlockVector3 vec : Shaper.getDisc(pos, radius)) {
            int x = vec.getX();
            int z = vec.getZ();
            
            for (int y = min + 1; y <= max; y++) { //scan to find solid block
                if (isSolid(session.getBlock(BlockVector3.at(x, y, z)))) {
                    
                    for (int i = 0; i < blocks; i++) { // remove from solid blocks
                        int height = y + i;
                        if (height > max) {
                            break;
                        }
                        if (!isSolid(session.getBlock(BlockVector3.at(x, height, z)))) {
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
