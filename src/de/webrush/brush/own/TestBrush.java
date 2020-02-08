package de.webrush.brush.own;

import java.util.ArrayList;
import java.util.HashSet;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockTypes;

import de.webrush.ShapeCycler;
import de.webrush.ShapeCycler.BrushFunction;


public class TestBrush implements Brush {
  
    @Override
    public void build(EditSession session, BlockVector3 vec, Pattern pattern, double size) throws MaxChangedBlocksException {
        ArrayList<BlockVector3> blocks = new ArrayList<>();
        
        BrushFunction testbrush = (x, y, z, distance) -> {
            BlockVector3 current = BlockVector3.at(x, y, z);
            blocks.add(current);
        };
        
        ShapeCycler cycler = new ShapeCycler(testbrush, size);
        cycler.run2(vec, true);
        
        HashSet<BlockVector3> duplicates = new HashSet<>();
        
        for (BlockVector3 pos : blocks) {
            if (duplicates.contains(pos) ) {
                session.setBlock(pos, BlockTypes.RED_WOOL.getDefaultState());
            } else {
                session.setBlock(pos, pattern);
                duplicates.add(pos);
            }
        }
    }
      
}
