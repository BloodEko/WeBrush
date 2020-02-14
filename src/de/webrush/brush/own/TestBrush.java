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
    public void build(EditSession session, BlockVector3 click, Pattern pattern, double size) throws MaxChangedBlocksException {
        ArrayList<BlockVector3> blocks = new ArrayList<>();
        
        BrushFunction testbrush = vec -> {
            blocks.add(vec);
        };
        
        new ShapeCycler(testbrush, size).run(click);
        
        HashSet<BlockVector3> duplicates = new HashSet<>();
        
        for (BlockVector3 temp : blocks) {
            if (duplicates.contains(temp) ) {
                session.setBlock(temp, BlockTypes.RED_WOOL.getDefaultState());
            } else {
                session.setBlock(temp, pattern);
                duplicates.add(temp);
            }
        }
    }
      
}
