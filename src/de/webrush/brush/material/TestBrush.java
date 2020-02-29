package de.webrush.brush.material;

import java.util.ArrayList;
import java.util.HashSet;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.block.BlockTypes;

import de.webrush.Shaper;
import de.webrush.Shaper.BrushFunction;


public class TestBrush implements Brush {
  
    @Override
    public void build(EditSession session, BlockVector3 click, Pattern pattern, double size) throws MaxChangedBlocksException {
        ArrayList<BlockVector3> blocks = new ArrayList<>();
        BrushFunction testbrush = vec -> {
            blocks.add(vec);
        };
        
        Shaper.runSphere(testbrush, click, size);
        HashSet<BlockVector3> duplicates = new HashSet<>();
        
        for (BlockVector3 vec : blocks) {
            Pattern mat = duplicates.contains(vec) ? BlockTypes.RED_WOOL.getDefaultState() : pattern;
            session.setBlock(vec, mat);
            duplicates.add(vec);
        }
    }
      
}
